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
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpOperationFailedException;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.exception.MissingDeliveryExecutionException;
import se.inera.axel.shs.exception.OtherErrorException;
import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ResponseMessageBuilder;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.io.IOException;

/**
 * Defines pipeline for processing and routing SHS asynchronous messages.
 */
public class AsynchBrokerRouteBuilder extends RouteBuilder {


    @Override
    public void configure() throws Exception {

        errorHandler(deadLetterChannel("direct:errors").useOriginalMessage());


        from("direct-vm:shs:asynch").routeId("direct-vm:shs:asynch")
        .errorHandler(defaultErrorHandler())
        .setHeader(ShsHeaders.X_SHS_CORRID, simple("${body.label.corrId}"))
        .setHeader(ShsHeaders.X_SHS_CONTENTID, simple("${body.label.content.contentId}"))
        .setHeader(ShsHeaders.X_SHS_NODEID, constant("nodeid")) // TODO set node id
        .setHeader(ShsHeaders.X_SHS_LOCALID, simple("${body.id}"))
        .setHeader(ShsHeaders.X_SHS_TXID, simple("${body.label.txId}"))
        .setHeader(ShsHeaders.X_SHS_ARRIVALDATE, simple("${body.label.datetime}")) // TODO not correct timestamp
        .setHeader(ShsHeaders.X_SHS_DUPLICATEMSG, constant("no")) // TODO handle duplicate messages
        .inOnly("activemq:queue:axel.shs.in")
        .setBody(simple("${body.label.txId}"));


        from("activemq:queue:axel.shs.in").routeId("activemq:queue:axel.shs.in")
        .setProperty(RecipientLabelTransformer.PROPERTY_SHS_RECEIVER_LIST,
                method("shsRouter", "resolveRecipients(${body.label})"))
        .bean(RecipientLabelTransformer.class, "transform(${body.label},*)")
        .beanRef("agreementService", "validateAgreement(${body.label})")
		.choice()
		.when().simple("${body.label.sequenceType} == 'ADM'")
			.setProperty("ShsMessageEntry", body())
			.beanRef("messageLogService", "loadMessage")
			.choice()
			.when().simple("${body.label.product.value} == 'error'")
		        .beanRef("messageLogService", "quarantineCorrelatedMessages")
			.when().simple("${body.label.product.value} == 'confirm'")
		        .beanRef("messageLogService", "acknowledgeCorrelatedMessages")
			.end()
	        .setBody(property("ShsMessageEntry"))
        .end()
        .choice()
        .when().method("shsRouter", "isLocal(${body.label})")
            .to("direct:sendAsynchLocal")
        .otherwise()
            .to("direct:sendAsynchRemote")
        .end();


        from("direct:sendAsynchRemote").routeId("direct:sendAsynchRemote")
        .onException(IOException.class)
                .useExponentialBackOff()
                .maximumRedeliveries(5)
                .logExhausted(true)
                .end()
        .removeHeaders("CamelHttp*")
        .setHeader(Exchange.HTTP_URI, method("shsRouter", "resolveEndpoint(${body.label})"))
        .setProperty("ShsMessageEntry", body())
        .beanRef("messageLogService", "loadMessage")
        .to("http://shsServer") // TODO handle response headers and error codes etc.
        .setBody(property("ShsMessageEntry"))
        .beanRef("messageLogService", "messageSent");


        from("direct:sendAsynchLocal").routeId("direct:sendAsynchLocal")
        .beanRef("messageLogService", "messageReceived");


        from("direct:errors")
        .errorHandler(loggingErrorHandler())
        .log("ERROR: ${exception} for ${body.label}")
        .bean(ExceptionConverter.class)
        .beanRef("messageLogService", "messageQuarantined")
        .bean(ErrorMessageBuilder.class)
        .to("direct-vm:shs:rs");
    }


    public static class ErrorMessageBuilder {
        ResponseMessageBuilder builder = new ResponseMessageBuilder();

        public ShsMessage buildErrorMessage(ShsMessageEntry entry, Exception exception) {

            ShsLabel requestLabel = entry.getLabel();
            return builder.buildErrorMessage(requestLabel, exception);
        }
    }


    public static class ExceptionConverter implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {

            ShsException shsException = exchange.getException(ShsException.class);

            if (shsException != null) {
                return;
            }

            IOException ioException = exchange.getException(IOException.class);

            if (ioException != null) {
                shsException = new MissingDeliveryExecutionException(ioException);
            }

            if (shsException == null) {
                HttpOperationFailedException httpOperationFailedException =
                    exchange.getException(HttpOperationFailedException.class);

                if (httpOperationFailedException != null) {
                    shsException = new MissingDeliveryExecutionException(httpOperationFailedException);
                }
            }

            if (shsException == null) {
                Exception exception = exchange.getException(Exception.class);

                if (exception != null) {
                    shsException = new OtherErrorException(exception);
                }
            }

            if (shsException != null)
                exchange.setException(shsException);
        }
    }
}
