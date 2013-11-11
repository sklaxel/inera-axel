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
package se.inera.axel.riv_ssek.internal;


import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.component.http.SSLContextParametersSecureProtocolSocketFactory;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

/**
 * Defines Camel routes for RIV <---> SSEK
 */
public class RivSsekRouteBuilder extends RouteBuilder {
		
	private static final String SOAP_ACTION = "SOAPAction";

	private static final String RECEIVER_ID = "ReceiverId";
	private static final String SENDER_ID = "SenderId";
	private static final String TX_ID = "TxId";

	private static final Namespaces addressing = new Namespaces("add", "http://www.w3.org/2005/08/addressing");

	@Override
	public void configure() throws Exception {
	
		configureSsl();

		// RIV-TO-SSEK Bridge
		from("jetty:{{riv2ssekInBridgeEndpoint}}?sslContextParametersRef=mySslContext").routeId("riv2ssek")
		.onException(Exception.class)
			.handled(true)
			.bean(HttpResponseStatusExceptionResolver.class)
			.end()
		.setHeader(SENDER_ID, simple("{{ssekSenderId}}"))
		.setHeader(RECEIVER_ID).xpath("//add:To", String.class, addressing)
		.setHeader(TX_ID, constant(""))
		.to("xquery:riv2ssek.xquery")
		.removeHeaders("*")
		.setHeader(SOAP_ACTION, constant(""))
        .setHeader(Exchange.CONTENT_TYPE, constant("application/xml"))
		.setHeader(Exchange.HTTP_URI, constant("{{ssekEndpoint}}"))
		.to("http://ssekService");
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
}
