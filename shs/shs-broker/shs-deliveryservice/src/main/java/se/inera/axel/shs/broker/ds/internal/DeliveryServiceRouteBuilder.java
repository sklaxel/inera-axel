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
package se.inera.axel.shs.broker.ds.internal;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.validation.PredicateValidationException;
import se.inera.axel.shs.broker.messagestore.MessageNotFoundException;

import java.net.HttpURLConnection;

public class DeliveryServiceRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        onException(IllegalArgumentException.class, PredicateValidationException.class)
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpURLConnection.HTTP_BAD_REQUEST))
        .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
        .transform(simple("${exception.message}"))
        .handled(true);

        onException(MessageNotFoundException.class)
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpURLConnection.HTTP_NOT_FOUND))
        .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
        .transform(simple("Message not found ${exception.message}\n"))
        .handled(true);

        from("timer://releaseFetchingInProgressTimer?delay=30000&period=60000")
        .beanRef("messageLogService", "releaseStaleFetchingInProgress()");
        
        from("{{shsDsHttpEndpoint}}" + HttpPathParamsExtractor.PATH_PREFIX +
                "?" +
                "httpBindingRef=shsHttpBinding" +
                "&matchOnUriPrefix=true")
        .routeId(HttpPathParamsExtractor.PATH_PREFIX)
        .bean(new HttpPathParamsExtractor())
        .validate(header("outbox").isNotNull())
        .choice()
        .when(header(Exchange.HTTP_METHOD).isEqualTo("POST"))
                .to("direct:post")
        .otherwise()
                .to("direct:get");


        from("direct:get").routeId("direct:get")
        .choice()
        .when(header("txId").isNotNull())
                .to("direct:fetchMessage")
        .otherwise()
                .to("direct:listMessages")
        .end();


        from("direct:fetchMessage").routeId("direct:fetchMessage")
        // the message is locked here and then committed or rollbacked in ShsHttpBinding on the jetty endpoint.
        .beanRef("messageLogService", "loadEntryAndLockForFetching(${header.outbox}, ${header.txId})")
        .setProperty("entry", body())  // must be set on 'entry'-property so ShsHttpBinding can use it
        .beanRef("messageLogService", "loadMessage(${property.entry})")
        .setHeader(Exchange.CONTENT_TYPE, constant("message/rfc822"));


        from("direct:listMessages").routeId("direct:listMessages")
        .bean(new HeaderToFilterConverter())
        .beanRef("messageLogService", "listMessages(${header.outbox}, ${body})")
        .bean(new MessageListConverter())
        .setHeader(Exchange.CONTENT_TYPE, constant("application/xml"))
        .setProperty(Exchange.CHARSET_NAME, constant("iso-8859-1"))
        .convertBodyTo(String.class);


        from("direct:post").routeId("direct:post")
        .choice()
        .when(header("action").isEqualTo("ack"))
               .to("direct:acknowledgeMessage");


        from("direct:acknowledgeMessage").routeId("direct:acknowledgeMessage")
        .beanRef("messageLogService", "loadEntry(${header.outbox}, ${header.txId})")
        .beanRef("messageLogService", "messageAcknowledged(${body})")
        .choice()
        .when().simple("${body.label.sequenceType} != 'ADM'")
            .bean(ConfirmMessageBuilder.class)
            .to("direct-vm:shs:rs")
        .end()
        .setBody(constant(""));

    }


}
