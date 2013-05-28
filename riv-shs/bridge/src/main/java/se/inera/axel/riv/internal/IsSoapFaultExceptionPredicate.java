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
package se.inera.axel.riv.internal;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.builder.xml.XPathBuilder;
import org.apache.camel.component.http.HttpOperationFailedException;

public final class IsSoapFaultExceptionPredicate implements Predicate {

	public boolean isSoapFault(HttpOperationFailedException e, Exchange exchange) {
		return XPathBuilder
			.xpath("/soap:Envelope/soap:Body/soap:Fault")
			.namespace("soap", "http://schemas.xmlsoap.org/soap/envelope/")
			.matches(exchange.getContext(), e.getResponseBody());
	}

	@Override
	public boolean matches(Exchange exchange) {
		HttpOperationFailedException e = getCaughtException(exchange);
		
		if (e == null) {
			return false;
		}
		
		return isSoapFault(e, exchange);
	}

	protected HttpOperationFailedException getCaughtException(Exchange exchange) {
		HttpOperationFailedException e = exchange.getException(HttpOperationFailedException.class);
		
		if (e == null) {
			e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
		}
		return e;
	}
}