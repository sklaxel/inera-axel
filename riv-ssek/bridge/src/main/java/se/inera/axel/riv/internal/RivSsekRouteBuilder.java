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
package se.inera.axel.riv.internal;


import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.SSLContextParametersSecureProtocolSocketFactory;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

/**
 * 
 * @author Ekkehart Lötzsch
 *
 */
public class RivSsekRouteBuilder extends RouteBuilder {
		
	private static final String SOAP_ACTION = "SOAPAction";
	private static final Object SOAP_ACTION_VALUE = "";

	private static final String CONTENT_TYPE_VALUE = "constant(\"application/soap+xml\")";

	private static final String HTTP_URI_SSEK = "http://localhost:8181/cxf/ssek/helloworld";

	@Override
	public void configure() throws Exception {
	
		configureSsl();

		// EKKLOT - This is just the basic code skeleton but it does not do anything
		// meaningful yet.
		from("jetty:{{riv2ssekInBridgeEndpoint}}?sslContextParametersRef=mySslContext").routeId("riv2ssek")
		.removeHeaders("CamelHttp*")
		.setHeader(SOAP_ACTION, constant(SOAP_ACTION_VALUE))
        .setHeader(Exchange.CONTENT_TYPE, header(CONTENT_TYPE_VALUE))
		.setHeader(Exchange.HTTP_URI, constant(HTTP_URI_SSEK))
		.beanRef("camelToSsekConverter")
		.to("http://ssekService")
		.beanRef("ssekToCamelConverter");

		// EKKLOT TODO - This is not routing anywhere yet.
		from("jetty:{{ssek2rivInBridgeEndpoint}}?sslContextParametersRef=mySslContext").routeId("ssek2riv")
		.to("log:ekklot_ssek2riv?showAll=true&level=INFO");
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
