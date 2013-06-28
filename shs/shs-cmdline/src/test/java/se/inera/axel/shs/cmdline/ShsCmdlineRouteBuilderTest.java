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
package se.inera.axel.shs.cmdline;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.testng.CamelSpringTestSupport;
import org.junit.BeforeClass;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class ShsCmdlineRouteBuilderTest extends CamelSpringTestSupport {
    @Produce(uri = "direct:listMessages")
    ProducerTemplate listMessages;

    @Produce(uri = "direct:fetchAll")
    ProducerTemplate fetchAll;

    @Produce(uri = "direct:fetch")
    ProducerTemplate fetch;

    @BeforeClass
    public void beforeClass() {
        System.setProperty("outputDir", "target/shscmdline");
    }

    @Test
    @DirtiesContext
    public void testListMessages() throws Exception {
        context.getRouteDefinitions().get(0).adviceWith(context, new DeliveryServiceResponseAdviceWithRouteBuilder());

        context.getRouteDefinition("listMessages").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                mockEndpoints("stream:out");
            }
        });

        context.start();

        getMockEndpoint("mock:dsList")
                .expectedHeaderReceived(Exchange.HTTP_PATH, "urn:X-shs:1111111111");

        getMockEndpoint("mock:dsList")
                .expectedHeaderReceived(Exchange.HTTP_QUERY, "filter=noack");

        getMockEndpoint("mock:stream:out").expectedMessageCount(1);

        Map headers = new HashMap();
        headers.put(ShsCmdlineHeaders.TO_URN, "urn:X-shs:1111111111");
        Map queryParameters = new LinkedHashMap();
        queryParameters.put("filter", "noack");

        headers.put(ShsCmdlineHeaders.QUERY_PARAMS, queryParameters);

        listMessages.sendBodyAndHeaders(null, headers);

        assertMockEndpointsSatisfied(1, TimeUnit.SECONDS);
    }

    @Test
    @DirtiesContext
    public void fetchAllMessages() throws Exception {
        context.getRouteDefinitions().get(0).adviceWith(context, new DeliveryServiceResponseAdviceWithRouteBuilder());

        context.start();

        getMockEndpoint("mock:message1label")
            .expectedMessageCount(1);

        getMockEndpoint("mock:message1dataPart")
            .expectedMessageCount(1);

        getMockEndpoint("mock:message2label")
            .expectedMessageCount(1);

        getMockEndpoint("mock:message2dataPart")
            .expectedMessageCount(1);

        Map headers = new HashMap();
        headers.put(ShsCmdlineHeaders.TO_URN, "urn:X-shs:1111111111");
        Map queryParameters = new LinkedHashMap();
        queryParameters.put("filter", "noack");

        headers.put(ShsCmdlineHeaders.QUERY_PARAMS, queryParameters);

        fetchAll.sendBodyAndHeaders(null, headers);

        assertMockEndpointsSatisfied(1, TimeUnit.SECONDS);
    }

    @Override
    public boolean isUseAdviceWith() {
        return true;
    }

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("classpath:se/inera/axel/shs/cmdline/shs-cmdline-context-test.xml");
    }

    private static class DeliveryServiceResponseProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            String httpPath = exchange.getIn().getHeader(Exchange.HTTP_PATH, String.class);

            String fileName;

            if ("urn:X-shs:1111111111".equals(httpPath)) {
                fileName = "se/inera/axel/shs/cmdline/listMessagesResponse.xml";
            } else if ("urn:X-shs:1111111111/981ead58-b2b1-4373-b3b8-d93e1594f359".equals(httpPath)) {
                fileName = "se/inera/axel/shs/cmdline/981ead58-b2b1-4373-b3b8-d93e1594f359.mime";
            } else if ("urn:X-shs:1111111111/e41a8be7-5e81-46cd-8418-11a250348c29".equals(httpPath)) {
                fileName = "se/inera/axel/shs/cmdline/e41a8be7-5e81-46cd-8418-11a250348c29.mime";
            } else {
                throw new IllegalArgumentException(String.format("Incorrect HTTP_PATH %s", httpPath));
            }

            exchange.getIn().setBody(
                    ClassLoader.getSystemClassLoader().getResourceAsStream(fileName));
        }
    }

    private static class DeliveryServiceResponseAdviceWithRouteBuilder extends AdviceWithRouteBuilder {
        @Override
        public void configure() throws Exception {
            interceptSendToEndpoint("{{shsServerUrlDs}}")
                    .skipSendToOriginalEndpoint()
                    .to("mock:dsList")
                    .bean(new DeliveryServiceResponseProcessor());
        }
    }

    public static class TestRouteBuilder extends RouteBuilder {

        @Override
        public void configure() throws Exception {
            from("file:target/shscmdline?fileName=981ead58-b2b1-4373-b3b8-d93e1594f359-label")
            .to("mock:message1label");

            from("file:target/shscmdline?fileName=981ead58-b2b1-4373-b3b8-d93e1594f359-0")
            .to("mock:message1dataPart");

            from("file:target/shscmdline?fileName=e41a8be7-5e81-46cd-8418-11a250348c29-label")
            .to("mock:message2label");

            from("file:target/shscmdline?fileName=e41a8be7-5e81-46cd-8418-11a250348c29-0")
            .to("mock:message2dataPart");

            from("file:target/shscmdline?fileName=config.properties")
            .to("mock:e41configproperties");
        }
    }
}
