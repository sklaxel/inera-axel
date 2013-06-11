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
package se.inera.axel.shs.broker.rs.internal;

import com.natpryce.makeiteasy.MakeItEasy;
import com.natpryce.makeiteasy.Maker;
import org.apache.camel.*;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.apache.camel.testng.AbstractCamelTestNGSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntryMaker;
import se.inera.axel.shs.broker.routing.ShsRouter;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.ShsLabelMaker;
import se.inera.axel.shs.xml.label.TransferType;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabel;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.to;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.transferType;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.To;

@ContextConfiguration
@MockEndpointsAndSkip("http://shsServer")
public class AsynchronousBrokerRouteBuilderTest extends AbstractCamelTestNGSpringContextTests {

    static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AsynchronousBrokerRouteBuilderTest.class);

    @Autowired
    ShsRouter shsRouter;

    @Autowired
    MessageLogService messageLogService;

    @Produce(context = "shs-broker-asynchronous-test", uri = "direct:in-vm")
    ProducerTemplate camel;


    @DirtiesContext
    @Test
    public void sendingAsynchMessageShouldReturnCorrectHeaders() throws Exception {

        ShsMessageEntry testMessage = make(createMessageEntry());

        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(testMessage);

        System.out.println("label: " + testMessage.getLabel());

        Exchange response = camel.send("direct:in-vm", exchange);

        Assert.assertNotNull(response);

        Message out = response.getOut();

        Assert.assertEquals(out.getMandatoryBody(String.class), testMessage.getLabel().getTxId());
        Assert.assertEquals(out.getHeader(ShsHeaders.X_SHS_TXID), testMessage.getLabel().getTxId());
        Assert.assertEquals(out.getHeader(ShsHeaders.X_SHS_CORRID), testMessage.getLabel().getCorrId());
        Assert.assertEquals(out.getHeader(ShsHeaders.X_SHS_CONTENTID), testMessage.getLabel().getContent().getContentId());
        Assert.assertEquals(out.getHeader(ShsHeaders.X_SHS_DUPLICATEMSG), "no");
        Assert.assertNotNull(out.getHeader(ShsHeaders.X_SHS_LOCALID));
        Assert.assertNotNull(out.getHeader(ShsHeaders.X_SHS_ARRIVALDATE)); // TODO verify format
        Assert.assertNull(out.getHeader(ShsHeaders.X_SHS_ERRORCODE));
    }

    @DirtiesContext
    @Test
    public void sendingAsynchMessageToLocal() throws Exception {

        ShsMessageEntry testMessage = make(createMessageEntryToSelf());

        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(testMessage);

        when(shsRouter.isLocal(any(ShsLabel.class))).thenReturn(true);

        Exchange response = camel.send("direct:in-vm", exchange);

        Assert.assertNotNull(response);

        Message out = response.getOut();
        Assert.assertEquals(out.getMandatoryBody(String.class), testMessage.getLabel().getTxId());

        Thread.sleep(1000);
        verify(messageLogService).messageReceived(any(ShsMessageEntry.class));

    }

    @DirtiesContext
    @Test
    public void sendingAsynchMessageToRemote() throws Exception {

        ShsMessageEntry testMessage = make(createMessageEntry());

        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(testMessage);

        when(shsRouter.isLocal(any(ShsLabel.class))).thenReturn(false);
        //when(shsRouter.resolveEndpoint(any(ShsLabel.class))).thenReturn("http://localhost");

        Exchange response = camel.send("direct:in-vm", exchange);

        Assert.assertNotNull(response);

        Message out = response.getOut();
        Assert.assertEquals(out.getMandatoryBody(String.class), testMessage.getLabel().getTxId());

        Thread.sleep(1000);

        verify(messageLogService).messageSent(any(ShsMessageEntry.class));
    }

    private Maker<ShsMessageEntry> createMessageEntry() {
            return a(ShsMessageEntryMaker.ShsMessageEntry, MakeItEasy.with(ShsMessageEntryMaker.ShsMessageEntryInstantiator.label, a(ShsLabel,
                    with(transferType, TransferType.ASYNCH))));
    }

    private Maker<ShsMessageEntry> createMessageEntryToSelf() {
            return a(ShsMessageEntryMaker.ShsMessageEntry, MakeItEasy.with(ShsMessageEntryMaker.ShsMessageEntryInstantiator.label, a(ShsLabel,
                    with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                    with(transferType, TransferType.ASYNCH))));
    }

}
