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
