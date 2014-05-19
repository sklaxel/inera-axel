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
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.builder.xml.XPathBuilder;
import org.springframework.beans.factory.annotation.Value;

/**
 * Converts RIV to Camel headers
 */
public class RivToCamelProcessor implements Processor {
	
	@Value("${ssekDefaultSender}")
	private String ssekDefaultSender;

	@Value("${ssekDefaultReceiver}")
	private String ssekDefaultReceiver;

	public static final String RIV_SENDER = "x-rivta-original-serviceconsumer-hsaid";
	public static final String RIV_CORR_ID = "x-vp-correlation-id";

	private static final Namespaces ns = new Namespaces("add", "http://www.w3.org/2005/08/addressing")
			.add("soap", "http://schemas.xmlsoap.org/soap/envelope/")
			.add("urn", "urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3");

	/**
	 * Sets Camel headers from RIV HTTP parameters as well as from RIV XML content.
	 */
	@Override
	public void process(Exchange exchange) {
		
		Message in = exchange.getIn();
	
		// SSEK_SENDER_ID
		String ssekSender = in.getHeader(RIV_SENDER, ssekDefaultSender, String.class);
		in.setHeader("sender", ssekSender);
		
		// SSEK_RECEIVER_ID
		String rivReceiver = XPathBuilder.xpath("/soap:Envelope/soap:Header/add:To/text()").namespaces(ns).evaluate(exchange.getContext(), exchange.getIn().getBody());

		if ("".equals(rivReceiver)) {
			in.setHeader("receiver", ssekDefaultReceiver);
		} else {
			in.setHeader("receiver", rivReceiver);
		}

		// SSEK_TX_ID
		String ssekTxId = in.getHeader(RIV_CORR_ID, UUID.randomUUID().toString(), String.class);
		in.setHeader("txId", ssekTxId);

		// SSEK_PAYLOAD
		String ssekPayload = XPathBuilder.xpath("/soap:Envelope/soap:Body/urn:RegisterMedicalCertificate/text()").namespaces(ns).evaluate(exchange.getContext(), exchange.getIn().getBody());
		in.setHeader("payload", ssekPayload);
	}
}
