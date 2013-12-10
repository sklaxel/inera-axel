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
import static org.apache.camel.builder.SimpleBuilder.simple;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessage;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessageInstantiator.label;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabel;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.To;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.sequenceType;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.to;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.transferType;

import java.io.InputStream;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.apache.camel.testng.AbstractCamelTestNGSpringContextTests;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import se.inera.axel.shs.broker.agreement.AgreementService;
import se.inera.axel.shs.broker.directory.DirectoryService;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.broker.routing.ShsRouter;
import se.inera.axel.shs.exception.MissingAgreementException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.ShsLabelMaker;
import se.inera.axel.shs.xml.label.TransferType;

import com.natpryce.makeiteasy.Maker;

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

    @Autowired
    AgreementService agreementService;
    
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
    
        // Make the MockEndpoint return a new ShsMessage object, because otherwise the same object will 
        // flow through the whole route builder and then the verify for REQUEST will not work
        // due to being overwritten by REPLY
        final ShsMessage shsMessageReply = make(createSynchReply());
        Expression expression = new Expression() {
			
			@Override
			public <T> T evaluate(Exchange arg0, Class<T> arg1) {
				T reply = arg0.getContext().getTypeConverter().convertTo(arg1,
						shsMessageReply);
				return reply;
			}
		};
		shsLocalEndpoint.returnReplyBody(expression);

		final ShsMessage shsMessageRequest = make(createSynchMessageWithLocalReceiver());
        ShsMessageEntry shsMessageEntryRequest = messageLogService.saveMessage(shsMessageRequest);
        
        when(shsRouter.isLocal(any(se.inera.axel.shs.xml.label.ShsLabel.class))).thenReturn(true);

        ShsMessageEntry shsMessageEntryReply = messageLogService.saveMessage(shsMessageReply);
        when(messageLogService.saveMessageStream(any(InputStream.class))).thenReturn(shsMessageEntryReply);

        String response = camel.requestBody("direct-vm:shs:synch", shsMessageEntryRequest, String.class);
        Assert.assertNotNull(response);

        verify(messageLogService).messageSent(any(ShsMessageEntry.class));
        verify(messageLogService).messageReceived(any(ShsMessageEntry.class));
        
        ArgumentCaptor<ShsMessageEntry> argument = ArgumentCaptor.forClass(ShsMessageEntry.class);
        verify(messageLogService).messageSent(argument.capture());
        Assert.assertEquals(argument.getValue().getLabel().getSequenceType(), SequenceType.REQUEST);
        
        verify(messageLogService).messageReceived(argument.capture());
        Assert.assertEquals(argument.getValue().getLabel().getSequenceType(), SequenceType.REPLY);

        shsLocalEndpoint.assertIsSatisfied();
    }

    @DirtiesContext
    @Test
    public void sendingSynchRequestWithKnownReceiver() throws Exception {
        shsServerEndpoint.expectedMessageCount(1);
        shsServerEndpoint.expectedMessagesMatches(simple("${body.dataParts[0]?.dataHandler.content} == '" + ShsLabelMaker.DEFAULT_TEST_BODY + "'"));

        // Make the MockEndpoint return a new ShsMessage object, because otherwise the same object will 
        // flow through the whole route builder and then the verify for REQUEST will not work
        // due to being overwritten by REPLY
        final ShsMessage shsMessageReply = make(createSynchReply());
        Expression expression = new Expression() {
			
			@Override
			public <T> T evaluate(Exchange arg0, Class<T> arg1) {
				T reply = arg0.getContext().getTypeConverter().convertTo(arg1,
						shsMessageReply);
				return reply;
			}
		};
		shsServerEndpoint.returnReplyBody(expression);


		ShsMessage testMessage = make(createSynchMessageWithKnownReceiver());
        ShsMessageEntry entry = messageLogService.saveMessage(testMessage);

        when(shsRouter.isLocal(any(se.inera.axel.shs.xml.label.ShsLabel.class))).thenReturn(false);

        ShsMessageEntry shsMessageEntryReply = messageLogService.saveMessage(shsMessageReply);
        when(messageLogService.saveMessageStream(any(InputStream.class))).thenReturn(shsMessageEntryReply);

        String response = camel.requestBody("direct-vm:shs:synch", entry, String.class);
        Assert.assertNotNull(response);

        verify(messageLogService).messageSent(any(ShsMessageEntry.class));
        verify(messageLogService).messageReceived(any(ShsMessageEntry.class));

        ArgumentCaptor<ShsMessageEntry> argument = ArgumentCaptor.forClass(ShsMessageEntry.class);
        verify(messageLogService).messageSent(argument.capture());
        Assert.assertEquals(argument.getValue().getLabel().getSequenceType(), SequenceType.REQUEST);
        
        verify(messageLogService).messageReceived(argument.capture());
        Assert.assertEquals(argument.getValue().getLabel().getSequenceType(), SequenceType.REPLY);

        shsServerEndpoint.assertIsSatisfied();
    }

    @DirtiesContext
    @Test
    public void sendingSynchRequestWithoutAgreementShouldBeQuarantined() throws InterruptedException {
        final ShsMessage testMessage = make(createSynchMessageWithLocalReceiver());
        ShsMessageEntry entry = messageLogService.saveMessage(testMessage);
        
        doThrow(new MissingAgreementException("no agreement found"))
                .when(agreementService).validateAgreement(any(ShsLabel.class));

        try {
            String response = camel.requestBody("direct-vm:shs:synch", entry,
                    String.class);
            Assert.fail("Did not throw excpetion when expected to");
        } catch (Exception e) {
        	// Exception is expected
        	log.info("Exception caught: " + e.getMessage());
        }

        shsLocalEndpoint.assertIsSatisfied();

        verify(messageLogService).messageQuarantined(any(ShsMessageEntry.class), any(MissingAgreementException.class));
    }

    private Maker<ShsMessage> createSynchMessageWithLocalReceiver() {
        return a(ShsMessage,
                with(label, a(ShsLabel,
                        with(transferType, TransferType.SYNCH),
                        with(sequenceType, SequenceType.REQUEST),
                        with(to, a(To,
                                with(ShsLabelMaker.ToInstantiator.value, "0000000000"))))));
    }

    private Maker<ShsMessage> createSynchMessageWithKnownReceiver() {

        return a(ShsMessage,
                with(label, a(ShsLabel,
                        with(transferType, TransferType.SYNCH),
                        with(sequenceType, SequenceType.REQUEST),
                        with(to, a(To,
                                with(ShsLabelMaker.ToInstantiator.value, "1111111111"))))));
    }

    private Maker<ShsMessage> createSynchReply() {
        return a(ShsMessage,
                with(label, a(ShsLabel,
                        with(transferType, TransferType.SYNCH),
                        with(sequenceType, SequenceType.REPLY),
                        with(to, a(To,
                                with(ShsLabelMaker.ToInstantiator.value, "9999999999"))))));
    }


}
