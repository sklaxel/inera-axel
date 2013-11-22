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
package se.inera.axel.riv2ssek.internal;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.component.http.SSLContextParametersSecureProtocolSocketFactory;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import se.inera.axel.shs.processor.ShsHeaders;

/**
 * Defines Camel routes for RIV <---> SSEK
 */
public class RivSsekRouteBuilder extends RouteBuilder {
		
	private static final String SOAP_ACTION = "SOAPAction";

	private static final String SSEK_RECEIVER_ID = "receiverId";
	private static final String SSEK_SENDER_ID = "senderId";
	private static final String SSEK_TX_ID = "txId";
	private static final String SSEK_PAYLOAD = "payload";

	private static final Namespaces addressing = new Namespaces("add", "http://www.w3.org/2005/08/addressing");
	private static final Namespaces urnNamespace = new Namespaces("urn", "urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3");

	public static final String RIV_SENDER_ID = "x-rivta-original-serviceconsumer-hsaid";
	public static final String RIV_CORR_ID = "x-vp-correlation-id";

	@Override
	public void configure() throws Exception {

		configureSsl();

		// RIV-TO-SSEK Bridge
		from("jetty:{{riv2ssekEndpoint.server}}:{{riv2ssekEndpoint.port}}/{{riv2ssekEndpoint.path}}?sslContextParametersRef=mySslContext").routeId("riv2ssek")
		.onException(Exception.class)
			.handled(true)
			.bean(HttpResponseStatusExceptionResolver.class)
		.end()

		// SENDER_ID
		.choice()
			.when(header(RIV_SENDER_ID).isNull())
				.setHeader(SSEK_SENDER_ID, simple("{{ssekDefaultSenderId}}"))
			.otherwise()
				.setHeader(SSEK_SENDER_ID, header(RIV_SENDER_ID))
		.end()

		// RECEIVER_ID
		.setHeader(SSEK_RECEIVER_ID).xpath("//add:To", String.class, addressing)
		.choice().when(header(SSEK_RECEIVER_ID).isEqualTo(""))
				.setHeader(SSEK_RECEIVER_ID, simple("{{ssekDefaultReceiverId}}"))
		.end()
		
		// TX_ID
		.choice()
			.when(header(RIV_CORR_ID).isNull())
				.setHeader(SSEK_TX_ID, simple(UUID.randomUUID().toString()))
			.otherwise()
				.setHeader(SSEK_TX_ID, header(RIV_CORR_ID))
		.end()
		
		.setHeader(SSEK_PAYLOAD).xpath("//urn:RegisterMedicalCertificate", String.class, urnNamespace)
		.to("xquery:riv2ssek.xquery")
		.removeHeaders("*")
		.setHeader(SOAP_ACTION, constant(""))
        .setHeader(Exchange.CONTENT_TYPE, constant("application/xml"))
		.setHeader(Exchange.HTTP_URI, constant("{{ssekEndpoint.server}}:{{ssekEndpoint.port}}/{{ssekEndpoint.path}}"))
		.to("http://ssekService");
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
}
