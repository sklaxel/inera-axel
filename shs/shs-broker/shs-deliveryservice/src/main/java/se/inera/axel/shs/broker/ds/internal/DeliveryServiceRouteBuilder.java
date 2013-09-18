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
        .transform(simple("${exception.message}"))
        .handled(true);

        onException(MessageNotFoundException.class)
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpURLConnection.HTTP_NOT_FOUND))
        .transform(simple("Message not found ${exception.message}\n"))
        .handled(true);

        from("timer://releaseFetchingInProgressTimer?delay=30000&period=60000")
        .beanRef("messageLogService", "releaseStaleFetchingInProgress()");
        
        from("jetty:{{shsDsHttpEndpoint}}:{{shsDsHttpEndpoint.port}}" + HttpPathParamsExtractor.PATH_PREFIX +
                "?sslContextParametersRef=mySslContext" +
                "&enableJmx=true" +
                "&httpBindingRef=shsHttpBinding" +
                "&matchOnUriPrefix=true")
        .routeId("jetty:" + HttpPathParamsExtractor.PATH_PREFIX)
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
        .beanRef("messageLogService", "loadEntryAndLockForFetching(${header.outbox}, ${header.txId})")
        .setProperty("entry", body())
        .beanRef("messageLogService", "loadMessage(${property.entry})");


        from("direct:listMessages").routeId("direct:listMessages")
        .bean(new HeaderToFilterConverter())
        .beanRef("messageLogService", "listMessages(${header.outbox}, ${body})")
        .bean(new MessageListConverter())
        .convertBodyTo(String.class);


        from("direct:post").routeId("direct:post")
        .choice()
        .when(header("action").isEqualTo("ack"))
               .to("direct:acknowledgeMessage");


        from("direct:acknowledgeMessage").routeId("direct:acknowledgeMessage")
        .beanRef("messageLogService", "loadEntry(${header.outbox}, ${header.txId})")
        .beanRef("messageLogService", "messageAcknowledged(${body})")
        .bean(ConfirmMessageBuilder.class)
        .to("direct-vm:shs:rs")
        .setBody(constant(""));

    }


}
