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
package se.inera.axel.shs.cmdline;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpOperationFailedException;
import org.apache.camel.component.http.SSLContextParametersSecureProtocolSocketFactory;
import org.apache.camel.util.URISupport;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import se.inera.axel.shs.camel.DataPartToCamelMessageProcessor;
import se.inera.axel.shs.camel.DefaultCamelToShsMessageProcessor;
import se.inera.axel.shs.camel.DefaultShsMessageToCamelProcessor;
import se.inera.axel.shs.camel.ShsMessageToDataParListProcessor;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.processor.SimpleLabelValidator;
import se.inera.axel.shs.xml.message.ShsMessageList;

import java.net.URISyntaxException;
import java.util.Map;

public class ShsCmdlineRouteBuilder extends RouteBuilder {
	
	@Override
	public void configure() throws Exception {
		configureSsl();

		onException(Exception.class)
		.handled(false)
		.log("${exception}");

		onException(HttpOperationFailedException.class)
		.handled(false)
		.log(LoggingLevel.ERROR, "Error message: ${exception.responseBody}")
		.log(LoggingLevel.ERROR, "Error code: ${exception.responseHeaders[X-shs-errorcode]}");


		from("direct:shsSendAsync")
		.bean(new DefaultCamelToShsMessageProcessor())
		.log("validating label...")
		.bean(SimpleLabelValidator.class)
		.log("sending message...")
		.to("{{shsServerUrl}}");


		from("direct:shsSendSync")
		.bean(new DefaultCamelToShsMessageProcessor())
		.log("validating label...")
		.bean(SimpleLabelValidator.class)
		.log("sending message...")
		.to("{{shsServerUrl}}")
		.bean(new DefaultShsMessageToCamelProcessor());

        from("direct:listMessages").routeId("listMessages")
                .to("direct:ds:list")
                .to("stream:out");

        from("direct:ds:list").routeId("ds:list")
                .setHeader(Exchange.HTTP_PATH, simple("${header.toUrn}"))
                .setHeader(Exchange.HTTP_QUERY, method(ParamsToQueryString.class))
                .to("{{shsServerUrlDs}}");

        from("direct:ds:fetch").routeId("ds:fetch")
                .setHeader(Exchange.HTTP_PATH, simple("${header.toUrn}/${header.ShsLabelTxId}"))
                .removeHeader(Exchange.HTTP_QUERY)
                .setBody(constant(null))
                .to("{{shsServerUrlDs}}");

        from("direct:ds:ack").routeId("ds:ack")
                .setHeader(Exchange.HTTP_PATH, simple("${header.toUrn}/${header.ShsLabelTxId}"))
                .removeHeader(Exchange.HTTP_QUERY)
                .setBody(constant(null))
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.HTTP_QUERY, constant("action=ack"))
                .to("{{shsServerUrlDs}}");

        from("direct:fetchAll").routeId("fetchAll")
                .to("direct:ds:list")
                .convertBodyTo(ShsMessageList.class)
                .split(simple("${body.message}"))
                .setHeader(ShsHeaders.TXID, simple("${body.txId}"))
                .to("direct:fetch");

        from("direct:fetch").routeId("fetch")
                .to("direct:ds:fetch")
                .bean(new ShsMessageToDataParListProcessor())
                .to("direct:writeShsLabel")
                .split(body())
                    .bean(new DataPartToCamelMessageProcessor())
                    .choice()
                        .when(header(ShsCmdlineHeaders.USE_ORIGINAL_FILENAMES).isNotNull())
                            .setHeader(Exchange.FILE_NAME, header(ShsHeaders.DATAPART_FILENAME))
                        .otherwise()
                            .setHeader(Exchange.FILE_NAME, simple("${header.ShsLabelTxId}-${header.CamelSplitIndex}"))
                    .end()
                    .to("file://{{outputDir}}")
                .end()
                .to("direct:ds:ack");

        from("direct:writeShsLabel")
                .setProperty("originalBody", body())
                .setBody().property(ShsHeaders.LABEL)
                .convertBodyTo(String.class)
                .to("file://{{outputDir}}?charset=iso-8859-1&fileName=${header.ShsLabelTxId}-label")
                .log("Wrote label to ${header.CamelFileNameProduced}")
                .setBody().property("originalBody");

	}
	
	private void configureSsl() {
		SSLContextParameters sslContextParameters = getContext().getRegistry().lookup("mySslContext", SSLContextParameters.class);
		
		ProtocolSocketFactory factory =
			    new SSLContextParametersSecureProtocolSocketFactory(sslContextParameters);

		Protocol.registerProtocol("https",
				new Protocol(
						"https",
						factory,
						443));
	}

    public static class ParamsToQueryString {
        public String toQueryString(@Header(ShsCmdlineHeaders.QUERY_PARAMS) Map<String, Object> queryParams) {
            try {
                if (queryParams == null || queryParams.size() == 0) {
                    return "";
                }
                return URISupport.createQueryString(queryParams);
            } catch (URISyntaxException e) {
               throw new RuntimeException(e);
            }
        }
    }
}
