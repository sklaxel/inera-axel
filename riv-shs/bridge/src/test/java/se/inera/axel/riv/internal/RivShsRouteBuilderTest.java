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
package se.inera.axel.riv.internal;

import com.natpryce.makeiteasy.Maker;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.AvailablePortFinder;
import org.apache.camel.testng.CamelTestSupport;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.message.MessageContentsList;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.inera.axel.riv.RivShsMappingService;
import se.inera.axel.shs.camel.DefaultCamelToShsMessageProcessor;
import se.inera.axel.shs.camel.DefaultShsMessageToCamelProcessor;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ResponseMessageBuilder;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.Product;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.ShsLabelMaker;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.apache.camel.builder.xml.XPathBuilder.xpath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static se.inera.axel.shs.mime.ShsMessageMaker.*;
import static se.inera.axel.shs.mime.ShsMessageMaker.DataPartInstantiator.dataHandler;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessageInstantiator.dataParts;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessageInstantiator.label;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.*;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.to;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class RivShsRouteBuilderTest extends CamelTestSupport {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private static final Namespaces NAMESPACES = new Namespaces("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");

    static {
        NAMESPACES.add("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        NAMESPACES.add("registry", "urn:riv:itintegration:registry:1");
        NAMESPACES.add("ping", "urn:riv:itintegration:monitoring:PingForConfigurationResponder:1");
    }


    private RepositoryRivShsMappingService rivShsMapper;
    private Maker<DataPart> pingRequestDataPart;
    private Maker<DataPart> pingRequestWithoutNamespace;
    private Maker<ShsMessage> shsMessageMaker;
    private static final int PING_PORT = AvailablePortFinder.getNextAvailable();
    private static final int RIV_IN_PORT = AvailablePortFinder.getNextAvailable();
    private static final String PING_ENDPOINT = String.format("http://localhost:%s/CamelContext/Ping", PING_PORT);
    private static final String PING_NAMESPACE = "urn:riv:itintegration:monitoring:PingForConfigurationResponder:1";
    private static final String PING_REQUEST_BODY =
            "<urn:PingForConfiguration xmlns:urn=\"urn:riv:itintegration:monitoring:PingForConfigurationResponder:1\">\n"
            + "         <urn:serviceContractNamespace>urn:riv:itintegration:monitoring:PingForConfigurationResponder:1</urn:serviceContractNamespace>\n"
            + "         <urn:logicalAddress>%s</urn:logicalAddress>\n"
            + "         <!--You may enter ANY elements at this point-->\n"
            + "      </urn:PingForConfiguration>";
    private static final String INVALID_PING_REQUEST_BODY =
            "<urn:PingForConfiguration xmlns:urn=\"urn:riv:itintegration:monitoring:PingForConfigurationResponder:1\">\n"
            + "         <urn:logicalAddress>%s</urn:logicalAddress>\n"
            + "         <!--You may enter ANY elements at this point-->\n"
            + "      </urn:PingForConfiguration>";
    private static final String SOAP_PING_REQUEST =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:riv:itintegration:registry:1\">"
            + "    <soapenv:Header>\n"
            + "        <urn:LogicalAddress>%1$s</urn:LogicalAddress>\n"
            + "    </soapenv:Header>\n"
            + "    <soapenv:Body>\n"
            + "        <urn1:PingForConfiguration xmlns:urn1=\"urn:riv:itintegration:monitoring:PingForConfigurationResponder:1\">\n"
            + "              <urn1:serviceContractNamespace>%2$s</urn1:serviceContractNamespace>\n"
            + "              <urn1:logicalAddress>%1$s</urn1:logicalAddress>\n"
            + "              <!--You may enter ANY elements at this point-->\n"
            + "          </urn1:PingForConfiguration>"
            + "    </soapenv:Body>\n"
            + "</soapenv:Envelope>";

    @Test
    public void rivPingRequestShouldReceiveCorrectPingResponse() throws InterruptedException {
        MockEndpoint mockEndpoint = getMockEndpoint("mock:testRiv2Shs");
        mockEndpoint.expectedMinimumMessageCount(1);
        mockEndpoint.expectedMessagesMatches(
                xpath("/soapenv:Envelope/soapenv:Body[count(*) = 1]/ping:PingForConfigurationResponse/ping:pingDateTime")
                .namespaces(NAMESPACES));

        template().requestBodyAndHeader(
                "direct:testRiv2Shs",
                String.format(SOAP_PING_REQUEST, "0000000000", "urn:riv:itintegration:monitoring:PingForConfigurationResponder:1"),
                RivShsMappingService.HEADER_SOAP_ACTION,
                "urn:riv:itintegration:monitoring:PingForConfigurationResponder:1");

        mockEndpoint.assertIsSatisfied(TimeUnit.SECONDS.toMillis(10));
    }

    @Test(enabled = true)
    public void pingResponseDataPartShouldContainPingForConfigurationResponse() throws InterruptedException, IOException {
        // TODO remove sleep temporary fix to avoid SocketException with openjdk
        Thread.sleep(10000);

        MockEndpoint mockEndpoint = getMockEndpoint("mock:testShs2riv");
        mockEndpoint.expectedMinimumMessageCount(1);
        mockEndpoint.expectedMessagesMatches(xpath("/ping:PingForConfigurationResponse/ping:pingDateTime")
                .namespace("ping", "urn:riv:itintegration:monitoring:PingForConfigurationResponder:1"));

        ShsMessage testMessage = make(shsMessageMaker);

        template().requestBody("direct:testShs2riv", testMessage);

        mockEndpoint.assertIsSatisfied(TimeUnit.SECONDS.toMillis(10));
    }

    @Test(enabled = true)
    public void pingRequestShouldBeValid() throws InterruptedException {
        // TODO remove sleep temporary fix to avoid SocketException with openjdk
        Thread.sleep(10000);

        ShsMessage testMessage = make(shsMessageMaker);

        MockEndpoint mockEndpoint = getMockEndpoint("mock:ping");
        mockEndpoint.expectedMinimumMessageCount(1);
        mockEndpoint.expectedMessagesMatches(isValidPingRequest(testMessage));

        template().requestBody("direct:testShs2riv", testMessage);

        mockEndpoint.assertIsSatisfied(TimeUnit.SECONDS.toMillis(10));
    }

    @Test(expectedExceptions = SoapFault.class, enabled = true)
    public void pingRequestWithInvalidToAddressShouldThrow() throws Throwable {
        // TODO remove sleep temporary fix to avoid SocketException with openjdk
        Thread.sleep(10000);
        ShsMessage testMessage = make(shsMessageMaker.but(
                with(label, a(ShsLabel,
                        with(to, to("1111111111"))))));

        try {
            template().requestBody("direct:testShs2riv", testMessage);
        } catch (CamelExecutionException e) {
            throw e.getCause();
        }
    }

    @Test(expectedExceptions = SoapFault.class, enabled = true)
    public void pingRequestWithoutNamespaceShouldThrow() throws Throwable {
        // TODO remove sleep temporary fix to avoid SocketException with openjdk
        Thread.sleep(10000);
        ShsMessage testMessage = make(a(ShsMessage,
                with(label, a(ShsLabel, with(to, to("0000000000")))),
                with(dataParts, listOf(pingRequestWithoutNamespace))));

        try {
            template().requestBody("direct:testShs2riv", testMessage);
        } catch (CamelExecutionException e) {
            throw e.getCause();
        }
    }

    private Predicate isValidPingRequest(final ShsMessage testMessage) {
        return new Predicate() {
            @Override
            public boolean matches(Exchange exchange) {
                MessageContentsList messageContents = exchange.getIn().getBody(MessageContentsList.class);
                assertThat(messageContents, hasSize(2));

                Object logicalAddress = messageContents.get(0);
                assertEquals(logicalAddress, testMessage.getLabel().getTo().getValue());

                Object payload = messageContents.get(1);
                assertThat(payload.getClass(), is(typeCompatibleWith(PingForConfigurationType.class)));

                PingForConfigurationType pingForConfigurationType = (PingForConfigurationType) payload;
                assertThat(pingForConfigurationType.getServiceContractNamespace(),
                        equalTo("urn:riv:itintegration:monitoring:PingForConfigurationResponder:1"));

                return true;
            }
        };
    }

    @BeforeClass
    public void beforeClass() throws IOException {
        //System.setProperty("skipStartingCamelContext", "true");
        System.setProperty("shsInBridgeEndpoint", "direct:shs2riv");
        System.setProperty("rsEndpoint", "direct-vm:shs:rs");
        System.setProperty("rivInBridgeEndpoint", String.format("jetty://http://0.0.0.0:%s/riv", RIV_IN_PORT));
        System.setProperty("rivInBridgePathPrefix", "/riv");

        rivShsMapper = mock(RepositoryRivShsMappingService.class);

        pingRequestDataPart = pingRequestDataPart(PING_REQUEST_BODY, "0000000000");
        pingRequestWithoutNamespace = pingRequestDataPart(INVALID_PING_REQUEST_BODY, "0000000000");

        shsMessageMaker = a(ShsMessage,
                with(dataParts, listOf(pingRequestDataPart)));
    }

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();

        reset(rivShsMapper);
        when(rivShsMapper.mapShsProductToRivService(any(ShsLabel.class)))
                .thenReturn(PING_NAMESPACE + ":PingForConfiguration");
        when(rivShsMapper.mapRivServiceToRivEndpoint(anyString())).thenReturn(PING_ENDPOINT);
        when(rivShsMapper.mapRivServiceToShsProduct(anyString())).thenReturn(ShsLabelMaker.DEFAULT_TEST_PRODUCT_ID);
    }

    private Maker<se.inera.axel.shs.xml.label.To> to(String to) {
        return a(To, with(ToInstantiator.value, to));
    }

    private Maker<DataPart> pingRequestDataPart(String bodyTemplate, String toAddress) throws IOException {
        return a(DataPart,
                with(dataHandler, stringDataHandler(String.format(bodyTemplate, toAddress))));
    }

    @Override
    protected RouteBuilder[] createRouteBuilders() throws Exception {
        return new RouteBuilder[]{new RivShsRouteBuilder(),
                new RouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        from("direct:testShs2riv").routeId("direct:testShs2riv")
                                .streamCaching()
                                .to("direct:shs2riv")
                                .log(LoggingLevel.INFO, "${body}")
                                .to("shsToCamelConverter")
                                .log(LoggingLevel.INFO, "Returned dataPart \"${body}\"")
                                .to("mock:testShs2riv");

                        // Implement Ping service
                        from(String.format("cxf:%s?"
                                           + "wsdlURL=/schemas/interactions/PingForConfigurationInteraction/PingForConfigurationInteraction_1.0_RIVTABP21.wsdl"
                                           + "&serviceClass=se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface"
                                           + "&serviceName={urn:riv:itintegration:monitoring:PingForConfiguration:1:rivtabp21}PingForConfigurationResponderService"
                                           + "&portName={urn:riv:itintegration:monitoring:PingForConfiguration:1:rivtabp21}PingForConfigurationResponderPort"
                                           + "&loggingFeatureEnabled=true"
                                , PING_ENDPOINT))
                                .to("mock:ping")
                                .process(new Processor() {
                                    @Override
                                    public void process(Exchange exchange) throws Exception {
                                        MessageContentsList messageContents = exchange.getIn().getBody(MessageContentsList.class);

                                        String logicalAddress = (String) messageContents.get(0);

                                        // Throw exception for non default to in order to test error handling
                                        if (!ShsLabelMaker.DEFAULT_TEST_TO.equals(logicalAddress)) {
                                            throw new IllegalArgumentException("Illegal to value");
                                        }

                                        PingForConfigurationResponseType response = new PingForConfigurationResponseType();
                                        response.setVersion("1.0");
                                        response.setPingDateTime(DATE_FORMAT.format(new Date()));
                                        exchange.getIn().setBody(new Object[]{response});
                                    }
                                });

                        from("direct:testRiv2Shs").routeId("direct:testRiv2Shs")
                                .streamCaching()
                                .to("{{rivInBridgeEndpoint}}{{rivInBridgePathPrefix}}")
                                .convertBodyTo(String.class)
                                .log(LoggingLevel.INFO, "${body}")
                                .to("mock:testRiv2Shs");

                        from("direct-vm:shs:rs")
                                .to("mock:direct-vm:shs:rs")
                                .beanRef("shsToCamelConverter")
                                .process(new Processor() {
                                    @Override
                                    public void process(Exchange exchange) throws Exception {
                                        ShsLabel label = exchange.getProperty(ShsHeaders.LABEL, se.inera.axel.shs.xml.label.ShsLabel.class);
                                        ShsLabel replyLabel = new ResponseMessageBuilder().buildReplyLabel(label);
                                        se.inera.axel.shs.xml.label.Product replyProduct = new Product();
                                        replyProduct.setValue(label.getProduct().getValue());
                                        replyLabel.setProduct(replyProduct);
                                        exchange.setProperty(ShsHeaders.LABEL, replyLabel);
                                    }
                                })
                                .transform(constant(
                                        "<ping:PingForConfigurationResponse xmlns:ping=\"urn:riv:itintegration:monitoring:PingForConfigurationResponder:1\">\n"
                                        + "         <ping:version>1.0</ping:version>\n"
                                        + "         <ping:pingDateTime>2013-12-10T10:29:36</ping:pingDateTime>\n"
                                        + "      </ping:PingForConfigurationResponse>"))
                                .beanRef("camelToShsConverter");
                    }
                }};
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        registry.bind("rivShsMapper", rivShsMapper);
        registry.bind("shsToCamelConverter", new DefaultShsMessageToCamelProcessor());
        registry.bind("camelToShsConverter", new DefaultCamelToShsMessageProcessor());
        registry.bind("mySslContext", new SSLContextParameters());

        return registry;
    }
}
