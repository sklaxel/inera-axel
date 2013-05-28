/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.broker.internal;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.protocol.ShsHeaders;
import se.inera.axel.shs.protocol.ShsMessage;

/**
 * Defines pipeline for processing and routing SHS messages
 */
public class ServerRouteBuilder extends RouteBuilder {


	@Override
	public void configure() throws Exception {

		// Handle MimeMessage
		from("jetty:{{shsRsHttpEndpoint}}:{{shsRsHttpEndpoint.port}}/shs/rs?sslContextParametersRef=mySslContext").routeId("jetty:/shs/rs")
		.onException(ShsException.class)
				.setHeader("X-shs-errorcode", simple("${exception.errorCode}"))
				.transform(simple("${exception.message}"))
				.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
				.handled(true)
		.end()
		.onException(Exception.class)
				.transform(simple("${exception.message}"))
				.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
				.handled(true)
		.end()
		.convertBodyTo(ShsMessage.class)
		.setProperty(ShsHeaders.LABEL, simple("${body.label}"))
		.to("shs:direct-vm:shs:rs")
		.choice().when().simple("${property.ShsLabel.transferType} == 'SYNCH'")
			.convertBodyTo(ShsMessage.class)
		.otherwise()
			.setHeader("corr.id", simple("${property.ShsLabel.corrId}")); // TODO add more return headers



		// Handle ShsMessage object
		from("direct-vm:shs:rs").routeId("direct-vm:shs:rs")
        .beanRef("messageLogService")
		.transform(method("labelHistoryTransformer"))
		.transform(method("fromValueTransformer"))
		.transform(method("toValueTransformer"))
		.choice().when().simple("${body.label.transferType} == 'SYNCH'")
			.to("direct-vm:shs:synchronBroker")
		.otherwise()
			.throwException(new RuntimeException("Asynch messaging not yet supported"))
		.end();
	}
	
}
