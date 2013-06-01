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
package se.inera.axel.shs.broker.internal;

import com.natpryce.makeiteasy.Maker;
import org.apache.camel.*;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.AvailablePortFinder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.inera.axel.shs.exception.UnknownReceiverException;
import se.inera.axel.shs.messagestore.MessageLogService;
import se.inera.axel.shs.messagestore.ShsMessageEntry;
import se.inera.axel.shs.protocol.ShsHeaders;
import se.inera.axel.shs.protocol.ShsMessage;
import se.inera.axel.shs.xml.label.TransferType;

import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static se.inera.axel.shs.protocol.ShsMessageMaker.ShsMessage;
import static se.inera.axel.shs.protocol.ShsMessageMaker.ShsMessageInstantiator.label;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabel;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.to;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.transferType;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.To;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ToInstantiator.value;

@ContextConfiguration
public class ServerRouteBuilderTest extends AbstractTestNGSpringContextTests {

    static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ServerRouteBuilderTest.class);

    public ServerRouteBuilderTest() {
        if (System.getProperty("shsRsHttpEndpoint.port") == null) {
            int port = AvailablePortFinder.getNextAvailable(9100);
            System.setProperty("shsRsHttpEndpoint.port", Integer.toString(port));
        }
    }

    @Autowired
    MessageLogService messageLogService;

    @Produce(context = "shs-broker-main-test", uri = "direct:in-vm")
    ProducerTemplate camel;

    @EndpointInject(uri = "mock:synchron")
    MockEndpoint synchronEndpoint;

    @EndpointInject(uri = "mock:asynchron")
    MockEndpoint asynchronEndpoint;

    @BeforeMethod
    public void beforeMethod() {
        given(messageLogService.createEntry(any(ShsMessage.class)))
                .willAnswer(new Answer<ShsMessageEntry>() {
                    @Override
                    public ShsMessageEntry answer(InvocationOnMock invocation) throws Throwable {
                        return ShsMessageEntry.createNewEntry(((ShsMessage) invocation.getArguments()[0]).getLabel());
                    }
                });
    }

    @DirtiesContext
    @Test
    public void sendingSynchRequestWithKnownReceiverInVmShouldWork() throws Exception {
        synchronEndpoint.expectedMessageCount(1);

        ShsMessage testMessage = make(createSynchMessageWithKnownReceiver());

        String response = camel.requestBody("direct:in-vm", testMessage, String.class);

        Assert.assertNotNull(response);

        MockEndpoint.assertIsSatisfied(synchronEndpoint);
    }

    @DirtiesContext
    @Test
    public void sendingSynchRequestWithUnknownReceiverInVmShouldThrow() throws Exception {
        synchronEndpoint.expectedMessageCount(1);

        try {
            ShsMessage testMessage = make(createSynchMessageWithUnknownReceiver());
            String response = camel.requestBody("direct:in-vm", testMessage, String.class);

            Assert.fail("request should throw exception");
        } catch (Exception e) {
            Throwable cause = e.getCause();
            Assert.assertNotNull(cause);
            Assert.assertTrue(cause instanceof UnknownReceiverException, "exception should be 'UnknownReceiverException'");
        }

    }

    @DirtiesContext
    @Test
    public void sendingAsynchRequestInVmShouldWork() throws Exception {
        asynchronEndpoint.expectedMessageCount(1);

        ShsMessage testMessage = make(createAsynchMessageWithKnownReceiver());

        String response = camel.requestBody("direct:in-vm", testMessage, String.class);

        Assert.assertNotNull(response);

        MockEndpoint.assertIsSatisfied(asynchronEndpoint);

        List<Exchange> exchanges = asynchronEndpoint.getReceivedExchanges();
        ShsMessageEntry entry = exchanges.get(0).getIn().getMandatoryBody(ShsMessageEntry.class);
        Assert.assertNotNull(entry);
    }

    @DirtiesContext
    @Test
    public void sendingAsynchRequestShouldReturnCorrectHeaders() throws Exception {
        asynchronEndpoint.expectedMessageCount(1);

        ShsMessage testMessage = make(createAsynchMessageWithKnownReceiver());

        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(testMessage);
        Exchange response = camel.send("direct:in-vm", exchange);

        Assert.assertNotNull(response);

        Message out = response.getOut();
        Assert.assertEquals(out.getHeader(ShsHeaders.X_SHS_TXID), testMessage.getLabel().getTxId());
        Assert.assertEquals(out.getHeader(ShsHeaders.X_SHS_CORRID), testMessage.getLabel().getCorrId());
        Assert.assertEquals(out.getHeader(ShsHeaders.X_SHS_CONTENTID), testMessage.getLabel().getContent().getContentId());
        Assert.assertEquals(out.getHeader(ShsHeaders.X_SHS_DUPLICATEMSG), "no");
        Assert.assertNotNull(out.getHeader(ShsHeaders.X_SHS_LOCALID));
        Assert.assertNotNull(out.getHeader(ShsHeaders.X_SHS_ARRIVALDATE)); // TODO verify format
        Assert.assertNull(out.getHeader(ShsHeaders.X_SHS_ERRORCODE));
    }

    private Maker<ShsMessage> createAsynchMessageWithKnownReceiver() {
            return a(ShsMessage,
                    with(label, a(ShsLabel,
                            with(transferType, TransferType.ASYNCH))));
        }

    private Maker<ShsMessage> createSynchMessageWithKnownReceiver() {
        return a(ShsMessage,
                with(label, a(ShsLabel,
                        with(transferType, TransferType.SYNCH))));
    }

    private Maker<ShsMessage> createSynchMessageWithUnknownReceiver() {

        return a(ShsMessage,
                with(label, a(ShsLabel,
                        with(transferType, TransferType.SYNCH),
                        with(to, a(To,
                                with(value, "1111111111"))))));
    }


}
