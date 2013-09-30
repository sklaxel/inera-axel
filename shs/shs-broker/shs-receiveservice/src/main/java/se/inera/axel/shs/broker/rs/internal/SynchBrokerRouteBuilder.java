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
import org.apache.camel.builder.RouteBuilder;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.ShsLabel;

/**
 * Defines pipeline for processing and routing SHS synchronous messages
 */
public class SynchBrokerRouteBuilder extends RouteBuilder {


    @Override
    public void configure() throws Exception {

        from("direct-vm:shs:synch").routeId("direct-vm:shs:synch")
        .setProperty(RecipientLabelTransformer.PROPERTY_SHS_RECEIVER_LIST, method("shsRouter", "resolveRecipients(${body.label})"))
        .bean(RecipientLabelTransformer.class, "transform(${body.label},*)")
        .beanRef("agreementService", "validateAgreement(${body.label})")
        .choice()
        .when().method("shsRouter", "isLocal(${body.label})")
        .to("direct:sendSynchLocal")
        .otherwise()
        .to("direct:sendSynchRemote")
        .end();

        from("direct:sendSynchRemote").routeId("direct:sendSynchRemote")
        .removeHeaders("CamelHttp*")
        .setHeader(Exchange.HTTP_URI, method("shsRouter", "resolveEndpoint(${body.label})"))
        .setHeader(Exchange.CONTENT_TYPE, constant("message/rfc822"))
        .beanRef("messageLogService", "loadMessage")
        .to("http://shsServer")
        .bean(ReplyLabelProcessor.class)
        .beanRef("messageLogService", "saveMessage");

        from("direct:sendSynchLocal").routeId("direct:sendSynchLocal")
        .setHeader(ShsHeaders.DESTINATION_URI, method("shsRouter", "resolveEndpoint(${body.label})"))
        .setHeader(Exchange.CONTENT_TYPE, constant("message/rfc822"))
        .beanRef("messageLogService", "loadMessage")
        .to("shs:local")
        .bean(ReplyLabelProcessor.class)
        .beanRef("messageLogService", "saveMessage");
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
