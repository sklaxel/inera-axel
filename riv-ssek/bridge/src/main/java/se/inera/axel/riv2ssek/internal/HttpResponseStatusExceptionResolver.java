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

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.camel.Exchange;
import org.apache.camel.component.http.HttpOperationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves Http exception
 */
public class HttpResponseStatusExceptionResolver {
	
	private final static Logger log = LoggerFactory
			.getLogger(HttpResponseStatusExceptionResolver.class);
	
	private MessageFactory messageFactory;
	
	public HttpResponseStatusExceptionResolver() {
		try {
			messageFactory = MessageFactory.newInstance();
		} catch (SOAPException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Resolves exception and create SOAPMessage with fault information.
	 * 
	 * @param e
	 * @param exchange
	 */
	public void resolveException(Exception e, Exchange exchange) {
					
		if (e instanceof HttpOperationFailedException) {
			// Forward the received HTTP error code including the received error contents
			HttpOperationFailedException e2 = (HttpOperationFailedException) e;
			exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, e2.getStatusCode());
			exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "text/xml");
			exchange.getIn().setBody(e2.getResponseBody());
		} else {
			// Set a new HTTP error code
			exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "text/xml");
			exchange.getIn().setBody(createSoapFault(e));
		}
	}

	/**
	 * Creates SOAPFault.
	 * 
	 * @param exception
	 * @return
	 */
	private Object createSoapFault(Exception exception) {
		SOAPPart soapPart = null;
		try {
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPFault soapFault = soapMessage.getSOAPBody().addFault();
			soapFault.setFaultCode(new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Server"));
			soapFault.setFaultString(exception.getMessage());

			soapPart = soapMessage.getSOAPPart();
		} catch (SOAPException e1) {
			log.error("Could not create SOAPFault message", e1);
			String fault =
					"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
					"<soap:Body>" +
					"<soap:Fault>" +
					"<faultcode>soap:Server</faultcode>" +
					"<faultstring>Internal server error</faultstring>" +
					"</soap:Fault>" +
					"</soap:Body>" +
					"</soap:Envelope>";
			return fault;
		}

		return soapPart;
	}
}
