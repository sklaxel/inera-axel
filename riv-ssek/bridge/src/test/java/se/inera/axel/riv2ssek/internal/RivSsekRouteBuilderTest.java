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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.component.http.HttpOperationFailedException;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.AvailablePortFinder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@ContextConfiguration
public class RivSsekRouteBuilderTest extends AbstractTestNGSpringContextTests {
	
	private Namespaces nameSpaces;

	private File rivRequestFile;
	Map<String, Object> rivRequestHttpHeaders = new HashMap<>();

	private File ssekResponseOkFile;

	private static final String RIV_CORR_ID_TESTVALUE = UUID.randomUUID().toString();
	private static final String RIV_SENDER_TESTVALUE = "TEST_SENDER";
	private static final String RIV_RECEIVER_TESTVALUE = "TEST_RECEIVER";
	private static final String RIV_PAYLOAD_TESTVALUE = "TEST_PAYLOAD";
	
	private static String ssekDefaultSender;
	private static String ssekDefaultReceiver;

	@Produce(context = "riv-ssek-bridge-test")
	ProducerTemplate camel;

	@EndpointInject(uri = "mock:ssek")
	MockEndpoint ssekEndpoint;

	public RivSsekRouteBuilderTest() {
		super();

		// Needs to be put into constructor instead of beforeTest method because the camel context needs it.
		System.setProperty("riv2ssekEndpoint.port", Integer.toString(AvailablePortFinder.getNextAvailable()));
		System.setProperty("ssekEndpoint.port", Integer.toString(AvailablePortFinder.getNextAvailable()));
	}

	@BeforeTest
	public void beforeTest() throws Exception {
		// Initialize RIV requests
		rivRequestFile = new File(ClassLoader.getSystemResource("riv-requests/registerMedicalCertificateRequest.xml").getFile());

		// Initialize SSEK response
		ssekResponseOkFile = new File(ClassLoader.getSystemResource("ssek-responses/helloWorldResponse.xml").getFile());
		
		// Initialize name spaces
    	nameSpaces = new Namespaces("soap", "http://schemas.xmlsoap.org/soap/envelope/");
    	nameSpaces.add("ssek", "http://schemas.ssek.org/ssek/2006-05-10/");
    	nameSpaces.add("ns", "http://schemas.ssek.org/helloworld/2011-11-17");
	}

	@BeforeMethod
	public void beforeMethod() throws Exception {
		// Initialize RIV request HTTP headers
		rivRequestHttpHeaders.put("SOAPAction", "urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3");
		rivRequestHttpHeaders.put(Exchange.CONTENT_TYPE, "application/xml");
		rivRequestHttpHeaders.put(RivToCamelProcessor.RIV_CORR_ID, RIV_CORR_ID_TESTVALUE);
		rivRequestHttpHeaders.put(RivToCamelProcessor.RIV_SENDER, RIV_SENDER_TESTVALUE);

    	// Get default SSEK values
    	ssekDefaultSender = camel.getCamelContext().resolvePropertyPlaceholders("{{ssekDefaultSender}}");
    	ssekDefaultReceiver = camel.getCamelContext().resolvePropertyPlaceholders("{{ssekDefaultReceiver}}");
	}

	/**
	 * Injects RIV request with a payload.
	 * Make sure this is used.
	 * 
	 * @throws InterruptedException
	 */
	@DirtiesContext
	@Test
	public void testRiv2SsekWithPayload() throws InterruptedException {
        ssekEndpoint.expectedMessageCount(1);
		ssekEndpoint.expectedMessagesMatches(xpath("/soap:Envelope/soap:Body/ns:HelloWorldRequest/ns:Message/text() = '" + RIV_PAYLOAD_TESTVALUE + "'").namespaces(nameSpaces));

		// Build SSEK response
		ssekEndpoint.whenAnyExchangeReceived(new Processor() {

			@Override
			public void process(Exchange exchange) throws Exception {
				
				Message outMessage = exchange.getOut();
				outMessage.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpServletResponse.SC_OK);
				outMessage.setHeader(Exchange.CONTENT_TYPE, "text/xml");
				outMessage.setBody(ssekResponseOkFile);
			}
		});

		String response = camel.requestBodyAndHeaders("direct:in-riv2ssek", rivRequestFile, rivRequestHttpHeaders, String.class);
		Assert.assertNotNull(response);

		MockEndpoint.assertIsSatisfied(ssekEndpoint);
	}

	/**
	 * Injects RIV request with <soap:Header><add:To> element.
	 * Make sure that this is used.
	 * 
	 * @throws InterruptedException
	 */
	@DirtiesContext
	@Test
	public void testRiv2SsekWithReceiver() throws InterruptedException {
		ssekEndpoint.expectedMessageCount(1);
		ssekEndpoint.expectedMessagesMatches(xpath("/soap:Envelope/soap:Header/ssek:SSEK/ssek:ReceiverId/text() = '" + RIV_RECEIVER_TESTVALUE + "'").namespaces(nameSpaces));

		// Build SSEK response
		ssekEndpoint.whenAnyExchangeReceived(new Processor() {

			@Override
			public void process(Exchange paramExchange) throws Exception {
				paramExchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpServletResponse.SC_OK);
				paramExchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/xml");
				paramExchange.getOut().setBody(ssekResponseOkFile);
			}
		});

		String response = camel.requestBodyAndHeaders("direct:in-riv2ssek", rivRequestFile, rivRequestHttpHeaders, String.class);
		Assert.assertNotNull(response);

		MockEndpoint.assertIsSatisfied(ssekEndpoint);
	}

	/**
	 * Injects RIV request without <soap:Header><add:To> element.
	 * Make sure that a default receiver is filled in by the route builder.
	 * 
	 * @throws InterruptedException
	 */
	@DirtiesContext
	@Test
	public void testRiv2SsekWithReceiverMissing() throws InterruptedException {
		ssekEndpoint.expectedMessageCount(1);
		ssekEndpoint.expectedMessagesMatches(xpath("/soap:Envelope/soap:Header/ssek:SSEK/ssek:ReceiverId/text() = '" + ssekDefaultReceiver + "'").namespaces(nameSpaces));

		// Build SSEK response
		ssekEndpoint.whenAnyExchangeReceived(new Processor() {

			@Override
			public void process(Exchange paramExchange) throws Exception {
				paramExchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpServletResponse.SC_OK);
				paramExchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/xml");
				paramExchange.getOut().setBody(ssekResponseOkFile);
			}
		});

		File f = new File(ClassLoader.getSystemResource("riv-requests/requestWithReceiverMissing.xml").getFile());
		String response = camel.requestBodyAndHeaders("direct:in-riv2ssek", f, rivRequestHttpHeaders, String.class);
		Assert.assertNotNull(response);

		MockEndpoint.assertIsSatisfied(ssekEndpoint);
	}

	/**
	 * Injects RIV request with unknown receiver. Make sure that the SSEK
	 * error is properly returned all the way back to RIV.
	 * 
	 * @throws InterruptedException
	 */
	@DirtiesContext
	@Test
	public void testRiv2SsekWithHttpError500() throws InterruptedException {

		// Build SSEK response
		ssekEndpoint.whenAnyExchangeReceived(new Processor() {

			@Override
			public void process(Exchange paramExchange) throws Exception {
				paramExchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				paramExchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/xml");
				
				File f = new File(ClassLoader.getSystemResource("ssek-responses/500_ReceiverIdUnknown.xml").getFile());
				paramExchange.getOut().setBody(f);
			}
		});

		try {
			camel.sendBodyAndHeaders("direct:in-riv2ssek", rivRequestFile, rivRequestHttpHeaders);
		} catch (CamelExecutionException e) {
			String responseBody = e.getExchange().getException(HttpOperationFailedException.class).getResponseBody();
			
			boolean isExpectedError = responseBody.indexOf("p:ReceiverIdUnknown") != -1 ? true : false;
			Assert.assertTrue(isExpectedError);

			return;
		}

		Assert.fail();
	}

	/**
	 * Injects RIV request with "x-vp-correlation-id" HTTP header.
	 * Make sure that this is used.
	 * 
	 * @throws InterruptedException
	 */
	@DirtiesContext
	@Test
	public void testRiv2SsekWithTxId() throws InterruptedException {
		ssekEndpoint.expectedMessageCount(1);
		ssekEndpoint.expectedMessagesMatches(xpath("/soap:Envelope/soap:Header/ssek:SSEK/ssek:TxId/text() = '" + RIV_CORR_ID_TESTVALUE + "'").namespaces(nameSpaces));

		// Build SSEK response
		ssekEndpoint.whenAnyExchangeReceived(new Processor() {

			@Override
			public void process(Exchange paramExchange) throws Exception {
				paramExchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpServletResponse.SC_OK);
				paramExchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/xml");
				paramExchange.getOut().setBody(ssekResponseOkFile);
			}
		});

		String response = camel.requestBodyAndHeaders("direct:in-riv2ssek", rivRequestFile, rivRequestHttpHeaders, String.class);
		Assert.assertNotNull(response);

		MockEndpoint.assertIsSatisfied(ssekEndpoint);
	}

	/**
	 * Injects RIV request without the "x-vp-correlation-id" HTTP header.
	 * Make sure that a generated TxId is filled in by the route builder.
	 * 
	 * @throws InterruptedException
	 */
	@DirtiesContext
	@Test
	public void testRiv2SsekWithTxIdMissing() throws InterruptedException {
		ssekEndpoint.expectedMessageCount(1);
		ssekEndpoint.expectedMessagesMatches(xpath("/soap:Envelope/soap:Header/ssek:SSEK/ssek:TxId/text()").namespaces(nameSpaces));

		// Build SSEK response
		ssekEndpoint.whenAnyExchangeReceived(new Processor() {

			@Override
			public void process(Exchange paramExchange) throws Exception {
				paramExchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpServletResponse.SC_OK);
				paramExchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/xml");
				paramExchange.getOut().setBody(ssekResponseOkFile);
			}
		});

		// Remove the corrId from the RIV header in order to simulate the error.
		rivRequestHttpHeaders.remove(RivToCamelProcessor.RIV_CORR_ID);
		String response = camel.requestBodyAndHeaders("direct:in-riv2ssek", rivRequestFile, rivRequestHttpHeaders, String.class);
		Assert.assertNotNull(response);

		MockEndpoint.assertIsSatisfied(ssekEndpoint);
	}

	/**
	 * Injects RIV request with "x-rivta-original-serviceconsumer-hsaid" HTTP header. 
	 * Make sure that this is used.
	 * 
	 * @throws Exception 
	 */
	@DirtiesContext
	@Test
	public void testRiv2SsekWithSender() throws Exception {
    	ssekEndpoint.expectedMessageCount(1);
		ssekEndpoint.expectedMessagesMatches(xpath("/soap:Envelope/soap:Header/ssek:SSEK/ssek:SenderId/text() = '" + RIV_SENDER_TESTVALUE + "'").namespaces(nameSpaces));

		// Build SSEK response
		ssekEndpoint.whenAnyExchangeReceived(new Processor() {

			@Override
			public void process(Exchange paramExchange) throws Exception {
				paramExchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpServletResponse.SC_OK);
				paramExchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/xml");
				paramExchange.getOut().setBody(ssekResponseOkFile);
			}
		});

		String response = camel.requestBodyAndHeaders("direct:in-riv2ssek", rivRequestFile, rivRequestHttpHeaders, String.class);
		Assert.assertNotNull(response);

		MockEndpoint.assertIsSatisfied(ssekEndpoint);
	}

	/**
	 * Injects RIV request without "x-rivta-original-serviceconsumer-hsaid" HTTP header. 
	 * Make sure that a default sender is filled in by the route builder.
	 * 
	 * @throws Exception 
	 */
	@DirtiesContext
	@Test
	public void testRiv2SsekWithSenderMissing() throws Exception {
    	ssekEndpoint.expectedMessageCount(1);
		ssekEndpoint.expectedMessagesMatches(xpath("/soap:Envelope/soap:Header/ssek:SSEK/ssek:SenderId/text() = '" + ssekDefaultSender + "'").namespaces(nameSpaces));

		// Build SSEK response
		ssekEndpoint.whenAnyExchangeReceived(new Processor() {

			@Override
			public void process(Exchange paramExchange) throws Exception {
				paramExchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpServletResponse.SC_OK);
				paramExchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/xml");
				paramExchange.getOut().setBody(ssekResponseOkFile);
			}
		});

		// Remove the SenderId from the header to simulate the error.
		rivRequestHttpHeaders.remove(RivToCamelProcessor.RIV_SENDER);
		String response = camel.requestBodyAndHeaders("direct:in-riv2ssek", rivRequestFile, rivRequestHttpHeaders, String.class);
		Assert.assertNotNull(response);

		MockEndpoint.assertIsSatisfied(ssekEndpoint);
	}
}
