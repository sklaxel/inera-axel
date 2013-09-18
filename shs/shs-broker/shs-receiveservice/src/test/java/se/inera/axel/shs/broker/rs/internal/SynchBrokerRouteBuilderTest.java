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
package se.inera.axel.shs.broker.rs.internal;

import com.natpryce.makeiteasy.Maker;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.apache.camel.testng.AbstractCamelTestNGSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.directory.DirectoryService;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.broker.routing.ShsRouter;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.ShsLabelMaker;
import se.inera.axel.shs.xml.label.TransferType;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.apache.camel.builder.SimpleBuilder.simple;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessage;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessageInstantiator.label;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabel;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.to;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.transferType;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.To;

@ContextConfiguration
@MockEndpointsAndSkip("http:shsServer|shs:local")
public class SynchBrokerRouteBuilderTest extends AbstractCamelTestNGSpringContextTests {

    static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SynchBrokerRouteBuilderTest.class);

    public SynchBrokerRouteBuilderTest() {
    }

    @Autowired
    ShsRouter shsRouter;

    @Autowired
    MessageLogService messageLogService;

    @Autowired
    DirectoryService directoryService;

    @Produce(context = "shs-receiveservice")
    ProducerTemplate camel;

    @EndpointInject(uri = "mock:http:shsServer")
    MockEndpoint shsServerEndpoint;

    @EndpointInject(uri = "mock:shs:local")
    MockEndpoint shsLocalEndpoint;

    @DirtiesContext
    @Test
    public void sendingSynchRequestWithLocalReceiver() throws Exception {
        shsLocalEndpoint.expectedMessageCount(1);

        ShsMessage testMessage = make(createSynchMessageWithLocalReceiver());
        ShsMessageEntry entry = messageLogService.saveMessage(testMessage);

        when(shsRouter.isLocal(any(se.inera.axel.shs.xml.label.ShsLabel.class))).thenReturn(true);

        String response = camel.requestBody("direct-vm:shs:synch", entry, String.class);

        shsLocalEndpoint.assertIsSatisfied();
    }

    @DirtiesContext
    @Test
    public void sendingSynchRequestWithKnownReceiver() throws Exception {
        shsServerEndpoint.expectedMessageCount(1);
        shsServerEndpoint.expectedMessagesMatches(simple("${body.dataParts[0]?.dataHandler.content} == '" + ShsLabelMaker.DEFAULT_TEST_BODY + "'"));

        ShsMessage testMessage = make(createSynchMessageWithKnownReceiver());
        ShsMessageEntry entry = messageLogService.saveMessage(testMessage);

        String response = camel.requestBody("direct-vm:shs:synch", entry, String.class);

        shsServerEndpoint.assertIsSatisfied();
    }


//    @DirtiesContext
//    @Test
//    public void testSimulateErrorUsingInterceptors() throws Exception {
//        // first find the route we need to advice. Since we only have one route
//        // then just grab the first from the list
//        RouteDefinition route = context.getRouteDefinitions().get(0);
//
//        // advice the route by enriching it with the route builder where
//        // we add a couple of interceptors to help simulate the error
//        route.adviceWith(context, new RouteBuilder() {
//            @Override
//            public void configure() throws Exception {
//                // intercept sending to http and detour to our processor instead
//                interceptSendToEndpoint("http://*")
//                    // skip sending to the real http when the detour ends
//                    .skipSendToOriginalEndpoint()
//                    .process(new SimulateHttpErrorProcessor());
//
//                // intercept sending to ftp and detour to the mock instead
//                interceptSendToEndpoint("ftp://*")
//                    // skip sending to the real ftp endpoint
//                    .skipSendToOriginalEndpoint()
//                    .to("mock:ftp");
//            }
//        });
//
//        // our mock should receive the message
//        MockEndpoint mock = getMockEndpoint("mock:ftp");
//        mock.expectedBodiesReceived("Camel rocks");
//
//        // start the test by creating a file that gets picked up by the route
//        template.sendBodyAndHeader(file, "Camel rocks", Exchange.FILE_NAME, "hello.txt");
//
//        // assert our test passes
//        assertMockEndpointsSatisfied();
//    }

//    private class SimulateHttpErrorProcessor implements Processor {
//
//        public void process(Exchange exchange) throws Exception {
//            // simulate the error by thrown the exception
//            throw new ConnectException("Simulated connection error");
//        }
//
//    }

    private Maker<ShsMessage> createSynchMessageWithLocalReceiver() {
        return a(ShsMessage,
                with(label, a(ShsLabel,
                        with(to, a(To,
                                with(ShsLabelMaker.ToInstantiator.value, "0000000000"))),
                        with(transferType, TransferType.SYNCH))));
    }

    private Maker<ShsMessage> createSynchMessageWithKnownReceiver() {

        return a(ShsMessage,
                with(label, a(ShsLabel,
                        with(transferType, TransferType.SYNCH),
                        with(to, a(To,
                                with(ShsLabelMaker.ToInstantiator.value, "1111111111"))))));
    }


}
