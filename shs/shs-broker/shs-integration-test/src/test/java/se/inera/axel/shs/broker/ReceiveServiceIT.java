package se.inera.axel.shs.broker;

import com.natpryce.makeiteasy.Donor;
import com.natpryce.makeiteasy.Maker;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.test.spring.CamelSpringTestContextLoader;
import org.apache.camel.test.spring.CamelSpringTestHelper;
import org.apache.camel.testng.AbstractCamelTestNGSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.MergedContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.agreement.AgreementAdminService;
import se.inera.axel.shs.broker.agreement.mongo.AgreementAssembler;
import se.inera.axel.shs.broker.directory.Address;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.directory.InMemoryDirectoryServerInitializer;
import se.inera.axel.shs.cmdline.ShsCmdline;
import se.inera.axel.shs.mime.TransferEncoding;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.agreement.ShsAgreementMaker;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.TransferType;
import se.inera.axel.test.DynamicProperties;
import se.inera.axel.test.EmbeddedMongoDbInitializer;

import javax.annotation.Resource;
import javax.xml.transform.OutputKeys;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.*;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ProductInstantiator.value;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsAgreementInstantiator.shs;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsAgreementInstantiator.uuid;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsInstantiator.customer;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsInstantiator.products;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
@ContextConfiguration(loader = ReceiveServiceIT.ReceiveServiceITContextLoader.class, initializers = {
        InMemoryDirectoryServerInitializer.class
        , EmbeddedMongoDbInitializer.class})
public class ReceiveServiceIT extends AbstractCamelTestNGSpringContextTests {
    @Produce(uri = "direct:rs", context = "shs-integration-test")
    ProducerTemplate producerTemplate;

    @Autowired
    private Environment environment;

    @Autowired
    private AgreementAdminService agreementAdminService;

    @Autowired
    private AgreementAssembler agreementAssembler;

    @Autowired
    private DirectoryAdminServiceRegistry directoryAdminServiceRegistry;

    @EndpointInject(uri = "mock:remoteShsBroker")
    private MockEndpoint mockRemoteShsBroker;

    @EndpointInject(uri = "mock:receiveServiceReply")
    private MockEndpoint mockReceiveServiceReply;

    @EndpointInject(uri = "mock:shsPing")
    private MockEndpoint mockShsPing;

    @Resource(name = "shs-integration-test")
    CamelContext camelContext;


    @BeforeMethod
    public void setUp() throws Exception {
        MockEndpoint.resetMocks(camelContext);

        String shsRsHttpEndpoint = environment.getProperty("shsRsHttpEndpoint")
                                   + environment.getProperty("shsRsPathPrefix");
        if (shsRsHttpEndpoint.indexOf("jetty://") > 0) {
            shsRsHttpEndpoint = shsRsHttpEndpoint.substring(shsRsHttpEndpoint.indexOf("jetty://"));
        }

        System.setProperty("shsServerUrl", shsRsHttpEndpoint);

        Maker<se.inera.axel.shs.xml.agreement.ShsAgreement> shsAgreementMaker = a(ShsAgreement,
                with(uuid, new Donor<String>() {
                    @Override
                    public String value() {
                        return UUID.randomUUID().toString();
                    }
                }),
                with(shs, a(Shs,
                        with(customer, a(Customer, with(ShsAgreementMaker.Customer.value, "0000000000"))),
                        with(products, listOf(a(Product, with(value, "00000000-0000-0000-0000-000000000000"))))))
        );

        agreementAdminService.save(make(shsAgreementMaker));

        agreementAdminService.save(make(shsAgreementMaker.but(with(shs, a(Shs,
                with(customer, a(Customer, with(ShsAgreementMaker.Customer.value, "1111111111"))),
                with(products, listOf(a(Product, with(value, "00000000-0000-0000-0000-000000000000"))))
        )))));

        DirectoryAdminService directoryAdminService =
                directoryAdminServiceRegistry.getDirectoryAdminService(directoryAdminServiceRegistry.getServerNames().get(0));

        Address address = directoryAdminService.getAddress("1111111111", "00000000-0000-0000-0000-000000000000");
        address.setDeliveryMethods("http4://localhost:" + DynamicProperties.get().get("remoteShsBrokerPort") + "/shs/rs");

        directoryAdminService.saveAddress(
                directoryAdminService.getOrganization("1111111111"),
                address);
    }

    @Test
    public void synchronousLocalPingShsCmdline() throws Throwable {
        String[] args = {"request",
                "-f", "0000000000",
                "-t", "0000000000",
                "-p", "00000000-0000-0000-0000-000000000000",
                "--fileName", "test.txt"
        };

        String data = "PING";
        InputStream testInput = new ByteArrayInputStream( data.getBytes("UTF-8") );
        InputStream old = System.in;
        try {
            System.setIn( testInput );

            ShsCmdline.main(args);

        } finally {
            System.setIn( old );
        }
    }

    @Test
    public void synchronousLocalPing() throws Throwable {
        mockShsPing.expectedMessageCount(1);
        mockReceiveServiceReply.expectedMessageCount(1);
        HashMap<String, Object> headers = new HashMap<>();
        headers.put(ShsHeaders.PRODUCT_ID, "00000000-0000-0000-0000-000000000000");
        headers.put(ShsHeaders.TO, "0000000000");
        producerTemplate.requestBodyAndHeaders("PING!", headers);

        MockEndpoint.assertIsSatisfied(mockShsPing, mockReceiveServiceReply);
    }

    @Test
    public void synchronousRemotePing() throws InterruptedException {
        mockReceiveServiceReply.expectedMessageCount(1);
        mockRemoteShsBroker.expectedMessageCount(1);

        HashMap<String, Object> headers = new HashMap<>();
        headers.put(ShsHeaders.PRODUCT_ID, "00000000-0000-0000-0000-000000000000");
        headers.put(ShsHeaders.TO, "1111111111");
        producerTemplate.requestBodyAndHeaders("PING!", headers);

        MockEndpoint.assertIsSatisfied(mockRemoteShsBroker, mockReceiveServiceReply);
    }

    public static final class TestRouteBuilder extends RouteBuilder {

        @Override
        public void configure() throws Exception {
            from("direct:rs").id("receiveService")
            .setHeader(ShsHeaders.FROM, constant("0000000000"))
            .setHeader(ShsHeaders.SEQUENCETYPE, constant(SequenceType.REQUEST))
            .setHeader(ShsHeaders.TRANSFERTYPE, constant(TransferType.SYNCH))
            .setHeader(ShsHeaders.DATAPART_TYPE, constant("txt"))
            .setHeader(ShsHeaders.DATAPART_FILENAME, simple("test.txt"))
            .setHeader(ShsHeaders.DATAPART_CONTENTTYPE, constant("application/text"))
            .setHeader(ShsHeaders.DATAPART_TRANSFERENCODING, constant(TransferEncoding.BASE64))
            .beanRef("defaultCamelToShsMessageProcessor")
            .to("ref:axel-rs")
            .to("bean:shs2camelConverter")
            .convertBodyTo(String.class)
            .to("stream:out")
            .to("mock:receiveServiceReply");

            from("jetty://http://0.0.0.0:{{remoteShsBrokerPort}}/shs/rs").id("remoteShsBroker")
            .to("bean:shs2camelConverter")
            .to("mock:remoteShsBroker")
            .to("bean:defaultCamelToShsMessageProcessor");
        }
    }

    /**
     * CamelSpringTestContextLoader does not prepare the context with the
     * configured initializers so we have to override so that the contexts are prepared
     * before they are started.
     */
    public static final class ReceiveServiceITContextLoader extends CamelSpringTestContextLoader {
        private MergedContextConfiguration mergedContextConfiguration;

        /**
         * Override to save the MergedContextConfiguration in order to give it to prepareContext
         * @param mergedConfig
         * @return
         * @throws Exception
         */
        @Override
        public ApplicationContext loadContext(MergedContextConfiguration mergedConfig) throws Exception {
            mergedContextConfiguration = mergedConfig;
            return super.loadContext(mergedConfig);
        }

        /**
         * Call prepareContext to prepare it with the ApplicationContextInitializers
         * @param testClass
         * @return
         */
        @Override
        protected GenericApplicationContext createContext(Class<?> testClass) {
            GenericApplicationContext context = super.createContext(testClass);
            prepareContext(context, mergedContextConfiguration);
            return context;
        }

        @Override
        protected void handleCamelContextStartup(GenericApplicationContext context, Class<?> testClass) throws Exception {
            addOverridePropertiesToPropertiesComponent(context);
            super.handleCamelContextStartup(context, testClass);
        }

        private void addOverridePropertiesToPropertiesComponent(GenericApplicationContext context) throws Exception {
            CamelSpringTestHelper.doToSpringCamelContexts(context, new CamelSpringTestHelper.DoToSpringCamelContextsStrategy() {
                @Override
                public void execute(String contextName, SpringCamelContext camelContext) throws Exception {
                    PropertiesComponent pc = camelContext.getComponent("properties", PropertiesComponent.class);
                    Properties extra = new Properties();
                    for (Map.Entry<String, Object> propertyEntry : DynamicProperties.get().entrySet()) {
                        if (propertyEntry.getValue() instanceof String) {
                            extra.setProperty(propertyEntry.getKey(), (String)propertyEntry.getValue());
                        }
                    }

                    if (extra != null && !extra.isEmpty()) {
                        pc.setOverrideProperties(extra);
                    }
                }
            });
        }
    }
}
