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

import org.apache.camel.*;
import org.apache.camel.component.http.HttpOperationFailedException;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.AbstractCamelTestNGSpringContextTests;
import org.apache.camel.test.AvailablePortFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import se.inera.axel.shs.broker.agreement.AgreementService;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.broker.routing.ShsRouter;
import se.inera.axel.shs.exception.MissingAgreementException;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.*;

import java.util.concurrent.TimeUnit;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import static se.inera.axel.shs.broker.messagestore.ShsMessageEntryMaker.ShsMessageEntry;
import static se.inera.axel.shs.broker.messagestore.ShsMessageEntryMaker.ShsMessageEntryInstantiator.label;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.*;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ProductInstantiator.value;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.*;

@ContextConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//@MockEndpointsAndSkip("http://shsServer")
public class AsynchBrokerRouteBuilderTest extends AbstractCamelTestNGSpringContextTests {

    static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AsynchBrokerRouteBuilderTest.class);

    @Autowired
    ShsRouter shsRouter;

    @Autowired
    AgreementService agreementService;

    @Autowired
    MessageLogService messageLogService;

    @Produce(context = "shs-broker-asynchronous-test", uri = "direct:in-vm-asynch")
    ProducerTemplate camel;


    @EndpointInject(uri = "mock:createdMessages")
    MockEndpoint createdMessagesEndpoint;

    @EndpointInject(uri = "mock:sentMessages")
    MockEndpoint sentMessagesEndpoint;

    @EndpointInject(uri = "mock:end")
    MockEndpoint endEndpoint;

    public AsynchBrokerRouteBuilderTest() {
        if (System.getProperty("shsRsHttpEndpoint.port") == null) {
            int port = AvailablePortFinder.getNextAvailable(9100);
            System.setProperty("shsRsHttpEndpoint.port", Integer.toString(port));
        }
    }

    @Test
    public void sendingAsynchMessageToLocal() throws Exception {

        ShsMessageEntry testMessage = make(createMessageEntryToSelf());

        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(testMessage);

        when(shsRouter.isLocal(any(ShsLabel.class))).thenReturn(true);

        Exchange response = camel.send("direct:in-vm-asynch", exchange);

        assertNotNull(response);

        Message out = response.getOut();
        assertEquals(out.getMandatoryBody(ShsMessageEntry.class).getLabel().getTxId(),
                testMessage.getLabel().getTxId());

        Thread.sleep(5000);
        verify(messageLogService).messageReceived(any(ShsMessageEntry.class));
    }

    @Test
    public void sendingAsynchMessageToRemote() throws Exception {

        final ShsMessageEntry testMessage = make(createMessageEntry());

        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(testMessage);

        when(shsRouter.isLocal(any(ShsLabel.class))).thenReturn(false);

        endEndpoint.expectedMessageCount(1);

        sentMessagesEndpoint.expectedMessageCount(1);
        sentMessagesEndpoint.expectedMessagesMatches(new Predicate() {
            @Override
            public boolean matches(Exchange exchange) {
                Message sentMessage = exchange.getIn();
                ShsMessage sentShsMessage = null;
                try {
                    sentShsMessage = sentMessage.getMandatoryBody(ShsMessage.class);
                } catch (InvalidPayloadException e) {
                    fail(e.getMessage());
                }
                assertEquals(sentShsMessage.getLabel().getCorrId(), testMessage.getLabel().getCorrId());

                return true;
            }
        });

        Exchange response = camel.send("direct:in-vm-asynch", exchange);

        assertNotNull(response);

        Message out = response.getOut();
        assertEquals(out.getMandatoryBody(ShsMessageEntry.class).getLabel().getTxId(),
                testMessage.getLabel().getTxId());


        MockEndpoint.assertIsSatisfied(5, TimeUnit.SECONDS, sentMessagesEndpoint, endEndpoint);

        verify(messageLogService, timeout(30000)).messageSent(any(ShsMessageEntry.class));
    }

    @Test
    public void sendingAsynchOneToMany() throws Exception {

        final ShsMessageEntry testMessage = make(createMessageEntry());
        
        // To = null will enforce product adressing
        // In this case MockConfig for shsRouter.resolveRecipients() is set up to return a list of 2 recipients
        testMessage.getLabel().setTo(null);

        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(testMessage);

        when(shsRouter.isLocal(any(ShsLabel.class))).thenReturn(false);

        createdMessagesEndpoint.expectedMessageCount(2);
        createdMessagesEndpoint.expectedMessagesMatches(new Predicate() {
            @Override
            public boolean matches(Exchange exchange) {
                Message message = exchange.getIn();
                ShsMessage shsMessage = null;
                try {
                    shsMessage = message.getMandatoryBody(ShsMessage.class);
                } catch (InvalidPayloadException e) {
                    fail(e.getMessage());
                }

                assertEquals(shsMessage.getLabel().getCorrId(), testMessage.getLabel().getCorrId());
                assertNotEquals(shsMessage.getLabel().getTxId(), testMessage.getLabel().getTxId());
                assertNotNull(shsMessage.getLabel().getTo());

                return true;
            }
        });

        Exchange response = camel.send("direct:in-vm-asynch", exchange);
        
        assertNotNull(response);

        MockEndpoint.assertIsSatisfied(5, TimeUnit.SECONDS, createdMessagesEndpoint);
    }

    @Test
    public void receivingErrorShouldQuarantineCorrelatedMessages() throws Exception {

    	Product errorProduct = make(a(Product, with(value, ShsLabelMaker.DEFAULT_TEST_PRODUCT_ERROR)));
    	ShsLabel shsLabel = make(a(ShsLabelMaker.ShsLabel,
    			with(sequenceType, SequenceType.ADM),
    			with(product, errorProduct)));
    	ShsMessageEntry shsMessageEntry = make(a(ShsMessageEntry, with(label, shsLabel)));
    	
        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(shsMessageEntry);

        Exchange response = camel.send("direct:in-vm-asynch", exchange);

        assertNotNull(response);

        Message out = response.getOut();
        assertEquals(out.getMandatoryBody(ShsMessageEntry.class).getLabel().getTxId(),
                shsMessageEntry.getLabel().getTxId());

        Thread.sleep(5000);

        verify(messageLogService).quarantineCorrelatedMessages(any(ShsMessage.class));
    }

    @Test
    public void receivingConfirmShouldAcknowledgeCorrelatedMessages() throws Exception {
    	
    	Product confirmProduct = make(a(Product, with(value, ShsLabelMaker.DEFAULT_TEST_PRODUCT_CONFIRM)));
    	ShsLabel shsLabel = make(a(ShsLabel,
    			with(sequenceType, SequenceType.ADM),
    			with(product, confirmProduct)));
    	ShsMessageEntry shsMessageEntry = make(a(ShsMessageEntry, with(label, shsLabel)));

        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(shsMessageEntry);

        Exchange response = camel.send("direct:in-vm-asynch", exchange);

        assertNotNull(response);

        Message out = response.getOut();
        assertEquals(out.getMandatoryBody(ShsMessageEntry.class).getLabel().getTxId(),
                shsMessageEntry.getLabel().getTxId());

        Thread.sleep(5000);

        verify(messageLogService).acknowledgeCorrelatedMessages(any(ShsMessage.class));
    }

    @Test
    public void sendingAsynchMessageWithNoAgreementShouldBeQuarantined() throws Exception {

        final ShsMessageEntry testMessage = make(createMessageEntry());

        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(testMessage);

        doThrow(new MissingAgreementException("no agreement found"))
                .when(agreementService).validateAgreement(any(ShsLabel.class));

        createdMessagesEndpoint.expectedMessageCount(1);
        createdMessagesEndpoint.expectedMessagesMatches(new Predicate() {
            @Override
            public boolean matches(Exchange exchange) {
                Message errorMessage = exchange.getIn();
                ShsMessage errorShsMessage = null;
                try {
                    errorShsMessage = errorMessage.getMandatoryBody(ShsMessage.class);
                } catch (InvalidPayloadException e) {
                    fail(e.getMessage());
                }
                assertEquals(errorShsMessage.getLabel().getCorrId(), testMessage.getLabel().getCorrId());
                assertEquals(errorShsMessage.getLabel().getSequenceType(), SequenceType.ADM);
                assertEquals(errorShsMessage.getLabel().getProduct().getValue(), "error");

                DataPart dataPart = errorShsMessage.getDataParts().get(0);
                assertEquals(dataPart.getContentType(), "text/xml; charset=ISO-8859-1");
                try {
					assertEquals(dataPart.getTransferEncoding(), "binary");
				} catch (Exception e) {
                    fail(e.getMessage());
				}
                
                return true;
            }
        });

        Exchange response = camel.send("direct:in-vm-asynch", exchange);

        assertNotNull(response);

        Message out = response.getOut();
        assertEquals(out.getMandatoryBody(ShsMessageEntry.class).getLabel().getTxId(),
                testMessage.getLabel().getTxId());

        MockEndpoint.assertIsSatisfied(5, TimeUnit.SECONDS, createdMessagesEndpoint);
        verify(messageLogService).messageQuarantined(any(ShsMessageEntry.class), any(MissingAgreementException.class));
    }

    @Test
    public void sendingAsynchMessageWithHttpErrorShouldBeQuarantined() throws Exception {

        final ShsMessageEntry testMessage = make(createMessageEntry());

        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(testMessage);

        when(shsRouter.resolveEndpoint(any(ShsLabel.class)))
                .thenReturn("https://localhost:" + System.getProperty("shsRsHttpEndpoint.port", "7070") + "/err");

        createdMessagesEndpoint.expectedMessageCount(1);
        createdMessagesEndpoint.expectedMessagesMatches(new Predicate() {
            @Override
            public boolean matches(Exchange exchange) {
                Message errorMessage = exchange.getIn();
                ShsMessage errorShsMessage = null;
                try {
                    errorShsMessage = errorMessage.getMandatoryBody(ShsMessage.class);
                } catch (InvalidPayloadException e) {
                    fail(e.getMessage());
                }
                assertEquals(errorShsMessage.getLabel().getCorrId(), testMessage.getLabel().getCorrId());
                assertEquals(errorShsMessage.getLabel().getSequenceType(), SequenceType.ADM);
                assertEquals(errorShsMessage.getLabel().getProduct().getValue(), "error");

                return true;
            }
        });

        Exchange response = camel.send("direct:in-vm-asynch", exchange);

        assertNotNull(response);

        Message out = response.getOut();
        assertEquals(out.getMandatoryBody(ShsMessageEntry.class).getLabel().getTxId(),
                testMessage.getLabel().getTxId());

        MockEndpoint.assertIsSatisfied(5, TimeUnit.SECONDS, createdMessagesEndpoint);

        verify(messageLogService).messageQuarantined(any(ShsMessageEntry.class), any(HttpOperationFailedException.class));
    }

    @Test
    public void sendingErrorMessageWithHttpErrorShouldNotGenerateNewErrorMessage() throws Exception {

        final ShsMessageEntry testMessage = make(createErrorMessageEntry());


        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(testMessage);

        when(shsRouter.resolveEndpoint(any(ShsLabel.class)))
                .thenReturn("https://localhost:" + System.getProperty("shsRsHttpEndpoint.port", "7070") + "/err");

        createdMessagesEndpoint.expectedMessageCount(0);
        createdMessagesEndpoint.setAssertPeriod(2000);

        Exchange response = camel.send("direct:in-vm-asynch", exchange);

        assertNotNull(response);

        Message out = response.getOut();
        assertEquals(out.getMandatoryBody(ShsMessageEntry.class).getLabel().getTxId(),
                testMessage.getLabel().getTxId());

        MockEndpoint.assertIsSatisfied(5, TimeUnit.SECONDS, createdMessagesEndpoint);

        verify(messageLogService).messageQuarantined(any(ShsMessageEntry.class), any(HttpOperationFailedException.class));
    }

    @Test
    public void sendingConfirmMessageWithHttpErrorShouldNotGenerateNewErrorMessage() throws Exception {

        final ShsMessageEntry testMessage = make(createConfirmMessageEntry());


        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(testMessage);

        when(shsRouter.resolveEndpoint(any(ShsLabel.class)))
                .thenReturn("https://localhost:" + System.getProperty("shsRsHttpEndpoint.port", "7070") + "/err");

        createdMessagesEndpoint.expectedMessageCount(0);
        createdMessagesEndpoint.setAssertPeriod(2000);

        Exchange response = camel.send("direct:in-vm-asynch", exchange);

        assertNotNull(response);

        Message out = response.getOut();
        assertEquals(out.getMandatoryBody(ShsMessageEntry.class).getLabel().getTxId(),
                testMessage.getLabel().getTxId());

        MockEndpoint.assertIsSatisfied(5, TimeUnit.SECONDS, createdMessagesEndpoint);

        verify(messageLogService).messageQuarantined(any(ShsMessageEntry.class), any(HttpOperationFailedException.class));
    }

    private Maker<ShsMessageEntry> createMessageEntry() {
            return a(ShsMessageEntry, with(label, a(ShsLabel,
                    with(transferType, TransferType.ASYNCH))));
    }

    private Maker<ShsMessageEntry> createErrorMessageEntry() {
            return a(ShsMessageEntry, with(label, a(ShsLabel,
                    with(sequenceType, SequenceType.ADM),
                    with(transferType, TransferType.ASYNCH),
                    with(product, a(Product, with(value, "error"))))));
    }

    private Maker<ShsMessageEntry> createConfirmMessageEntry() {
           return a(ShsMessageEntry, with(label, a(ShsLabel,
                   with(sequenceType, SequenceType.ADM),
                   with(transferType, TransferType.ASYNCH),
                   with(product, a(Product, with(value, "confirm"))))));
    }

    private Maker<ShsMessageEntry> createMessageEntryToSelf() {
            return a(ShsMessageEntry, with(label, a(ShsLabel,
                    with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                    with(transferType, TransferType.ASYNCH))));
    }
}
