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
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import se.inera.axel.shs.messagestore.MessageState;
import se.inera.axel.shs.messagestore.ShsMessageEntry;
import se.inera.axel.shs.protocol.ShsHeaders;
import se.inera.axel.shs.protocol.ShsMessage;

import java.util.Date;

/**
 * Defines pipeline for processing and routing SHS asynchronous messages.
 */
public class AsynchronBrokerRouteBuilder extends RouteBuilder {

	
	@Override
	public void configure() throws Exception {

        from("direct-vm:shs:asynchronBroker").routeId("direct-vm:shs:asynchronBroker")
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
			.when().method("shsRouter", "isLocal(${body.label})")
				.to("direct:sendAsynchLocal")
			.otherwise()
				.to("direct:sendAsynchRemote")
		.end();

		from("direct:sendAsynchRemote").routeId("direct:sendAsynchRemote")
		.removeHeaders("CamelHttp*")
		.setHeader(Exchange.HTTP_URI, method("shsRouter", "resolveEndpoint(${body.label})"))
        .setProperty("ShsMessageEntry", body())
        .beanRef("messageLogService", "fetchMessage")
		.to("http://shsServer") // TODO handle response headers and error codes etc.
		.setBody(property("ShsMessageEntry"))
        .beanRef("messageLogService", "messageSent");

		from("direct:sendAsynchLocal").routeId("direct:sendAsynchLocal")
        .beanRef("messageLogService", "messageReceived");
	}
}
