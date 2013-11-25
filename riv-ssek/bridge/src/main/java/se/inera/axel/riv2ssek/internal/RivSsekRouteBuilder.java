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

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.SSLContextParametersSecureProtocolSocketFactory;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

/**
 * Defines Camel routes for RIV <---> SSEK
 */
public class RivSsekRouteBuilder extends RouteBuilder {
		
	@Override
	public void configure() throws Exception {

		configureSsl();

		// RIV-TO-SSEK Bridge
		from("jetty:{{riv2ssekEndpoint.server}}:{{riv2ssekEndpoint.port}}/{{riv2ssekEndpoint.path}}?sslContextParametersRef=mySslContext").routeId("riv2ssek")
		.onException(Exception.class)
			.handled(true)
			.bean(HttpResponseStatusExceptionResolver.class)
		.end()
		.beanRef("rivToCamelProcessor")
		.to("xquery:camel2ssek.xquery")
		.removeHeaders("*")
		.setHeader("SOAPAction", constant(""))
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
