/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.broker.rs.internal;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpOperationFailedException;
import org.apache.camel.component.http.SSLContextParametersSecureProtocolSocketFactory;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.ShsLabel;

/**
 * Defines pipeline for processing and routing SHS synchronous messages
 */
public class SynchBrokerRouteBuilder extends RouteBuilder {

    private boolean enableStreamCaching = false;

    public void setEnableStreamCaching(boolean enabled) {
        this.enableStreamCaching = enabled;
    }

    @Override
    public void configure() throws Exception {
        getContext().setStreamCaching(enableStreamCaching);

        configureSsl();
        
        onException(Exception.class)
        .useOriginalMessage()
        .log(LoggingLevel.INFO, "Exception caught: ${exception.stacktrace}")
        .handled(false);

        onException(HttpOperationFailedException.class)
                .onWhen(header(ShsHeaders.X_SHS_ERRORCODE).isNotNull())
                .useOriginalMessage()
                .handled(false)
                .beanRef("remoteMessageHandlingErrorHandler");

        from("direct-vm:shs:synch").routeId("direct-vm:shs:synch")
// Disabled content based (agreement based) routing for synchronous messages
// to avoid needing to transform the mime request.
//        .setProperty(RecipientLabelTransformer.PROPERTY_SHS_RECEIVER_LIST,
//                method("shsRouter", "resolveRecipients(${property.ShsLabel})"))
//        .bean(RecipientLabelTransformer.class, "transform(${property.ShsLabel},*)")
//        .beanRef("commonNameTransformer")
        .beanRef("agreementService", "validateAgreement(${property.ShsLabel})")
        .removeHeaders("CamelHttp*")
        .setHeader(Exchange.CONTENT_TYPE, constant("message/rfc822"))
//		.setProperty("request", body())
        .choice()
        .when().method("shsRouter", "isLocal(${property.ShsLabel})")
            .to("direct:sendSynchLocal")
        .otherwise()
            .to("direct:sendSynchRemote")
        .end();
//        .bean(ReplyLabelProcessor.class);

        from("direct:sendSynchRemote").routeId("direct:sendSynchRemote")
        .setHeader(Exchange.HTTP_URI, method("shsRouter", "resolveEndpoint(${property.ShsLabel})"))
        .to("http://shsServer?httpClient.soTimeout=300000");

        from("direct:sendSynchLocal").routeId("direct:sendSynchLocal")
        .setHeader(ShsHeaders.DESTINATION_URI, method("shsRouter", "resolveEndpoint(${property.ShsLabel})"))
        .to("shs:local");
    }

    private void configureSsl() {
        SSLContextParameters sslContextParameters = getContext().getRegistry().lookupByNameAndType("mySslContext", SSLContextParameters.class);

        ProtocolSocketFactory factory =
                new SSLContextParametersSecureProtocolSocketFactory(sslContextParameters);

        Protocol.registerProtocol("https",
                new Protocol(
                        "https",
                        factory,
                        443));
    }

    static public class ReplyLabelProcessor {
        public ShsMessage fixReply(ShsMessage reply) {
            ShsLabel label = reply.getLabel();
            if (label.getSequenceType() != SequenceType.REPLY
                    && label.getSequenceType() != SequenceType.ADM) {
                label.setSequenceType(SequenceType.REPLY);
            }

            return reply;
        }
    }
}
