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

import org.apache.camel.CamelExecutionException;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.http.HttpOperationFailedException;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.AvailablePortFinder;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.apache.camel.testng.AbstractCamelTestNGSpringContextTests;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.agreement.AgreementService;
import se.inera.axel.shs.broker.directory.DirectoryService;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.routing.ShsRouter;
import se.inera.axel.shs.exception.MissingAgreementException;
import se.inera.axel.shs.exception.MissingDeliveryExecutionException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.mime.ShsMessageMaker;
import se.inera.axel.shs.processor.ResponseMessageBuilder;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.util.HashMap;
import java.util.Map;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessage;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessageInstantiator.label;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabel;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.sequenceType;

@ContextConfiguration
@MockEndpointsAndSkip("http:shsServer.*|shs:local")
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

    @Value("${shsRsPathPrefix}")
    private String shsRsPathPrefix;

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

    static {
        if (System.getProperty("shsRsHttpEndpoint.port") == null) {
            int port = AvailablePortFinder.getNextAvailable();
            System.setProperty("shsRsHttpEndpoint.port", Integer.toString(port));
        }

        System.setProperty("shsRsHttpEndpoint",
                String.format("jetty://http://localhost:%s", System.getProperty("shsRsHttpEndpoint.port")));
    }

    @BeforeClass
    public void beforeClass() {
    }

	@BeforeMethod
	public void beforeMethod() {
    	shsLocalEndpoint.returnReplyBody(replyShsMessageBuilder);
    	shsServerEndpoint.returnReplyBody(replyShsMessageBuilder);
	}

    private void routeToRemoteNode() {
        when(shsRouter.isLocal(any(se.inera.axel.shs.xml.label.ShsLabel.class))).thenReturn(false);
        when(shsRouter.resolveEndpoint(any(ShsLabel.class)))
                .thenReturn("http://localhost:"
                            + System.getProperty("shsRsHttpEndpoint.port")
                            + shsRsPathPrefix);
    }

    private void routeToLocalNode() {
        when(shsRouter.isLocal(any(se.inera.axel.shs.xml.label.ShsLabel.class))).thenReturn(true);
    }

	@DirtiesContext
    @Test
    public void sendShouldReturnResponseWhenSendingToLocalReceiver() throws InterruptedException {
        shsLocalEndpoint.expectedMessageCount(1);
        routeToLocalNode();

        final ShsMessage requestShsMessage = makeRequestShsMessage();
		ShsMessage responseShsMessage = camel.requestBody("direct-vm:shs:synch", requestShsMessage, ShsMessage.class);
        Assert.assertNotNull(responseShsMessage);
        Assert.assertEquals(responseShsMessage.getLabel().getSequenceType(), SequenceType.REPLY);

        shsLocalEndpoint.assertIsSatisfied();
    }

    @DirtiesContext
    @Test
    public void sendShouldReturnResponseWhenSendingToRemoteReceiver() throws InterruptedException {
    	shsServerEndpoint.expectedMessageCount(1);
        routeToRemoteNode();

        final ShsMessage requestShsMessage = makeRequestShsMessage();
		ShsMessage responseShsMessage = camel.requestBody("direct-vm:shs:synch", requestShsMessage, ShsMessage.class);
        Assert.assertNotNull(responseShsMessage);
        Assert.assertEquals(responseShsMessage.getLabel().getSequenceType(), SequenceType.REPLY);

        shsServerEndpoint.assertIsSatisfied();
    }

    @DirtiesContext
    @Test
    public void requestShouldNotBeRoutedWhenAgreementValidationFails() {
        final ShsMessage requestShsMessage = makeRequestShsMessage();

		// Simulate a failure in route processing by means of throwing an exception
		doThrow(new MissingAgreementException("no agreement found")).when(
                agreementService).validateAgreement(any(ShsLabel.class));

        reset(shsRouter);

        try {
            camel.requestBody("direct-vm:shs:synch", requestShsMessage);
            Assert.fail("Did not throw exception when expected to");
        } catch (Exception e) {
            assertThat(e.getCause(), instanceOf(MissingAgreementException.class));
        }
        verify(shsRouter, never()).isLocal(any(se.inera.axel.shs.xml.label.ShsLabel.class));
    }

    @DirtiesContext
    @Test
    public void whenRemoteNodeReturnsMessageHandlingErrorMissingDeliveryExecutionExceptionShouldBeThrown() {
        routeToRemoteNode();

        final ShsMessage requestShsMessage = makeRequestShsMessage();
        camel.requestBody("direct-vm:shs:synch", requestShsMessage, String.class);

        shsServerEndpoint.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                MissingAgreementException missingAgreementException = new MissingAgreementException();
                exchange.getIn().setHeader(ShsHeaders.X_SHS_ERRORCODE, missingAgreementException.getErrorCode());

                Map<String, String> httpHeaders = new HashMap<>();
                httpHeaders.put(ShsHeaders.X_SHS_ERRORCODE, missingAgreementException.getErrorCode());

                HttpOperationFailedException httpOperationFailedException = new HttpOperationFailedException("http://shsServer",
                        HttpURLConnection.HTTP_BAD_REQUEST,
                        "Bad request",
                        null,
                        httpHeaders,
                        missingAgreementException.getMessage());

                throw httpOperationFailedException;
            }
        });

        try {
            camel.requestBody("direct-vm:shs:synch", requestShsMessage);
            Assert.fail("Did not throw exception when expected to");
        } catch (CamelExecutionException e) {
            assertThat(e.getCause(), is(instanceOf(MissingDeliveryExecutionException.class)));
        }
    }

    private ShsMessage makeRequestShsMessage() {
    	ShsLabel label = make(a(ShsLabel,
                with(sequenceType, SequenceType.REQUEST)));
    	
    	ShsMessage shsMessage =
	            make(a(ShsMessage,
	                    with(ShsMessageMaker.ShsMessageInstantiator.label, label)));
		return shsMessage;
    }
}
