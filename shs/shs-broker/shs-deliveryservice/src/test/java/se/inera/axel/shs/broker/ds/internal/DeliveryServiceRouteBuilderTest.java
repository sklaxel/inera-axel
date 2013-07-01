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

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.testng.AbstractCamelTestNGSpringContextTests;
import org.apache.camel.testng.AvailablePortFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.ShsLabelMaker;
import se.inera.axel.shs.xml.message.Message;
import se.inera.axel.shs.xml.message.ShsMessageList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@ContextConfiguration
public class DeliveryServiceRouteBuilderTest extends AbstractCamelTestNGSpringContextTests {

    static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeliveryServiceRouteBuilderTest.class);

    @Autowired
    MessageLogService messageLogService;

    @Produce(context = "shs-deliveryservice-test")
    ProducerTemplate camel;

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

        verify(messageLogService).messageFetched(any(ShsMessageEntry.class));
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
        Assert.assertEquals(shsMessage.getLabel().getTxId(), m1.getTxId(), "returned message is not same as expected");
        Assert.assertNotNull(shsMessage.getDataParts().get(0));

        Object content = shsMessage.getDataParts().get(0).getDataHandler().getContent();
        Assert.assertNotNull(content, "no content in fetched message");

        verify(messageLogService).messageFetched(any(ShsMessageEntry.class));

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(Exchange.HTTP_METHOD, "POST");
        headers.put("action", "ack");

        camel.sendBodyAndHeaders(DEFAULT_SHS_DS_URL + DEFAULT_OUTBOX + "/" + m1.getTxId(), null, headers);

        verify(messageLogService).acknowledge(any(ShsMessageEntry.class));
    }

}
