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
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.ShsLabelMaker;
import se.inera.axel.shs.xml.label.TransferType;

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
        ShsMessage message = make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                            with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_TO))),
                                            with(transferType, TransferType.ASYNCH)))));
        ShsMessageEntry entry = messageLogService.createEntry(message);

        Assert.assertNotNull(entry);
        Assert.assertEquals(entry.getLabel().getTxId(), message.getLabel().getTxId());

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
        ShsMessage message = make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                    with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_TO))),
                                    with(transferType, TransferType.SYNCH)))));
        ShsMessageEntry entry = messageLogService.createEntry(message);

        Assert.assertNotNull(entry);

        message = make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                    with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_TO))),
                                    with(transferType, TransferType.ASYNCH)))));
        entry = messageLogService.createEntry(message);

        Assert.assertNotNull(entry);

        message = make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                            with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_TO))),
                                            with(transferType, TransferType.SYNCH)))));
        entry = messageLogService.createEntry(message);

        Assert.assertNotNull(entry);



        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO,
                        new MessageLogService.Filter());


        Assert.assertNotNull(iter);
        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        Assert.assertEquals(list.size(), 1, "no synchronous message should be returned");
    }

    @DirtiesContext
    @Test
    public void listMessagesWithCorrectShsAddress() throws Exception {
        ShsMessage message = make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                    with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_TO))),
                                    with(transferType, TransferType.ASYNCH)))));
        ShsMessageEntry entry = messageLogService.createEntry(message);

        Assert.assertNotNull(entry);

        message = make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                    with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                                    with(transferType, TransferType.ASYNCH)))));
        entry = messageLogService.createEntry(message);

        Assert.assertNotNull(entry);


        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO,
                        new MessageLogService.Filter());


        Assert.assertNotNull(iter);
        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        Assert.assertEquals(list.size(), 1, "outbox criteria does not work");
    }

    @DirtiesContext
    @Test
    public void listMessagesWithOneProductId() throws Exception {
        ShsMessage message1 = make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                            with(product, a(Product, with(Product.value, ShsLabelMaker.DEFAULT_TEST_PRODUCT_ID))),
                            with(transferType, TransferType.ASYNCH)))));

        ShsMessageEntry entry1 = messageLogService.createEntry(message1);
        Assert.assertNotNull(entry1);

        ShsMessage message2 = make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                with(product, a(Product, with(Product.value, "error"))),
                with(sequenceType, SequenceType.ADM),
                with(transferType, TransferType.ASYNCH)))));

        ShsMessageEntry entry2 = messageLogService.createEntry(message2);
        Assert.assertNotNull(entry2);

        MessageLogService.Filter filter = new MessageLogService.Filter();
        filter.getProductIds().add(ShsLabelMaker.DEFAULT_TEST_PRODUCT_ID);

        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO, filter);

        Assert.assertNotNull(iter);
        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        Assert.assertEquals(list.size(), 1, "product criteria does not work");
    }

    @DirtiesContext
    @Test
    public void listMessagesWithTwoProductId() throws Exception {
        ShsMessage message1 = make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                            with(product, a(Product, with(Product.value, ShsLabelMaker.DEFAULT_TEST_PRODUCT_ID))),
                            with(transferType, TransferType.ASYNCH)))));

        ShsMessageEntry entry1 = messageLogService.createEntry(message1);
        Assert.assertNotNull(entry1);

        ShsMessage message2 = make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                with(product, a(Product, with(Product.value, "error"))),
                with(sequenceType, SequenceType.ADM),
                with(transferType, TransferType.ASYNCH)))));

        ShsMessageEntry entry2 = messageLogService.createEntry(message2);
        Assert.assertNotNull(entry2);

        ShsMessage message3 = make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                with(product, a(Product, with(Product.value, "confirm"))),
                with(sequenceType, SequenceType.ADM),
                with(transferType, TransferType.ASYNCH)))));

        ShsMessageEntry entry3 = messageLogService.createEntry(message3);
        Assert.assertNotNull(entry3);

        MessageLogService.Filter filter = new MessageLogService.Filter();
        filter.getProductIds().add(ShsLabelMaker.DEFAULT_TEST_PRODUCT_ID);
        filter.getProductIds().add("error");

        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO, filter);

        Assert.assertNotNull(iter);
        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        Assert.assertEquals(list.size(), 2, "filter with two product ids does not work");
    }

    @DirtiesContext
    @Test
    public void listMessagesWithMaxHits() throws Exception {
        ShsMessage message1 = make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                            with(product, a(Product, with(Product.value, ShsLabelMaker.DEFAULT_TEST_PRODUCT_ID))),
                            with(transferType, TransferType.ASYNCH)))));

        ShsMessageEntry entry1 = messageLogService.createEntry(message1);
        Assert.assertNotNull(entry1);

        ShsMessage message2 = make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                with(product, a(Product, with(Product.value, "error"))),
                with(sequenceType, SequenceType.ADM),
                with(transferType, TransferType.ASYNCH)))));

        ShsMessageEntry entry2 = messageLogService.createEntry(message2);
        Assert.assertNotNull(entry2);

        ShsMessage message3 = make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                with(product, a(Product, with(Product.value, "confirm"))),
                with(sequenceType, SequenceType.ADM),
                with(transferType, TransferType.ASYNCH)))));

        ShsMessageEntry entry3 = messageLogService.createEntry(message3);
        Assert.assertNotNull(entry3);

        MessageLogService.Filter filter = new MessageLogService.Filter();

        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO, filter);

        Assert.assertNotNull(iter);
        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        Assert.assertEquals(list.size(), 3, "filter with no maxHits should return 3 messages");

        filter.setMaxHits(2);
        iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO, filter);

        Assert.assertNotNull(iter);
        list = Lists.newArrayList(iter);
        Assert.assertEquals(list.size(), 2, "filter with maxHits 2 should return 2 messages");

    }

    @DirtiesContext
    @Test
    public void listMessagesWithNoAck() throws Exception {
        ShsMessage message1 = make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                            with(product, a(Product, with(Product.value, ShsLabelMaker.DEFAULT_TEST_PRODUCT_ID))),
                            with(transferType, TransferType.ASYNCH)))));

        ShsMessageEntry entry1 = messageLogService.createEntry(message1);
        Assert.assertNotNull(entry1);

        ShsMessage message2 = make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                with(product, a(Product, with(Product.value, "error"))),
                with(sequenceType, SequenceType.ADM),
                with(transferType, TransferType.ASYNCH)))));

        ShsMessageEntry entry2 = messageLogService.createEntry(message2);
        Assert.assertNotNull(entry2);

        ShsMessage message3 = make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                with(product, a(Product, with(Product.value, "confirm"))),
                with(sequenceType, SequenceType.ADM),
                with(transferType, TransferType.ASYNCH)))));

        ShsMessageEntry entry3 = messageLogService.createEntry(message3);
        Assert.assertNotNull(entry3);

        entry1.setAcknowledged(true);
        entry1 = messageLogService.update(entry1);
        Assert.assertNotNull(entry1);

        MessageLogService.Filter filter = new MessageLogService.Filter();

        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO, filter);

        Assert.assertNotNull(iter);
        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        Assert.assertEquals(list.size(), 3, "filter without noAck should return 3 messages");

        filter.setNoAck(true);
        iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO, filter);

        Assert.assertNotNull(iter);
        list = Lists.newArrayList(iter);
        Assert.assertEquals(list.size(), 2, "filter with noAck should return 2 messages");

    }

}
