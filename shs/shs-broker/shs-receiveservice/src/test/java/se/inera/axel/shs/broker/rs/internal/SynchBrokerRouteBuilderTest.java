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
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessage;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessageInstantiator.label;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabel;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.sequenceType;

import java.io.InputStream;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.AvailablePortFinder;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.apache.camel.testng.AbstractCamelTestNGSpringContextTests;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.inera.axel.shs.broker.agreement.AgreementService;
import se.inera.axel.shs.broker.directory.DirectoryService;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntryMaker;
import se.inera.axel.shs.broker.routing.ShsRouter;
import se.inera.axel.shs.exception.MissingAgreementException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ResponseMessageBuilder;
import se.inera.axel.shs.processor.ShsMessageMarshaller;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.ShsLabel;

@ContextConfiguration
@MockEndpointsAndSkip("http:shsServer|shs:local")
public class SynchBrokerRouteBuilderTest extends AbstractCamelTestNGSpringContextTests {

    static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SynchBrokerRouteBuilderTest.class);

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

    /**
     * Builds a reply ShsMessage from a request ShsMessage.
     */
    final Expression replyShsMessageBuilder = new Expression() {

    	/**
    	 * Creates new instance of reply ShsLabel and ShsMessage which is required so that
    	 * the unit tests can verify both the request and the reply object. Otherwise,
    	 * the reply would overwrite the request.
    	 */
		@Override
		public <T> T evaluate(Exchange exchange, Class<T> type) {

			ShsMessage request = exchange.getIn().getBody(ShsMessage.class);
			ShsLabel requestLabel = request.getLabel();

			// Create new instance of ShsLabel
			ResponseMessageBuilder rmb = new ResponseMessageBuilder();
			ShsLabel replyLabel = rmb.buildReplyLabel(requestLabel);
			
			// Create new instance of ShsMessage
			ShsMessage replyShsMessage = make(a(ShsMessage, with(label, replyLabel)));
			replyShsMessage.getLabel().setSequenceType(SequenceType.REPLY);
			
			T reply = exchange.getContext().getTypeConverter().convertTo(type, replyShsMessage);
			
			return reply;
		}
	};

    @BeforeClass
    public void beforeClass() {
        System.setProperty("shsRsHttpEndpoint",
                String.format("jetty://http://localhost:%s", AvailablePortFinder.getNextAvailable()));
    }

	@BeforeMethod
	public void beforeMethod() {
		mockMessageLogService_saveMessageStream();
    	shsLocalEndpoint.returnReplyBody(replyShsMessageBuilder);
    	shsServerEndpoint.returnReplyBody(replyShsMessageBuilder);
	}
    
	private void mockMessageLogService_saveMessageStream() {
		final ShsMessageMarshaller shsMessageMarshaller = new ShsMessageMarshaller();
		when(messageLogService.saveMessageStream(any(InputStream.class)))
				.thenAnswer(new Answer<ShsMessageEntry>() {
					@Override
					public ShsMessageEntry answer(InvocationOnMock invocation)
							throws Throwable {

						InputStream originalMessageStream = (InputStream) invocation
								.getArguments()[0];
						ShsLabel label = shsMessageMarshaller
								.parseLabel(originalMessageStream);

						ShsMessageEntry shsMessageEntry = new ShsMessageEntry();
						shsMessageEntry.setLabel(label);

						return shsMessageEntry;
					}
				});
	}

	@DirtiesContext
    @Test
    public void sendShouldReturnResponseWhenSendingToLocalReceiver() throws InterruptedException {
        shsLocalEndpoint.expectedMessageCount(1);
        when(shsRouter.isLocal(any(se.inera.axel.shs.xml.label.ShsLabel.class))).thenReturn(true);
    
		final ShsMessageEntry requestShsMessageEntry = makeRequestShsMessageEntry();
		ShsMessageEntry responseShsMessageEntry = camel.requestBody("direct-vm:shs:synch", requestShsMessageEntry, ShsMessageEntry.class);
        Assert.assertNotNull(responseShsMessageEntry);
        Assert.assertEquals(responseShsMessageEntry.getLabel().getSequenceType(), SequenceType.REPLY);

        shsLocalEndpoint.assertIsSatisfied();
    }
    
    @DirtiesContext
    @Test
    public void sendShouldReturnResponseWhenSendingToRemoteReceiver() throws InterruptedException {
    	shsServerEndpoint.expectedMessageCount(1);
        when(shsRouter.isLocal(any(se.inera.axel.shs.xml.label.ShsLabel.class))).thenReturn(false);
    
		final ShsMessageEntry requestShsMessageEntry = makeRequestShsMessageEntry();
		ShsMessageEntry responseShsMessageEntry = camel.requestBody("direct-vm:shs:synch", requestShsMessageEntry, ShsMessageEntry.class);
        Assert.assertNotNull(responseShsMessageEntry);
        Assert.assertEquals(responseShsMessageEntry.getLabel().getSequenceType(), SequenceType.REPLY);

        shsServerEndpoint.assertIsSatisfied();
    }

    @DirtiesContext
    @Test
	public void requestShouldBeMarkedAsSentWhenSendingToLocalReceiver() {
        when(shsRouter.isLocal(any(se.inera.axel.shs.xml.label.ShsLabel.class))).thenReturn(true);

		final ShsMessageEntry requestShsMessageEntry = makeRequestShsMessageEntry();
		camel.requestBody("direct-vm:shs:synch", requestShsMessageEntry, String.class);
		
        ArgumentCaptor<ShsMessageEntry> argument = ArgumentCaptor.forClass(ShsMessageEntry.class);
        verify(messageLogService).messageSent(argument.capture());
        Assert.assertEquals(argument.getValue().getLabel().getSequenceType(), SequenceType.REQUEST);
    }
    
    @DirtiesContext
    @Test
	public void requestShouldBeMarkedAsSentWhenSendingToRemoteReceiver() {
        when(shsRouter.isLocal(any(se.inera.axel.shs.xml.label.ShsLabel.class))).thenReturn(false);

		final ShsMessageEntry requestShsMessageEntry = makeRequestShsMessageEntry();
		camel.requestBody("direct-vm:shs:synch", requestShsMessageEntry, String.class);
		
        ArgumentCaptor<ShsMessageEntry> argument = ArgumentCaptor.forClass(ShsMessageEntry.class);
        verify(messageLogService).messageSent(argument.capture());
        Assert.assertEquals(argument.getValue().getLabel().getSequenceType(), SequenceType.REQUEST);
    }
    
    @DirtiesContext
    @Test
    public void replyShouldBeMarkedAsReceivedWhenSendingToLocalReceiver() {
        when(shsRouter.isLocal(any(se.inera.axel.shs.xml.label.ShsLabel.class))).thenReturn(true);

		final ShsMessageEntry requestShsMessageEntry = makeRequestShsMessageEntry();
		camel.requestBody("direct-vm:shs:synch", requestShsMessageEntry, String.class);
		
        ArgumentCaptor<ShsMessageEntry> argument = ArgumentCaptor.forClass(ShsMessageEntry.class);
        verify(messageLogService).messageReceived(argument.capture());
        Assert.assertEquals(argument.getValue().getLabel().getSequenceType(), SequenceType.REPLY);
    }

    @DirtiesContext
    @Test
    public void replyShouldBeMarkedAsReceivedWhenSendingToRemoteReceiver() {
        when(shsRouter.isLocal(any(se.inera.axel.shs.xml.label.ShsLabel.class))).thenReturn(false);

		final ShsMessageEntry requestShsMessageEntry = makeRequestShsMessageEntry();
		camel.requestBody("direct-vm:shs:synch", requestShsMessageEntry, String.class);
		
        ArgumentCaptor<ShsMessageEntry> argument = ArgumentCaptor.forClass(ShsMessageEntry.class);
        verify(messageLogService).messageReceived(argument.capture());
        Assert.assertEquals(argument.getValue().getLabel().getSequenceType(), SequenceType.REPLY);
    }

    @DirtiesContext
    @Test
    public void requestShouldBeMarkedAsQuarantinedWhenSendingToLocalReceiverFails() {
        when(shsRouter.isLocal(any(se.inera.axel.shs.xml.label.ShsLabel.class))).thenReturn(true);

		final ShsMessageEntry requestShsMessageEntry = makeRequestShsMessageEntry();
		camel.requestBody("direct-vm:shs:synch", requestShsMessageEntry, String.class);

		// Simulate a failure in route processing by means of throwing an exception
		doThrow(new MissingAgreementException("no agreement found")).when(
				agreementService).validateAgreement(any(ShsLabel.class));

        try {
            camel.requestBody("direct-vm:shs:synch", requestShsMessageEntry);
            Assert.fail("Did not throw excpetion when expected to");
        } catch (Exception e) {
        	// Exception is expected
        	log.info("Exception caught: " + e.getMessage());
        }

        verify(messageLogService).messageQuarantined(any(ShsMessageEntry.class), any(MissingAgreementException.class));
    }

    @DirtiesContext
    @Test
    public void requestShouldBeMarkedAsQuarantinedWhenSendingToRemoteReceiverFails() {
        when(shsRouter.isLocal(any(se.inera.axel.shs.xml.label.ShsLabel.class))).thenReturn(false);

		final ShsMessageEntry requestShsMessageEntry = makeRequestShsMessageEntry();
		camel.requestBody("direct-vm:shs:synch", requestShsMessageEntry, String.class);

		// Simulate a failure in route processing by means of throwing an exception
		doThrow(new MissingAgreementException("no agreement found")).when(
				agreementService).validateAgreement(any(ShsLabel.class));

        try {
            camel.requestBody("direct-vm:shs:synch", requestShsMessageEntry);
            Assert.fail("Did not throw excpetion when expected to");
        } catch (Exception e) {
        	// Exception is expected
        	log.info("Exception caught: " + e.getMessage());
        }

        verify(messageLogService).messageQuarantined(any(ShsMessageEntry.class), any(MissingAgreementException.class));
    }

    private ShsMessageEntry makeRequestShsMessageEntry() {
    	ShsLabel label = make(a(ShsLabel,
                with(sequenceType, SequenceType.REQUEST)));
    	
    	ShsMessageEntry shsMessageEntry =
	            make(a(ShsMessageEntryMaker.ShsMessageEntry,
	                    with(ShsMessageEntryMaker.ShsMessageEntryInstantiator.label, label)));
		return shsMessageEntry;
    }
}
