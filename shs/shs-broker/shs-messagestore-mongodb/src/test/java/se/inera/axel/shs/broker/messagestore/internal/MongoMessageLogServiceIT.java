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
package se.inera.axel.shs.broker.messagestore.internal;

import com.google.common.collect.Lists;
import org.apache.camel.spring.javaconfig.test.JavaConfigContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.ShsLabelMaker;
import se.inera.axel.shs.xml.label.Status;
import se.inera.axel.shs.xml.label.TransferType;

import java.util.GregorianCalendar;
import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessage;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.*;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.*;

@ContextConfiguration(locations =
        {"se.inera.axel.shs.broker.messagestore.internal.MongoDBTestContextConfig"},
        loader = JavaConfigContextLoader.class)
public class MongoMessageLogServiceIT extends AbstractTestNGSpringContextTests {

    @Autowired
    MessageLogService messageLogService;

    @BeforeMethod
    public void setupTestDB() {
        messageLogService.createEntry(
                make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                        with(transferType, TransferType.ASYNCH))))));

        messageLogService.createEntry(
                make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                        with(transferType, TransferType.ASYNCH))))));

        messageLogService.messageSent(
                messageLogService.createEntry(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(transferType, TransferType.ASYNCH)))))));

        messageLogService.messageReceived(
                messageLogService.createEntry(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(transferType, TransferType.SYNCH)))))));

        messageLogService.messageReceived(
                messageLogService.createEntry(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(transferType, TransferType.ASYNCH)))))));

        messageLogService.messageReceived(
                messageLogService.createEntry(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                                with(transferType, TransferType.ASYNCH)))))));

        messageLogService.messageReceived(
                messageLogService.createEntry(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(product, a(Product, with(Product.value, "error"))),
                                with(sequenceType, SequenceType.ADM),
                                with(transferType, TransferType.ASYNCH)))))));

        messageLogService.messageReceived(
                messageLogService.createEntry(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(product, a(Product, with(Product.value, "confirm"))),
                                with(sequenceType, SequenceType.ADM),
                                with(transferType, TransferType.ASYNCH)))))));


        messageLogService.messageReceived(
                messageLogService.createEntry(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(transferType, TransferType.ASYNCH),
                                with(status, Status.TEST)))))));


        ShsMessageEntry entry = messageLogService.messageReceived(
                messageLogService.createEntry(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(transferType, TransferType.ASYNCH)))))));

        entry.setAcknowledged(true);
        messageLogService.update(entry);

        messageLogService.messageReceived(
                messageLogService.createEntry(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                                with(endRecipient, a(EndRecipient, with(EndRecipient.value,
                                        ShsLabelMaker.DEFAULT_TEST_ENDRECIPIENT))),
                                with(transferType, TransferType.ASYNCH)))))));

        messageLogService.messageReceived(
                messageLogService.createEntry(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                                with(corrId, "testing-corrid"),
                                with(transferType, TransferType.ASYNCH)))))));

        messageLogService.messageReceived(
                messageLogService.createEntry(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                                with(transferType, TransferType.ASYNCH),
                                with(content, a(Content, with(Content.contentId, "testing-contentid")))))))));

        messageLogService.messageReceived(
                messageLogService.createEntry(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                                with(transferType, TransferType.ASYNCH),
                                with(meta, listOf(a(Meta, with(Meta.name, "namn"),
                                        with(Meta.value, "varde"))))))))));

        entry = messageLogService.messageReceived(
                messageLogService.createEntry(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                                with(subject, "lastWeeksMessage"),
                                with(transferType, TransferType.ASYNCH)))))));

        GregorianCalendar lastWeek = new GregorianCalendar();
        lastWeek.add(GregorianCalendar.DAY_OF_MONTH, -7);

        entry.setStateTimeStamp(lastWeek.getTime());
        messageLogService.update(entry);


    }

    @DirtiesContext
    @Test
    public void loggingMessageShouldCreateEntry() throws Exception {
        ShsMessage message = make(a(ShsMessage));
        ShsMessageEntry entry = messageLogService.createEntry(message);

        Assert.assertNotNull(entry);
        Assert.assertEquals(entry.getLabel().getTxId(), message.getLabel().getTxId());
    }

    @DirtiesContext
    @Test
    public void loggedAndFetchedMessageShouldBeTheSame() throws Exception {
        ShsMessage message = make(a(ShsMessage));
        ShsMessageEntry entry = messageLogService.createEntry(message);

        Assert.assertNotNull(entry);
        Assert.assertEquals(entry.getLabel().getTxId(), message.getLabel().getTxId());

        ShsMessage fetchedMessage = messageLogService.fetchMessage(entry);
        Assert.assertNotNull(fetchedMessage);
        Assert.assertEquals(fetchedMessage.getLabel().getTxId(), entry.getLabel().getTxId());

    }

    @DirtiesContext
    @Test
    public void listMessagesWithEmptyShsAddressShouldReturnNone() throws Exception {

        Iterable<ShsMessageEntry> list = messageLogService.listMessages(null, new MessageLogService.Filter());
        Assert.assertNotNull(list);
        Assert.assertTrue(list.iterator().hasNext() == false);

        list = messageLogService.listMessages("", new MessageLogService.Filter());
        Assert.assertNotNull(list);
        Assert.assertTrue(list.iterator().hasNext() == false);

        list = messageLogService.listMessages("    ", new MessageLogService.Filter());
        Assert.assertNotNull(list);
        Assert.assertTrue(list.iterator().hasNext() == false);

    }

    @DirtiesContext
    @Test
    public void listMessagesShouldNotReturnSyncMessages() throws Exception {

        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO,
                        new MessageLogService.Filter());


        Assert.assertNotNull(iter);

        for (ShsMessageEntry entry : iter) {
            if (entry.getLabel().getTransferType() == TransferType.SYNCH)
                Assert.fail("no synchronous message should be returned");
        }
    }

    @DirtiesContext
    @Test
    public void listMessagesWithCorrectShsAddress() throws Exception {

        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO,
                        new MessageLogService.Filter());


        Assert.assertNotNull(iter);
        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        Assert.assertEquals(list.size(), 4, "correct 'to'-address with empty filter should return 4 messages");
    }

    @DirtiesContext
    @Test
    public void listMessagesWithOneProductId() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();
        filter.getProductIds().add("confirm");

        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO, filter);

        Assert.assertNotNull(iter);
        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        Assert.assertEquals(list.size(), 1, "only 1 'confirm' messages should be returned in message list");
    }

    @DirtiesContext
    @Test
    public void listMessagesWithTwoProductId() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();
        filter.getProductIds().add("confirm");
        filter.getProductIds().add("error");

        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO, filter);

        Assert.assertNotNull(iter);
        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        Assert.assertEquals(list.size(), 2, "1 'error' and 1 'confirm' should be returned");
    }

    @DirtiesContext
    @Test
    public void listMessagesWithMaxHits() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();

        filter.setMaxHits(2);
        Iterable<ShsMessageEntry> iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO, filter);

        Assert.assertNotNull(iter);
        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        Assert.assertEquals(list.size(), 2, "filter with maxHits 2 should return 2 messages");

    }

    @DirtiesContext
    @Test
    public void listMessagesWithNoAck() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();

        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO, filter);

        Assert.assertNotNull(iter);
        List<ShsMessageEntry> listOfAll = Lists.newArrayList(iter);

        filter.setNoAck(true);
        iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO, filter);

        Assert.assertNotNull(iter);
        List<ShsMessageEntry> listOfNotAcked = Lists.newArrayList(iter);
        Assert.assertEquals(listOfNotAcked.size(), listOfAll.size() - 1,
                "filter with noAck should return 1 less than all");

    }

    @DirtiesContext
    @Test
    public void listMessagesWithStatus() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();

        filter.setStatus(Status.TEST);
        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO, filter);

        Assert.assertNotNull(iter);
        List<ShsMessageEntry> list = Lists.newArrayList(iter);

        Assert.assertEquals(list.size(), 1, "exactly 1 test message should exist");

    }

    @DirtiesContext
    @Test
    public void listMessagesWithEndRecipient() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();
        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        Assert.assertTrue(list.size() > 1,
                "more than 1 message should be address to " + ShsLabelMaker.DEFAULT_TEST_FROM);

        filter.setEndRecipient(ShsLabelMaker.DEFAULT_TEST_ENDRECIPIENT);
        iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        Assert.assertNotNull(iter);
        list = Lists.newArrayList(iter);

        Assert.assertEquals(list.size(), 1,
                "exactly 1 message should be addressed to end recipient " + ShsLabelMaker.DEFAULT_TEST_ENDRECIPIENT);

    }

    @DirtiesContext
    @Test
    public void listMessagesWithCorrId() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();
        filter.setCorrId("testing-corrid");
        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        Assert.assertEquals(list.size(), 1, "exactly 1 message with given corrId should exist");

    }

    @DirtiesContext
    @Test
    public void listMessagesWithContentId() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();
        filter.setContentId("testing-contentid");
        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        Assert.assertEquals(list.size(), 1, "exactly 1 message with given contentId should exist");

    }

    @DirtiesContext
    @Test
    public void listMessagesWithMeta() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();
        filter.setMetaName("namn");
        filter.setMetaValue("varde");
        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        Assert.assertEquals(list.size(), 1, "exactly 1 message with given meta data should exist");


        filter.setMetaName("namn");
        filter.setMetaValue(null);
        iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        list = Lists.newArrayList(iter);
        Assert.assertEquals(list.size(), 1, "exactly 1 message with given meta data name should exist");


        filter.setMetaName(null);
        filter.setMetaValue("varde");
        iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        list = Lists.newArrayList(iter);
        Assert.assertEquals(list.size(), 1, "exactly 1 message with given meta data value should exist");

        filter.setMetaName(ShsLabelMaker.DEFAULT_TEST_META_NAME);
        filter.setMetaValue(ShsLabelMaker.DEFAULT_TEST_META_VALUE);
        iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        list = Lists.newArrayList(iter);
        Assert.assertTrue(list.size() > 1, "more than 1 message with default meta data value should exist");

    }

    @DirtiesContext
    @Test
    public void listMessagesWithSince() throws Exception {


        MessageLogService.Filter filter = new MessageLogService.Filter();
        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        int sizeWithoutSince = list.size();
        Assert.assertTrue(sizeWithoutSince > 2, "more than 2 messages should exist");

        GregorianCalendar yesterDay = new GregorianCalendar();
        yesterDay.add(GregorianCalendar.DAY_OF_MONTH, -1);

        filter.setSince(yesterDay.getTime());

        iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        list = Lists.newArrayList(iter);
        int sizeWithSince = list.size();
        Assert.assertEquals(sizeWithSince, sizeWithoutSince - 1,
                "sizeWithSince should be one less than sizeWithoutSince");

    }


    @DirtiesContext
    @Test
    public void listMessagesWithArrivalOrder() throws Exception {


        MessageLogService.Filter filter = new MessageLogService.Filter();
        filter.setArrivalOrder("ascending");
        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        List<ShsMessageEntry> list = Lists.newArrayList(iter);

        Assert.assertEquals(list.get(0).getLabel().getSubject(), "lastWeeksMessage",
                "first (last weeks) message should be returned first when arrivalsortorder is true.");

        filter.setArrivalOrder(null);

        iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        list = Lists.newArrayList(iter);
        Assert.assertEquals(list.get(0).getLabel().getSubject(), "lastWeeksMessage",
                "first (last weeks) message should be returned first when arrivalsortorder is null.");


        filter.setArrivalOrder("descending");

        iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        list = Lists.newArrayList(iter);

        Assert.assertEquals(list.get(list.size() - 1).getLabel().getSubject(), "lastWeeksMessage",
                "first (last weeks) message should be returned last when arrivalsortorder is false.");

    }

    @DirtiesContext
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void listMessagesWithFaultyArrivalOrderShouldThrow() throws Exception {


        MessageLogService.Filter filter = new MessageLogService.Filter();
        filter.setArrivalOrder("asc");
        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);
    }

}
