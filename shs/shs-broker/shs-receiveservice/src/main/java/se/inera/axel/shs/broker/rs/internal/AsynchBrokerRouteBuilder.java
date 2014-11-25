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
import se.inera.axel.shs.xml.label.ShsLabel;

import java.io.IOException;
import java.util.Map;

import static org.apache.camel.builder.PredicateBuilder.startsWith;

/**
 * Defines pipeline for processing and routing SHS asynchronous messages.
 */
public class AsynchBrokerRouteBuilder extends RouteBuilder {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AsynchBrokerRouteBuilder.class);


    @Override
    public void configure() throws Exception {

        errorHandler(defaultErrorHandler());

        from("direct-vm:shs:asynch").routeId("direct-vm:shs:asynch")
        .errorHandler(defaultErrorHandler())
        .inOnly("activemq:queue:axel.shs.in");

        from("activemq:queue:axel.shs.in").routeId("activemq:queue:axel.shs.in")
        .errorHandler(deadLetterChannel("direct:errors").useOriginalMessage())
        .to("direct:asynch_process");

        from("direct-vm:shs:asynch_process").routeId("direct-vm:shs:asynch_process")
        .onException(Exception.class).useOriginalMessage().handled(false).to("direct:errors").end()
        .to("direct:asynch_process");

        from("direct:asynch_process").routeId("direct:asynch_process")
        .errorHandler(noErrorHandler())
        .setProperty(RecipientLabelTransformer.PROPERTY_SHS_RECEIVER_LIST,
                method("shsRouter", "resolveRecipients(${body.label})"))
        .choice()
        .when(simple("${property.PROPERTY_SHS_RECEIVER_LIST.size} > 1"))
        	.to("direct:shs:asynch:one_to_many")
	        .beanRef("messageLogService", "messageOneToMany")
        	.stop()
        .end()
        .bean(RecipientLabelTransformer.class, "transform(${body.label},*)")
        .beanRef("commonNameTransformer")
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

        from("direct:shs:asynch:one_to_many").routeId("direct:shs:asynch:one_to_many")
        .errorHandler(noErrorHandler())
       	.split().method("recipientSplitter", "split")
        .to("direct-vm:shs:rs");

        from("direct:sendAsynchRemote").routeId("direct:sendAsynchRemote")
        .errorHandler(noErrorHandler())
        .onException(IOException.class)
                .useExponentialBackOff()
                .maximumRedeliveries(5)
                .logExhausted(true)
                .useOriginalMessage()
                .end()
        .removeHeaders("CamelHttp*")
        .setHeader(Exchange.HTTP_URI, method("shsRouter", "resolveEndpoint(${body.label})"))
        .setHeader(Exchange.CONTENT_TYPE, constant("message/rfc822"))
        .setProperty("ShsMessageEntry", body())
        .beanRef("messageLogService", "loadMessage")
        .choice().when(startsWith(header(Exchange.HTTP_URI), constant("https")))
            .to("https4://shsServer?httpClient.soTimeout=300000&disableStreamCache=true&sslContextParameters=shsRsSslContext&x509HostnameVerifier=allowAllHostnameVerifier")
        .otherwise()
            .to("http4://shsServer?httpClient.soTimeout=300000&disableStreamCache=true")
        .end()
        .setBody(property("ShsMessageEntry"))
        .beanRef("messageLogService", "messageSent");


        from("direct:sendAsynchLocal").routeId("direct:sendAsynchLocal")
        .errorHandler(noErrorHandler())
        .beanRef("messageLogService", "messageReceived");


        from("direct:errors").routeId("direct:errors")
        .errorHandler(loggingErrorHandler())
        .log("ERROR: ${exception} for ${body.label}")
        .bean(ExceptionConverter.class)
        .beanRef("messageLogService", "messageQuarantined")
        .filter(simple("${body.label.sequenceType} != 'ADM' && ${header.AxelRobustAsynchShs} == null"))
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

            Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
            if (exception instanceof ShsException) {
                return;
            } else if (exception instanceof IOException) {
                exception = new MissingDeliveryExecutionException(exception);
            } else if (exception instanceof HttpOperationFailedException) {
                HttpOperationFailedException httpOperationFailedException = (HttpOperationFailedException)exception;
                exception = createMissingDeliveryExecutionException(
                        exception,
                        httpOperationFailedException.getResponseHeaders(),
                        httpOperationFailedException.getResponseBody());
            } else if (exception instanceof org.apache.camel.component.http4.HttpOperationFailedException) {
                org.apache.camel.component.http4.HttpOperationFailedException httpOperationFailedException = (org.apache.camel.component.http4.HttpOperationFailedException)exception;
                exception = createMissingDeliveryExecutionException(
                        exception,
                        httpOperationFailedException.getResponseHeaders(),
                        httpOperationFailedException.getResponseBody());
            } else {
                exception = new OtherErrorException(exception);
            }

            if (exception != null)
                exchange.setProperty(Exchange.EXCEPTION_CAUGHT, exception);
        }

        private Exception createMissingDeliveryExecutionException(
                Exception exception,
                Map<String, String> responseHeaders,
                String errorInfo) {

            LOG.debug("Failed Http response headers: {}", responseHeaders);

            exception = new MissingDeliveryExecutionException(
                    String.format(
                            "Delivery of message failed. Response headers: %s Response body: %s",
                            responseHeaders.toString(),
                        errorInfo),
                    exception);
            return exception;
        }
    }
}
