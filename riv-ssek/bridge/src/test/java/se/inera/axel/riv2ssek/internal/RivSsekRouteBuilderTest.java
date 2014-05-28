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

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.builder.xml.XPathBuilder;
import org.apache.camel.component.http.HttpOperationFailedException;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.AvailablePortFinder;
import org.apache.camel.testng.AbstractCamelTestNGSpringContextTests;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.axel.riv2ssek.SsekServiceInfo;
import se.inera.ifv.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderService;
import se.inera.ifv.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;

import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.apache.camel.builder.xml.XPathBuilder.xpath;
import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.testng.Assert.*;
import static se.inera.axel.riv2ssek.internal.RivSsekRouteBuilderTest.TestMessage.*;

@ContextConfiguration
public class RivSsekRouteBuilderTest extends AbstractCamelTestNGSpringContextTests {
    @Autowired
    @Qualifier("riv-ssek")
    private CamelContext camelContext;

    @Autowired
    @Qualifier("riv-ssek-bridge-test")
    private CamelContext camelContextTest;

    @Autowired
    private RivSsekMappingService rivSsekMappingServiceMock;

    public enum TestMessage {
        SSEK_HELLO_WORLD_OK("/ssek-responses/helloWorldResponse.xml"),
        SSEK_FAULT_RECEIVERIDUNKNOWN("/ssek-responses/500_ReceiverIdUnknown.xml"),
        SSEK_REGISTERMEDICALCERTIFICATE_RESPONSE("/ssek-responses/registerMedicalCertificateResponse.xml"),
        RIV_REQUEST_MISSING_RECEIVER("/riv-requests/requestWithReceiverMissing.xml"),
        RIV_REGISTERMEDICALCERTIFICATE_REQUEST("/riv-requests/registerMedicalCertificateRequest.xml");

        private final String resourcePath;

        TestMessage(String resourcePath) {
            this.resourcePath = resourcePath;
        }

        public InputStream getResourceAsStream() {
            return getClass().getResourceAsStream(resourcePath);
        }
    }

    private final Namespaces namespaces = new Namespaces("riv", "urn:riv:itintegration:registry:1")
            .add("add", "http://www.w3.org/2005/08/addressing")
            .add("soap", "http://schemas.xmlsoap.org/soap/envelope/")
            .add("ssek", "http://schemas.ssek.org/ssek/2006-05-10/")
            .add("ns", "http://schemas.ssek.org/helloworld/2011-11-17");

    Map<String, Object> rivRequestHttpHeaders = new HashMap<>();

    private static final String RIV_CORR_ID_TESTVALUE = UUID.randomUUID().toString();
    private static final String RIV_SENDER_TESTVALUE = "TEST_SENDER";
    private static final String RIV_RECEIVER_TESTVALUE = "TEST_RECEIVER";
    private static final String RIV_PAYLOAD_TESTVALUE = "TEST_PAYLOAD";
    public static final String RIV_SENDER = "x-rivta-original-serviceconsumer-hsaid";
    public static final String RIV_CORR_ID = "x-vp-correlation-id";
    
    @Produce(context = "riv-ssek-bridge-test")
    ProducerTemplate camel;

    @EndpointInject(uri = "mock:ssekRegisterMedicalCertificate")
    MockEndpoint ssekRegisterMedicalCertificate;

    @EndpointInject(uri = "mock:ssekHelloWorld")
    MockEndpoint ssekHelloWorld;

    private final int riv2ssekPort;

    public RivSsekRouteBuilderTest() {
        super();

        // Needs to be put into constructor instead of beforeTest method because the camel context needs it.
        riv2ssekPort = AvailablePortFinder.getNextAvailable();
        System.setProperty("riv2ssekEndpoint", String.format("jetty://http://0.0.0.0:%s", riv2ssekPort));
        System.setProperty("riv2ssekEndpoint.path", "/rivSsekEndpoint");
        System.setProperty("ssekEndpoint.port", Integer.toString(AvailablePortFinder.getNextAvailable()));
        System.setProperty(Exchange.LOG_DEBUG_BODY_MAX_CHARS, "0");

    }



    @BeforeTest
    public void beforeTest() throws Exception {
    }

    @BeforeMethod
    public void beforeMethod() throws Exception {
        // Initialize RIV request HTTP headers
        rivRequestHttpHeaders.put("SOAPAction", "urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3");
        rivRequestHttpHeaders.put(Exchange.CONTENT_TYPE, "application/xml");
        rivRequestHttpHeaders.put(RIV_CORR_ID, RIV_CORR_ID_TESTVALUE);
        rivRequestHttpHeaders.put(RIV_SENDER, RIV_SENDER_TESTVALUE);

        MockEndpoint.resetMocks(camelContext);
        MockEndpoint.resetMocks(camelContextTest);
        ssekRegisterMedicalCertificate.expectedMessageCount(0);
        ssekHelloWorld.expectedMessageCount(0);

        Mockito.reset(rivSsekMappingServiceMock);
        Mockito.when(rivSsekMappingServiceMock.lookupSsekService(anyString(), anyString()))
                .thenReturn(new SsekServiceInfo.Builder().address(
                        camelContext.resolvePropertyPlaceholders("{{ssekEndpoint.server}}:{{ssekEndpoint.port}}/registerMedicalCertificate")).build());

    }

    /**
     * Injects RIV request with a payload.
     * Make sure this is used.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRiv2SsekWithPayload() throws InterruptedException {
        ssekRegisterMedicalCertificate.expectedMessageCount(1);
        ssekRegisterMedicalCertificate.expectedMessagesMatches(
                xpath("/soap:Envelope/soap:Body/*/text() = '"
                      + RIV_PAYLOAD_TESTVALUE + "'").namespaces(namespaces));

        // Build SSEK response
        ssekRegisterMedicalCertificate.whenAnyExchangeReceived(
                httpResponse(HttpServletResponse.SC_OK, SSEK_HELLO_WORLD_OK));

        String response = camel.requestBodyAndHeaders("direct:in-riv2ssek",
                RIV_REGISTERMEDICALCERTIFICATE_REQUEST.getResourceAsStream(),
                rivRequestHttpHeaders,
                String.class);

        assertNotNull(response);

        assertMocksAreSatisfied();
    }

    /**
     * Injects RIV request with <soap:Header><add:To> element.
     * Make sure that this is used.
     *
     * @throws InterruptedException
     */
    @Test
    public void testRiv2SsekWithReceiver() throws InterruptedException {
        ssekRegisterMedicalCertificate.expectedMessageCount(1);
        ssekRegisterMedicalCertificate.expectedMessagesMatches(
                xpath("/soap:Envelope/soap:Header/ssek:SSEK/ssek:ReceiverId/text() = '"
                      + RIV_RECEIVER_TESTVALUE + "'").namespaces(namespaces)
        );

        // Build SSEK response
        ssekRegisterMedicalCertificate.whenAnyExchangeReceived(
                httpResponse(HttpServletResponse.SC_OK, SSEK_HELLO_WORLD_OK));

        String response = camel.requestBodyAndHeaders("direct:in-riv2ssek",
                RIV_REGISTERMEDICALCERTIFICATE_REQUEST.getResourceAsStream(),
                rivRequestHttpHeaders,
                String.class);

        assertNotNull(response);
        assertMocksAreSatisfied();
    }

    /**
     * Injects RIV request without <soap:Header><add:To> element.
     * Make sure that a default receiver is filled in by the route builder.
     *
     * @throws InterruptedException
     */
    @Test
    public void testRiv2SsekWithReceiverMissing() throws InterruptedException {
        ssekRegisterMedicalCertificate.expectedMessageCount(0);

        // Build SSEK response
        ssekRegisterMedicalCertificate.whenAnyExchangeReceived(
                httpResponse(HttpServletResponse.SC_OK, SSEK_HELLO_WORLD_OK));

        try {
            camel.requestBodyAndHeaders("direct:in-riv2ssek",
                    RIV_REQUEST_MISSING_RECEIVER.getResourceAsStream(),
                    rivRequestHttpHeaders,
                    String.class);
            fail("Soap fault should have been returned");
        } catch (CamelExecutionException e) {
            assertThat(e.getCause(), instanceOf(HttpOperationFailedException.class));
            HttpOperationFailedException httpOperationFailedException = (HttpOperationFailedException) e.getCause();
            assertThat(httpOperationFailedException.getStatusCode(), is(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
            assertThat(
                    evaluateXpath("/soap:Envelope/soap:Body/soap:Fault/faultactor/text()",
                            httpOperationFailedException.getResponseBody()),
                    is("Inera"));
        }

        assertMocksAreSatisfied();
    }

    /**
     * Injects RIV request with unknown receiver. Make sure that the SSEK
     * error is properly returned all the way back to RIV.
     *
     * @throws InterruptedException
     */
    @Test
    public void testRiv2SsekWithHttpError500() throws InterruptedException {

        // Build SSEK response
        ssekRegisterMedicalCertificate.whenAnyExchangeReceived(httpResponse(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                SSEK_FAULT_RECEIVERIDUNKNOWN));

        try {
            camel.sendBodyAndHeaders("direct:in-riv2ssek",
                    RIV_REGISTERMEDICALCERTIFICATE_REQUEST.getResourceAsStream(),
                    rivRequestHttpHeaders);
        } catch (CamelExecutionException e) {
            String responseBody = e.getExchange().getException(HttpOperationFailedException.class).getResponseBody();

            assertTrue(responseBody.contains("p:ReceiverIdUnknown"));

            return;
        }

        fail("ReceiverIdUnknown soap fault should have been returned");
    }

    /**
     * Injects RIV request with "x-vp-correlation-id" HTTP header.
     * Make sure that this is used.
     *
     * @throws InterruptedException
     */
    @Test
    public void testRiv2SsekWithTxId() throws InterruptedException {
        ssekRegisterMedicalCertificate.expectedMessageCount(1);
        ssekRegisterMedicalCertificate.expectedMessagesMatches(xpath("/soap:Envelope/soap:Header/ssek:SSEK/ssek:TxId/text() = '"
                                                   + RIV_CORR_ID_TESTVALUE + "'").namespaces(namespaces));

        // Build SSEK response
        ssekRegisterMedicalCertificate.whenAnyExchangeReceived(httpResponse(HttpServletResponse.SC_OK, SSEK_HELLO_WORLD_OK));

        String response = camel.requestBodyAndHeaders("direct:in-riv2ssek",
                RIV_REGISTERMEDICALCERTIFICATE_REQUEST.getResourceAsStream(),
                rivRequestHttpHeaders,
                String.class);

        assertNotNull(response);
        assertMocksAreSatisfied();
    }

    /**
     * Injects RIV request without the "x-vp-correlation-id" HTTP header.
     * Make sure that a generated TxId is filled in by the route builder.
     *
     * @throws InterruptedException
     */
    @Test
    public void testRiv2SsekWithTxIdMissing() throws InterruptedException {
        ssekRegisterMedicalCertificate.expectedMessageCount(1);
        ssekRegisterMedicalCertificate.expectedMessagesMatches(
                xpath("/soap:Envelope/soap:Header/ssek:SSEK/ssek:TxId/text()").namespaces(namespaces));

        // Build SSEK response
        ssekRegisterMedicalCertificate.whenAnyExchangeReceived(httpResponse(HttpServletResponse.SC_OK, SSEK_HELLO_WORLD_OK));

        // Remove the corrId from the RIV header in order to simulate the error.
        rivRequestHttpHeaders.remove(RIV_CORR_ID);
        String response = camel.requestBodyAndHeaders("direct:in-riv2ssek",
                RIV_REGISTERMEDICALCERTIFICATE_REQUEST.getResourceAsStream(),
                rivRequestHttpHeaders,
                String.class);

        assertNotNull(response);
        assertMocksAreSatisfied();
    }

    @Test
    public void sendJaxwsRegisterMedicalCertificateRequest() throws InterruptedException {
        ssekRegisterMedicalCertificate.expectedMessageCount(1);
        ssekRegisterMedicalCertificate.whenAnyExchangeReceived(
                httpResponse(HttpServletResponse.SC_OK, SSEK_REGISTERMEDICALCERTIFICATE_RESPONSE));

        RegisterMedicalCertificateResponderInterface registerMedicalCertificateResponderPort =
                getRegisterMedicalCertificateResponderInterface();

        RegisterMedicalCertificateType registerMedicalCertificateType = new RegisterMedicalCertificateType();
        AttributedURIType to = new AttributedURIType();
        to.setValue("0000000000");

        registerMedicalCertificateResponderPort.registerMedicalCertificate(to, registerMedicalCertificateType);

        assertMocksAreSatisfied();
    }

    @Test
    public void sendRegisterMedicalCertificateWithUnknownReceiverId() {
        ssekRegisterMedicalCertificate.whenAnyExchangeReceived(
                httpResponse(HttpServletResponse.SC_OK, SSEK_FAULT_RECEIVERIDUNKNOWN));

        RegisterMedicalCertificateResponderInterface registerMedicalCertificateResponderPort =
                getRegisterMedicalCertificateResponderInterface();

        RegisterMedicalCertificateType registerMedicalCertificateType = new RegisterMedicalCertificateType();
        AttributedURIType to = new AttributedURIType();
        to.setValue("1111111111");

        try {
            registerMedicalCertificateResponderPort.registerMedicalCertificate(to, registerMedicalCertificateType);
            fail("Should have thrown Soap fault");
        } catch (SOAPFaultException e) {
            assertEquals(e.getFault().getFaultActor(), "Prisma@SESTVER53");
            assertEquals(e.getFault().getFaultCodeAsQName().getNamespaceURI(), "http://schemas.ssek.org/ssek/2006-05-10/");
            assertEquals(e.getFault().getFaultCodeAsQName().getLocalPart(), "ReceiverIdUnknown");
        }
    }

    protected RegisterMedicalCertificateResponderInterface getRegisterMedicalCertificateResponderInterface() {
        RegisterMedicalCertificateResponderService registerMedicalCertificateResponderService =
                new RegisterMedicalCertificateResponderService();
        RegisterMedicalCertificateResponderInterface registerMedicalCertificateResponderPort =
                registerMedicalCertificateResponderService.getRegisterMedicalCertificateResponderPort();

        Client client =
                ClientProxy.getClient(registerMedicalCertificateResponderPort);
        Endpoint cxfEndpoint = client.getEndpoint();
        cxfEndpoint.getInInterceptors().add(new LoggingInInterceptor());
        cxfEndpoint.getOutInterceptors().add(new LoggingOutInterceptor());
        String riv2ssekEndpoint = System.getProperty("riv2ssekEndpoint");
        String riv2ssekEndpointPath = System.getProperty("riv2ssekEndpoint.path");
        if (riv2ssekEndpoint.startsWith("jetty://")) {
            riv2ssekEndpoint = riv2ssekEndpoint.substring("jetty://".length());
        }
        ((BindingProvider)registerMedicalCertificateResponderPort).getRequestContext()
                .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                        riv2ssekEndpoint + riv2ssekEndpointPath);
        return registerMedicalCertificateResponderPort;
    }

    protected Processor httpResponse(final int responseCode, final TestMessage message) {
        return new Processor() {

            @Override
            public void process(Exchange paramExchange) throws Exception {
                paramExchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, responseCode);
                paramExchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/xml");
                paramExchange.getOut().setBody(message.getResourceAsStream());
            }
        };
    }

    protected String evaluateXpath(String xpath, String body) {
        return XPathBuilder.xpath(xpath).namespaces(namespaces)
                .evaluate(camelContext, body, String.class);
    }

    protected void assertMocksAreSatisfied() throws InterruptedException {
        assertIsSatisfied(camelContext);
        assertIsSatisfied(camelContextTest);
    }
}
