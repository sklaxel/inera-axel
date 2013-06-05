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
import se.inera.axel.shs.protocol.ShsHeaders;
import se.inera.axel.shs.protocol.ShsMessage;

/**
 * Defines pipeline for processing and routing SHS synchronous messages
 */
public class SynchronBrokerRouteBuilder extends RouteBuilder {

	
	@Override
	public void configure() throws Exception {

		from("direct-vm:shs:synchronBroker").routeId("direct-vm:shs:synchronBroker")
		.setProperty(RecipientLabelTransformer.PROPERTY_SHS_RECEIVER_LIST, method("shsRouter", "resolveRecipients(${body.label})"))
		.bean(RecipientLabelTransformer.class, "transform(${body.label},*)")
		.beanRef("agreementService", "validateAgreement(${body.label})")
		.choice()
			.when().method("shsRouter", "isLocal(${body.label})")
				.to("direct:sendSynchLocal")
			.otherwise()
				.to("direct:sendSynchRemote")
		.end();

		from("direct:sendSynchRemote").routeId("direct:sendSynchRemote")
		.removeHeaders("CamelHttp*")
		.setHeader(Exchange.HTTP_URI, method("shsRouter", "resolveEndpoint(${body.label})"))
        .beanRef("messageLogService", "fetchMessage")
		.to("http://shsServer")
        .beanRef("messageLogService", "createEntry");

		from("direct:sendSynchLocal").routeId("direct:sendSynchLocal")
		.setHeader(ShsHeaders.DESTINATION_URI, method("shsRouter", "resolveEndpoint(${body.label})"))
        .beanRef("messageLogService", "fetchMessage")
		.to("shs:local")
        .beanRef("messageLogService", "createEntry");
	}
}
