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

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabel;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.To;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.to;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.transferType;

import java.util.List;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.http.HttpOperationFailedException;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.AbstractCamelTestNGSpringContextTests;
import org.apache.camel.testng.AvailablePortFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import se.inera.axel.shs.broker.agreement.AgreementService;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntryMaker;
import se.inera.axel.shs.broker.routing.ShsRouter;
import se.inera.axel.shs.exception.MissingAgreementException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.Product;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.ShsLabelMaker;
import se.inera.axel.shs.xml.label.TransferType;

import com.natpryce.makeiteasy.MakeItEasy;
import com.natpryce.makeiteasy.Maker;

@ContextConfiguration
//@MockEndpointsAndSkip("http://shsServer")
public class AsynchBrokerRouteBuilderTest extends AbstractCamelTestNGSpringContextTests {

    static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AsynchBrokerRouteBuilderTest.class);

    @Autowired
    ShsRouter shsRouter;

    @Autowired
    AgreementService agreementService;

    @Autowired
    MessageLogService messageLogService;

    @Produce(context = "shs-broker-asynchronous-test", uri = "direct:in-vm")
    ProducerTemplate camel;


    @EndpointInject(uri = "mock:createdMessages")
    MockEndpoint createdMessagesEndpoint;

    @EndpointInject(uri = "mock:sentMessages")
    MockEndpoint sentMessagesEndpoint;

    public AsynchBrokerRouteBuilderTest() {
        if (System.getProperty("shsRsHttpEndpoint.port") == null) {
            int port = AvailablePortFinder.getNextAvailable(9100);
            System.setProperty("shsRsHttpEndpoint.port", Integer.toString(port));
        }
    }

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


        Exchange response = camel.send("direct:in-vm", exchange);

        Assert.assertNotNull(response);

        Message out = response.getOut();
        Assert.assertEquals(out.getMandatoryBody(String.class), testMessage.getLabel().getTxId());

        Thread.sleep(1000);

        verify(messageLogService).messageSent(any(ShsMessageEntry.class));
        Exchange sentExchange = sentMessagesEndpoint.assertExchangeReceived(0);
        Message sentMessage = sentExchange.getIn();
        ShsMessage sentShsMessage = sentMessage.getMandatoryBody(ShsMessage.class);
        Assert.assertEquals(sentShsMessage.getLabel().getCorrId(), testMessage.getLabel().getCorrId());
    }

    @DirtiesContext
    @Test
    public void sendingAsynchOneToMany() throws Exception {

        ShsMessageEntry testMessage = make(createMessageEntry());
        
        // To = null will enforce product adressing
        // In this case MockConfig for shsRouter.resolveRecipients() is set up to return a list of 2 recipients
        testMessage.getLabel().setTo(null);

        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(testMessage);

        when(shsRouter.isLocal(any(ShsLabel.class))).thenReturn(false);

        Exchange response = camel.send("direct:in-vm", exchange);
        
        Thread.sleep(1000);

        Assert.assertNotNull(response);

        // Verifications
		Assert.assertEquals(createdMessagesEndpoint.getReceivedCounter(), 2);
		List<Exchange> receivedExchanges = createdMessagesEndpoint.getExchanges();
		for (Exchange receivedExchange : receivedExchanges) {
			Message message = receivedExchange.getIn();
			ShsMessage shsMessage = message.getMandatoryBody(ShsMessage.class);
			
			Assert.assertEquals(shsMessage.getLabel().getCorrId(), testMessage.getLabel().getCorrId());
			Assert.assertNotEquals(shsMessage.getLabel().getTxId(), testMessage.getLabel().getTxId());
			Assert.assertNotNull(shsMessage.getLabel().getTo());
		}
    }

    @DirtiesContext
    @Test
    public void receivingErrorShouldQuarantineCorrelatedMessages() throws Exception {

    	Product product = make(a(ShsLabelMaker.Product, with(ShsLabelMaker.Product.value, ShsLabelMaker.DEFAULT_TEST_PRODUCT_ERROR)));
    	ShsLabel label = make(a(ShsLabelMaker.ShsLabel,
    			with(ShsLabelMaker.ShsLabel.sequenceType, SequenceType.ADM),
    			with(ShsLabelMaker.ShsLabel.product, product)));
    	ShsMessageEntry shsMessageEntry = make(a(ShsMessageEntryMaker.ShsMessageEntry, with(ShsMessageEntryMaker.ShsMessageEntryInstantiator.label, label)));
    	
        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(shsMessageEntry);

        Exchange response = camel.send("direct:in-vm", exchange);

        Assert.assertNotNull(response);

        Message out = response.getOut();
        Assert.assertEquals(out.getMandatoryBody(String.class),
                shsMessageEntry.getLabel().getTxId());

        Thread.sleep(1000);

        verify(messageLogService).quarantineCorrelatedMessages(any(ShsMessage.class));
    }

    @DirtiesContext
    @Test
    public void receivingConfirmShouldAcknowledgeCorrelatedMessages() throws Exception {
    	
    	Product product = make(a(ShsLabelMaker.Product, with(ShsLabelMaker.Product.value, ShsLabelMaker.DEFAULT_TEST_PRODUCT_CONFIRM)));
    	ShsLabel label = make(a(ShsLabelMaker.ShsLabel,
    			with(ShsLabelMaker.ShsLabel.sequenceType, SequenceType.ADM),
    			with(ShsLabelMaker.ShsLabel.product, product)));
    	ShsMessageEntry shsMessageEntry = make(a(ShsMessageEntryMaker.ShsMessageEntry, with(ShsMessageEntryMaker.ShsMessageEntryInstantiator.label, label)));

        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(shsMessageEntry);

        Exchange response = camel.send("direct:in-vm", exchange);

        Assert.assertNotNull(response);

        Message out = response.getOut();
        Assert.assertEquals(out.getMandatoryBody(String.class),
                shsMessageEntry.getLabel().getTxId());

        Thread.sleep(1000);

        verify(messageLogService).acknowledgeCorrelatedMessages(any(ShsMessage.class));
    }

    @DirtiesContext
    @Test
    public void sendingAsynchMessageWithNoAgreementShouldBeQuarantined() throws Exception {

        ShsMessageEntry testMessage = make(createMessageEntry());

        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(testMessage);

        doThrow(new MissingAgreementException("no agreement found"))
                .when(agreementService).validateAgreement(any(ShsLabel.class));

        Exchange response = camel.send("direct:in-vm", exchange);

        Assert.assertNotNull(response);

        Message out = response.getOut();
        Assert.assertEquals(out.getMandatoryBody(String.class),
                testMessage.getLabel().getTxId());

        Thread.sleep(1000);

        verify(messageLogService).messageQuarantined(any(ShsMessageEntry.class), any(MissingAgreementException.class));

        Exchange errorExchange = createdMessagesEndpoint.assertExchangeReceived(0);
        Message errorMessage = errorExchange.getIn();
        ShsMessage errorShsMessage = errorMessage.getMandatoryBody(ShsMessage.class);
        Assert.assertEquals(errorShsMessage.getLabel().getCorrId(), testMessage.getLabel().getCorrId());
        Assert.assertEquals(errorShsMessage.getLabel().getSequenceType(), SequenceType.ADM);
        Assert.assertEquals(errorShsMessage.getLabel().getProduct().getValue(), "error");
    }

    @DirtiesContext
    @Test
    public void sendingAsynchMessageWithHttpErrorShouldBeQuarantined() throws Exception {

        ShsMessageEntry testMessage = make(createMessageEntry());

        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(testMessage);

        when(shsRouter.resolveEndpoint(any(ShsLabel.class)))
                .thenReturn("http://localhost:" + System.getProperty("shsRsHttpEndpoint.port", "7070") + "/err");

        Exchange response = camel.send("direct:in-vm", exchange);

        Assert.assertNotNull(response);

        Message out = response.getOut();
        Assert.assertEquals(out.getMandatoryBody(String.class),
                testMessage.getLabel().getTxId());

        Thread.sleep(1000);

        verify(messageLogService).messageQuarantined(any(ShsMessageEntry.class), any(HttpOperationFailedException.class));

        Exchange errorExchange = createdMessagesEndpoint.assertExchangeReceived(0);
        Message errorMessage = errorExchange.getIn();
        ShsMessage errorShsMessage = errorMessage.getMandatoryBody(ShsMessage.class);
        Assert.assertEquals(errorShsMessage.getLabel().getCorrId(), testMessage.getLabel().getCorrId());
        Assert.assertEquals(errorShsMessage.getLabel().getSequenceType(), SequenceType.ADM);
        Assert.assertEquals(errorShsMessage.getLabel().getProduct().getValue(), "error");
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
