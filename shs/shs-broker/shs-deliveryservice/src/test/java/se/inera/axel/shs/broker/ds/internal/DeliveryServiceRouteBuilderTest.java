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
package se.inera.axel.shs.broker.ds.internal;

import org.apache.camel.*;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.AbstractCamelTestNGSpringContextTests;
import org.apache.camel.testng.AvailablePortFinder;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.ShsLabelMaker;
import se.inera.axel.shs.xml.label.Status;
import se.inera.axel.shs.xml.message.Message;
import se.inera.axel.shs.xml.message.ShsMessageList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@ContextConfiguration
public class DeliveryServiceRouteBuilderTest extends AbstractCamelTestNGSpringContextTests {

    static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeliveryServiceRouteBuilderTest.class);

    @Autowired
    MessageLogService messageLogService;

    @Produce(context = "shs-deliveryservice-test")
    ProducerTemplate camel;

    @EndpointInject(uri = "mock:createdMessages")
    MockEndpoint createdMessagesEndpoint;

    public static String DEFAULT_OUTBOX = "urn:x-shs:" + ShsLabelMaker.DEFAULT_TEST_TO;
    public static String DEFAULT_SHS_DS_URL = "{{shsDsHttpEndpoint}}:{{shsDsHttpEndpoint.port}}/shs/ds/";

    public DeliveryServiceRouteBuilderTest() {
        if (System.getProperty("shsDsHttpEndpoint.port") == null) {
            int port = AvailablePortFinder.getNextAvailable(9100);
            System.setProperty("shsDsHttpEndpoint.port", Integer.toString(port));
        }
    }


    @DirtiesContext
    @Test
    public void listMessagesWithCorrectURLShouldWork() throws Exception {

        ShsMessageList response =
                camel.requestBodyAndHeader(DEFAULT_SHS_DS_URL + DEFAULT_OUTBOX, null,
                Exchange.HTTP_METHOD, "GET", ShsMessageList.class);

        Assert.assertNotNull(response, "no response from server");
        Assert.assertNotNull(response.getMessage(), "message list in response is null");
        Assert.assertTrue(response.getMessage().size() > 0, "some messages are expected");
    }

    @DirtiesContext
    @Test(expectedExceptions = CamelExecutionException.class)
    public void listMessagesWithoutOutboxShouldThrow() throws Exception {
        camel.requestBodyAndHeader(DEFAULT_SHS_DS_URL, null, Exchange.HTTP_METHOD, "GET");
    }

    @DirtiesContext
    @Test
    public void listMessagesWithNonExistingOutboxReturnEmptyList() throws Exception {

        ShsMessageList response =
                camel.requestBodyAndHeader(DEFAULT_SHS_DS_URL + "nonexisting", null,
                Exchange.HTTP_METHOD, "GET", ShsMessageList.class);

        Assert.assertNotNull(response, "no response from server");
        Assert.assertNotNull(response.getMessage(), "message list in response is null");
        Assert.assertEquals(response.getMessage().size(), 0, "no message is expected");
    }

    @DirtiesContext
    @Test
    public void listAndFetchMessageWithCorrectURLShouldWork() throws Exception {

        ShsMessageList response =
                camel.requestBodyAndHeader(DEFAULT_SHS_DS_URL + DEFAULT_OUTBOX, null,
                Exchange.HTTP_METHOD, "GET", ShsMessageList.class);

        Assert.assertNotNull(response, "no response from server");
        Assert.assertNotNull(response.getMessage(), "message list in response is null");
        Assert.assertTrue(response.getMessage().size() > 0, "some messages are expected");

        Message m1 = response.getMessage().get(0);

        ShsMessage shsMessage =
                camel.requestBodyAndHeader(DEFAULT_SHS_DS_URL + DEFAULT_OUTBOX + "/" + m1.getTxId(), null,
                Exchange.HTTP_METHOD, "GET", ShsMessage.class);

        Assert.assertNotNull(shsMessage, "no response from server");
        Assert.assertEquals(shsMessage.getLabel().getTxId(), m1.getTxId(), "returned message is not same as expected");
        Assert.assertNotNull(shsMessage.getDataParts().get(0));

        Object content = shsMessage.getDataParts().get(0).getDataHandler().getContent();
        Assert.assertNotNull(content, "no content in fetched message");

        // unstable verify, sometimes work and sometimes does not.
        // verify(messageLogService).messageFetched(Matchers.any(ShsMessageEntry.class));
    }


    @DirtiesContext
    @Test(expectedExceptions = CamelExecutionException.class)
    public void listAndFetchMessageWithNonExistingTxIdShouldThrow() throws Exception {

        camel.requestBodyAndHeader(DEFAULT_SHS_DS_URL + DEFAULT_OUTBOX + "/" + UUID.randomUUID(), null,
                    Exchange.HTTP_METHOD, "GET");
    }

    @DirtiesContext
    @Test
    public void listAndFetchMessageWithNonTxIdShouldReturnMessageList() throws Exception {
        //Test if productid in url

        ShsMessageList response =
                camel.requestBodyAndHeader(DEFAULT_SHS_DS_URL + DEFAULT_OUTBOX + "/" + "confirm", null,
                Exchange.HTTP_METHOD, "GET", ShsMessageList.class);

        Assert.assertNotNull(response);

    }

    @DirtiesContext
    @Test
    public void listAndFetchAndAckMessageWithCorrectURLShouldWork() throws Exception {

        ShsMessageList response =
                camel.requestBodyAndHeader(DEFAULT_SHS_DS_URL + DEFAULT_OUTBOX, null,
                Exchange.HTTP_METHOD, "GET", ShsMessageList.class);

        Assert.assertNotNull(response, "no response from server");
        Assert.assertNotNull(response.getMessage(), "message list in response is null");
        Assert.assertTrue(response.getMessage().size() > 0, "some messages are expected");

        Message m1 = response.getMessage().get(0);

        ShsMessage shsMessage =
                camel.requestBodyAndHeader(DEFAULT_SHS_DS_URL + DEFAULT_OUTBOX + "/" + m1.getTxId(), null,
                Exchange.HTTP_METHOD, "GET", ShsMessage.class);

        Assert.assertNotNull(shsMessage, "no response from server");
        Assert.assertEquals(shsMessage.getLabel().getTxId(), m1.getTxId(),
                "returned message is not same as expected");
        Assert.assertNotNull(shsMessage.getDataParts().get(0));

        Object content = shsMessage.getDataParts().get(0).getDataHandler().getContent();
        Assert.assertNotNull(content, "no content in fetched message");

        verify(messageLogService).messageFetched(Matchers.any(ShsMessageEntry.class));

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(Exchange.HTTP_METHOD, "POST");
        headers.put("action", "ack");

        camel.sendBodyAndHeaders(DEFAULT_SHS_DS_URL + DEFAULT_OUTBOX + "/" + m1.getTxId(), null, headers);

        verify(messageLogService).acknowledge(Matchers.any(ShsMessageEntry.class));

        Exchange exchange = createdMessagesEndpoint.assertExchangeReceived(0);
        ShsMessage confirmMessage = exchange.getIn().getBody(ShsMessage.class);
        Assert.assertNotNull(confirmMessage, "no confirm message created");

        Assert.assertNotNull(confirmMessage.getLabel());
        Assert.assertNotNull(confirmMessage.getLabel().getProduct());
        Assert.assertNotNull(confirmMessage.getLabel().getProduct().getvalue());
        Assert.assertEquals(confirmMessage.getLabel().getProduct().getvalue(),
                "confirm", "Received message is not a confirm message");

        Assert.assertEquals(confirmMessage.getLabel().getTo().getvalue(),
                shsMessage.getLabel().getFrom().getvalue(),
                "Received confirm message's 'to' should equal request message's 'from'");
    }

    @DirtiesContext
    @Test
    public void listMessagesWithQueryParams() throws Exception {

        camel.requestBodyAndHeader(DEFAULT_SHS_DS_URL + DEFAULT_OUTBOX
                + "?filter=noack", null,
                Exchange.HTTP_METHOD, "GET", ShsMessageList.class);

        verify(messageLogService).listMessages(
                eq(ShsLabelMaker.DEFAULT_TEST_TO),
                (MessageLogService.Filter)argThat(
                        allOf(hasProperty("noAck", is(true)),
                                hasProperty("since", nullValue()))));

        camel.requestBodyAndHeader(DEFAULT_SHS_DS_URL + DEFAULT_OUTBOX
                + "?producttype=error,confirm"
                + "&maxhits=5"
                + "&status=test"
                + "&corrid=12345"
                + "&originator=origin"
                + "&endrecipient=endrep"
                + "&sortattribute=from"
                + "&sortorder=descending"
                + "&arrivalorder=descending"
                + "&contentid=98765"
                + "&since=1998-08-19T12:13:39"
                , null,
                Exchange.HTTP_METHOD, "GET", ShsMessageList.class);

        verify(messageLogService).listMessages(
                eq(ShsLabelMaker.DEFAULT_TEST_TO),
                (MessageLogService.Filter)argThat(
                        allOf(hasProperty("noAck", is(false)),
                                hasProperty("productIds",
                                        containsInAnyOrder("error", "confirm")),
                                hasProperty("maxHits", equalTo(5)),
                                hasProperty("contentId", equalTo("98765")),
                                hasProperty("corrId", equalTo("12345")),
                                hasProperty("originator", equalTo("origin")),
                                hasProperty("status", equalTo(Status.TEST)),
                                hasProperty("endRecipient", equalTo("endrep")),
                                hasProperty("sortAttribute", equalTo("from")),
                                hasProperty("sortOrder", equalTo("descending")),
                                hasProperty("arrivalOrder", equalTo("descending")),
                                hasProperty("since", notNullValue())
                                )));

    }

}
