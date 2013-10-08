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
import se.inera.axel.shs.broker.messagestore.MessageAlreadyExistsException;
import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.processor.TimestampConverter;

import java.net.HttpURLConnection;

/**
 * Defines pipeline for processing and routing SHS messages
 */
public class ReceiveServiceRouteBuilder extends RouteBuilder {


    @Override
    public void configure() throws Exception {

        // Handle MimeMessage
        from("jetty:{{shsRsHttpEndpoint}}:{{shsRsHttpEndpoint.port}}/shs/rs?sslContextParametersRef=mySslContext&disableStreamCache={{shsJettyDisableStreamCache}}&enableJmx=true")
        .routeId("jetty:/shs/rs")
        .onException(ShsException.class)
            .setHeader(ShsHeaders.X_SHS_ERRORCODE, simple("${exception.errorCode}"))
            .transform(simple("${exception.message}"))
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpURLConnection.HTTP_BAD_REQUEST))
            .handled(true)
        .end()
        .onException(Exception.class)
            .transform(simple("${exception.message}"))
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpURLConnection.HTTP_INTERNAL_ERROR))
            .handled(true)
        .end()
        .inOnly("{{wireTapEndpoint}}")
        .filter(header(Exchange.HTTP_METHOD).isEqualTo("POST"))
        .convertBodyTo(ShsMessage.class)
        .setProperty(ShsHeaders.LABEL, simple("${body.label}"))
        .to("shs:direct-vm:shs:rs")
        .choice().when().simple("${property.ShsLabel.transferType} == 'SYNCH'")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpURLConnection.HTTP_OK))
            .convertBodyTo(ShsMessage.class)
        .otherwise()
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpURLConnection.HTTP_ACCEPTED))
        .end();


        // Handle ShsMessage object
        from("direct-vm:shs:rs").routeId("direct-vm:shs:rs")
        .onException(MessageAlreadyExistsException.class)
                .onWhen(simple("${body.label.transferType} == 'ASYNCH'"))
            .setHeader(ShsHeaders.X_SHS_CORRID, simple("${body.label.corrId}"))
            .setHeader(ShsHeaders.X_SHS_CONTENTID, simple("${body.label.content.contentId}"))
            .setHeader(ShsHeaders.X_SHS_NODEID, simple("${properties:nodeId}"))
            .setHeader(ShsHeaders.X_SHS_TXID, simple("${body.label.txId}"))
            .setHeader(ShsHeaders.X_SHS_ARRIVALDATE, simple("${exception.previousMessageTimestamp}"))
            .setHeader(ShsHeaders.X_SHS_ARRIVALDATE,
                    simple("${date:header." + ShsHeaders.X_SHS_ARRIVALDATE
                            + ":" + TimestampConverter.DATETIME_FORMAT + "}"))
            .setHeader(ShsHeaders.X_SHS_DUPLICATEMSG, constant("yes"))
            .transform(header(ShsHeaders.X_SHS_TXID))
            .handled(true)
        .end()
        .transform(method("labelHistoryTransformer"))
        .transform(method("fromValueTransformer"))
        .transform(method("toValueTransformer"))
        .beanRef("messageLogService", "saveMessage")
        .choice().when().simple("${body.label.transferType} == 'SYNCH'")
            .to("direct-vm:shs:synch")
            .beanRef("messageLogService", "loadMessage")
        .otherwise()
            .to("direct-vm:shs:asynch")
            .setHeader(ShsHeaders.X_SHS_CORRID, simple("${body.label.corrId}"))
            .setHeader(ShsHeaders.X_SHS_CONTENTID, simple("${body.label.content.contentId}"))
            .setHeader(ShsHeaders.X_SHS_NODEID, simple("${properties:nodeId}"))
            .setHeader(ShsHeaders.X_SHS_LOCALID, simple("${body.id}"))
            .setHeader(ShsHeaders.X_SHS_TXID, simple("${body.label.txId}"))
            .setHeader(ShsHeaders.X_SHS_ARRIVALDATE, simple("${body.stateTimeStamp}"))
            .setHeader(ShsHeaders.X_SHS_ARRIVALDATE,
                    simple("${date:header." + ShsHeaders.X_SHS_ARRIVALDATE
                            + ":" + TimestampConverter.DATETIME_FORMAT  + "}"))
            .setHeader(ShsHeaders.X_SHS_DUPLICATEMSG, constant("no"))
            .transform(simple("${body.label.txId}"))
        .end();
    }

}
