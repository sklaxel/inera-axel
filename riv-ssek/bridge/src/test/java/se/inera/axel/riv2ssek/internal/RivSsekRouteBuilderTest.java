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

import static org.apache.camel.builder.xml.XPathBuilder.xpath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.AvailablePortFinder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@ContextConfiguration
public class RivSsekRouteBuilderTest extends AbstractTestNGSpringContextTests {
	
	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory
			.getLogger(RivSsekRouteBuilderTest.class);
	
	private Namespaces nameSpaces;

	private MessageFactory messageFactory;

	private File riv_request;
	private File riv_request_no_addTo;
	Map<String, Object> riv_request_http_headers = new HashMap<>();

	private File ssek_response_ok;

	private static final String SOAP_ACTION = "SOAPAction";
	private static final String SOAP_ACTION_RIV_REGISTER_MEDICAL_CERTIFICATE = "urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:1";

	private static final String CONTENT_TYPE_VALUE = "application/xml";
	private static final String RIV_CORR_ID_VALUE = UUID.randomUUID().toString();
	private static final String RIV_SENDER_ID_VALUE = "Mina intyg";
	private static final String RIV_RECEIVER_ID_VALUE = "Skandia";
	private static final String RIV_PAYLOAD = "Test";

	@Produce(context = "riv-ssek-bridge-test")
	ProducerTemplate camel;

	@EndpointInject(uri = "mock:ssek")
	MockEndpoint ssekEndpoint;

	public RivSsekRouteBuilderTest() {
		super();

		// Needs to be put into constructor instead of beforeTest method because the camel context needs it.
		if (System.getProperty("riv2ssekEndpoint.port") == null) {
			int port = AvailablePortFinder.getNextAvailable();
			System.setProperty("riv2ssekEndpoint.port", Integer.toString(port));
		}

		if (System.getProperty("ssekEndpoint.port") == null) {
			int port = AvailablePortFinder.getNextAvailable();
			System.setProperty("ssekEndpoint.port", Integer.toString(port));
		}
	}

	@BeforeTest
	public void beforeTest() throws SOAPException {
		messageFactory = MessageFactory.newInstance();

		// Initialize RIV requests
		riv_request = new File(ClassLoader.getSystemResource("riv-request.xml").getFile());
		riv_request_no_addTo = new File(ClassLoader.getSystemResource("riv-request-no-addTo.xml").getFile());

		// Initialize SSEK response
		ssek_response_ok = new File(ClassLoader.getSystemResource("ssek-response-ok.xml").getFile());
		
		// Initialize name spaces
    	nameSpaces = new Namespaces("soap", "http://schemas.xmlsoap.org/soap/envelope/");
    	nameSpaces.add("ssek", "http://schemas.ssek.org/ssek/2006-05-10/");
    	nameSpaces.add("ns", "http://schemas.ssek.org/helloworld/2011-11-17");
	}

	@BeforeMethod
	public void beforeMethod() throws SOAPException {
		// Initialize RIV request HTTP headers
		riv_request_http_headers.put(SOAP_ACTION, SOAP_ACTION_RIV_REGISTER_MEDICAL_CERTIFICATE);
		riv_request_http_headers.put(Exchange.CONTENT_TYPE, CONTENT_TYPE_VALUE);
		riv_request_http_headers.put(RivSsekRouteBuilder.RIV_CORR_ID, RIV_CORR_ID_VALUE);
		riv_request_http_headers.put(RivSsekRouteBuilder.RIV_SENDER_ID, RIV_SENDER_ID_VALUE);
	}

	/**
	 * Injects RIV request into RIV endpoint. SSEK endpoint responds with
	 * OK/logical ERROR which is sent as HttpServletResponse.SC_OK.
	 * 
	 * @throws InterruptedException
	 */
	@DirtiesContext
	@Test
	public void testRiv2SsekOk() throws InterruptedException {
        ssekEndpoint.expectedMessageCount(1);
		
		// Check correct values in SSEK request
		ssekEndpoint.expectedMessagesMatches(xpath("/soap:Envelope").namespace("soap","http://schemas.xmlsoap.org/soap/envelope/"));
		ssekEndpoint.expectedMessagesMatches(xpath("/soap:Envelope/soap:Header/ssek:SSEK/ssek:SenderId/text() = '" + RIV_SENDER_ID_VALUE + "'").namespaces(nameSpaces));
		ssekEndpoint.expectedMessagesMatches(xpath("/soap:Envelope/soap:Header/ssek:SSEK/ssek:ReceiverId/text() = '" + RIV_RECEIVER_ID_VALUE + "'").namespaces(nameSpaces));
		ssekEndpoint.expectedMessagesMatches(xpath("/soap:Envelope/soap:Header/ssek:SSEK/ssek:TxId/text() = '" + RIV_CORR_ID_VALUE + "'").namespaces(nameSpaces));
		ssekEndpoint.expectedMessagesMatches(xpath("/soap:Envelope/soap:Body/ns:HelloWorldRequest/ns:Message/text() = '" + RIV_PAYLOAD + "'").namespaces(nameSpaces));

		// Build SSEK response
		ssekEndpoint.whenAnyExchangeReceived(new Processor() {

			@Override
			public void process(Exchange exchange) throws Exception {
				
				Message outMessage = exchange.getOut();
				outMessage.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpServletResponse.SC_OK);
				outMessage.setHeader(Exchange.CONTENT_TYPE, "text/xml");
				outMessage.setBody(ssek_response_ok);
			}
		});

		String response = camel.requestBodyAndHeaders("direct:in-riv2ssek", riv_request, riv_request_http_headers, String.class);
		Assert.assertNotNull(response);

		MockEndpoint.assertIsSatisfied(ssekEndpoint);
	}

	/**
	 * Injects RIV request without <soapenv:Header><add:To> element into RIV
	 * endpoint. Make sure that a default ReceiverId is filled in by the route builder.
	 * 
	 * @throws InterruptedException
	 */
	@DirtiesContext
	@Test
	public void testRiv2SsekMissingReceiverId() throws InterruptedException {
		ssekEndpoint.expectedMessageCount(1);
		ssekEndpoint.expectedMessagesMatches(xpath("/soap:Envelope/soap:Header/ssek:SSEK/ssek:ReceiverId/text() = '" + RIV_RECEIVER_ID_VALUE + "'").namespaces(nameSpaces));

		// Build SSEK response
		ssekEndpoint.whenAnyExchangeReceived(new Processor() {

			@Override
			public void process(Exchange paramExchange) throws Exception {
				paramExchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpServletResponse.SC_OK);
				paramExchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/xml");
				paramExchange.getOut().setBody(ssek_response_ok);
			}
		});

		String response = camel.requestBodyAndHeaders("direct:in-riv2ssek", riv_request_no_addTo, riv_request_http_headers, String.class);
		Assert.assertNotNull(response);

		MockEndpoint.assertIsSatisfied(ssekEndpoint);
	}

	/**
	 * Injects RIV request without HTTP header RIV corrId into RIV
	 * endpoint. Make sure that a generated TxId is filled in by the route builder if the RIV corrId is missing.
	 * 
	 * @throws InterruptedException
	 */
	@DirtiesContext
	@Test
	public void testRiv2SsekMissingTxId() throws InterruptedException {
		ssekEndpoint.expectedMessageCount(1);
		ssekEndpoint.expectedMessagesMatches(xpath("/soap:Envelope/soap:Header/ssek:SSEK/ssek:TxId/text()").namespaces(nameSpaces));

		// Build SSEK response
		ssekEndpoint.whenAnyExchangeReceived(new Processor() {

			@Override
			public void process(Exchange paramExchange) throws Exception {
				paramExchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpServletResponse.SC_OK);
				paramExchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/xml");
				paramExchange.getOut().setBody(ssek_response_ok);
			}
		});

		// Remove the TxId from the header to simulate the error.
		riv_request_http_headers.remove(RivSsekRouteBuilder.RIV_CORR_ID);
		String response = camel.requestBodyAndHeaders("direct:in-riv2ssek", riv_request, riv_request_http_headers, String.class);
		Assert.assertNotNull(response);

		MockEndpoint.assertIsSatisfied(ssekEndpoint);
	}

	/**
	 * Injects RIV request without HTTP header RIV senderId into RIV
	 * endpoint. Make sure that a default SenderId is filled in by the route builder in this case.
	 * 
	 * @throws InterruptedException
	 */
	@DirtiesContext
	@Test
	public void testRiv2SsekMissingSenderId() throws InterruptedException {
		ssekEndpoint.expectedMessageCount(1);
		ssekEndpoint.expectedMessagesMatches(xpath("/soap:Envelope/soap:Header/ssek:SSEK/ssek:SenderId/text()").namespaces(nameSpaces));

		// Build SSEK response
		ssekEndpoint.whenAnyExchangeReceived(new Processor() {

			@Override
			public void process(Exchange paramExchange) throws Exception {
				paramExchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpServletResponse.SC_OK);
				paramExchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/xml");
				paramExchange.getOut().setBody(ssek_response_ok);
			}
		});

		// Remove the SenderId from the header to simulate the error.
		riv_request_http_headers.remove(RivSsekRouteBuilder.RIV_SENDER_ID);
		String response = camel.requestBodyAndHeaders("direct:in-riv2ssek", riv_request, riv_request_http_headers, String.class);
		Assert.assertNotNull(response);

		MockEndpoint.assertIsSatisfied(ssekEndpoint);
	}

	/**
	 * Injects RIV request into RIV endpoint. SSEK endpoint responds with ERROR
	 * that is sent as HttpServletResponse.SC_BAD_REQUEST.
	 * 
	 * @throws InterruptedException
	 */
	@DirtiesContext
	@Test(expectedExceptions = CamelExecutionException.class)
	public void testRiv2SsekHttp400() throws InterruptedException {
		ssekEndpoint.whenAnyExchangeReceived(new Processor() {

			@Override
			public void process(Exchange paramExchange) throws Exception {
				// Build reply
				paramExchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE,
						HttpServletResponse.SC_BAD_REQUEST);
				paramExchange.getOut().setHeader(Exchange.CONTENT_TYPE,
						"text/xml");
				paramExchange.getOut().setBody(createSoapFault());
			}
		});

		camel.requestBodyAndHeaders("direct:in-riv2ssek", riv_request,
				riv_request_http_headers, String.class);
	}

	/**
	 * Injects RIV request into RIV endpoint. SSEK endpoint responds with ERROR
	 * that is sent as HttpServletResponse.SC_INTERNAL_SERVER_ERROR.
	 * 
	 * @throws InterruptedException
	 */
	@DirtiesContext
	@Test(expectedExceptions = CamelExecutionException.class)
	public void testRiv2SsekHttp500() throws InterruptedException {
		ssekEndpoint.whenAnyExchangeReceived(new Processor() {

			@Override
			public void process(Exchange paramExchange) throws Exception {
				// Build reply
				paramExchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE,
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				paramExchange.getOut().setHeader(Exchange.CONTENT_TYPE,
						"text/xml");
				paramExchange.getOut().setBody(createSoapFault());
			}
		});

		camel.requestBodyAndHeaders("direct:in-riv2ssek", riv_request,
				riv_request_http_headers, String.class);
	}

	/**
	 * Helper function for creating SOAPFault.
	 * 
	 * @return
	 * @throws SOAPException
	 * @throws IOException
	 */
	private Object createSoapFault() throws SOAPException, IOException {
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPFault soapFault = soapMessage.getSOAPBody().addFault();
		soapFault.setFaultCode(new QName(
				SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Server"));

		// SSEK can return a fault data section
		URL inUrl = ClassLoader
				.getSystemResource("ssek-response-fault-data.xml");
		InputStream inStream = inUrl.openStream();
		String s = IOUtils.toString(inStream, "us-ascii");
		soapFault.setFaultString(s);

		return soapMessage.getSOAPPart();
	}
}
