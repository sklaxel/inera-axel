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

import org.apache.camel.CamelExecutionException;
import org.apache.camel.spring.javaconfig.test.JavaConfigContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.MessageState;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsManagementMarshaller;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.ShsLabelMaker;
import se.inera.axel.shs.xml.label.Status;
import se.inera.axel.shs.xml.label.TransferType;
import se.inera.axel.shs.xml.management.Confirmation;
import se.inera.axel.shs.xml.management.Error;
import se.inera.axel.shs.xml.management.ObjectFactory;
import se.inera.axel.shs.xml.management.ShsManagement;

import java.util.GregorianCalendar;
import java.util.List;

import javax.activation.DataHandler;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessage;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.*;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.*;

@ContextConfiguration(locations =
        {"se.inera.axel.shs.broker.messagestore.internal.MongoDBTestContextConfig"},
        loader = JavaConfigContextLoader.class)
public class MongoMessageLogServiceIT extends AbstractMongoMessageLogTest {

	private static ObjectFactory shsManagementFactory = new ObjectFactory();

	@Autowired
    MongoTemplate mongoTemplate;
    
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
    public void findEntryByShsToAndTxid() throws Exception {
        ShsMessage message = make(a(ShsMessage));
        ShsMessageEntry entry = messageLogService.createEntry(message);
        Assert.assertNotNull(entry);
        ShsMessageEntry resultEntry = messageLogService.findEntryByShsToAndTxid(message.getLabel().getTo().getValue(), message.getLabel().getTxId());
        Assert.assertNotNull(resultEntry);
        Assert.assertEquals(resultEntry.getLabel().getTo().getValue(), message.getLabel().getTo().getValue());
    }

    @DirtiesContext
    @Test
    public void findEntryByShsToAndTxidWithFaultShsToShouldReturnNone() throws Exception {
        ShsMessage message = make(a(ShsMessage));
        ShsMessageEntry entry = messageLogService.createEntry(message);
        Assert.assertNotNull(entry);
        ShsMessageEntry resultEntry = messageLogService.findEntryByShsToAndTxid("1111111111", message.getLabel().getTxId());
        Assert.assertNull(resultEntry);
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
    public void listMessagesShouldOnlyReturnRECEIVEDMessages() throws Exception {

        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO,
                        new MessageLogService.Filter());


        Assert.assertNotNull(iter);

        for (ShsMessageEntry entry : iter) {
            Assert.assertTrue(entry.getState() == MessageState.RECEIVED, "Only RECEIVED messages should be returned");
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
    public void listMessagesWithOriginator() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();
        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        Assert.assertTrue(list.size() > 1,
                "more than 1 message should be address to " + ShsLabelMaker.DEFAULT_TEST_FROM);

        filter.setOriginator(ShsLabelMaker.DEFAULT_TEST_ORIGINATOR);
        iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        Assert.assertNotNull(iter);
        list = Lists.newArrayList(iter);

        Assert.assertEquals(list.size(), 1,
                "exactly 1 message should be addressed from originator " + ShsLabelMaker.DEFAULT_TEST_ORIGINATOR);
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
    @Test
    public void fetchTwiceShouldFail() throws Exception {

    	// Create message
        ShsMessage message = make(a(ShsMessage));
        ShsMessageEntry entry = messageLogService.createEntry(message);
        Assert.assertNotNull(entry);
        Assert.assertEquals(entry.getState(), MessageState.NEW);
        
        messageLogService.messageReceived(entry);
        Assert.assertEquals(entry.getState(), MessageState.RECEIVED);
        
        // Fetch message
        ShsMessageEntry entry_1 = messageLogService.findEntryByShsToAndTxidAndLockMessageForFetching(message.getLabel().getTo().getValue(), message.getLabel().getTxId());
        Assert.assertNotNull(entry_1);
        Assert.assertEquals(entry_1.getState(), MessageState.FETCHING_IN_PROGRESS);
        
        // Fetch message a second time which should fail
        ShsMessageEntry entry_2 = messageLogService.findEntryByShsToAndTxidAndLockMessageForFetching(message.getLabel().getTo().getValue(), message.getLabel().getTxId());
        Assert.assertNull(entry_2);
    }

    @DirtiesContext
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void listMessagesWithFaultyArrivalOrderShouldThrow() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();
        filter.setArrivalOrder("asc");
        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);
    }

    @DirtiesContext
    @Test
    public void receivedErrorShouldQuarantineMessages() {

		// ------------------------------------------------------------
    	// Inject two SHS messages with same corrId & contentId
    	ShsLabel label1 = make(a(ShsLabel));
        ShsMessage message1 = make(a(ShsMessage, with(ShsMessage.label, label1)));
        messageLogService.createEntry(message1);

        ShsLabel label2 = make(a(ShsLabel, 
				with(corrId, label1.getCorrId()),
				with(content, make(a(Content,
						with(Content.contentId, label1.getContent().getContentId()))))));
		ShsMessage message2 = make(a(ShsMessage, with(ShsMessage.label, label2))); 
        messageLogService.createEntry(message2);
    	
        // Assert
		Query queryQuarantinedMessages = new Query(Criteria
				.where("label.corrId").is(label1.getCorrId())
        		.and("label.content.contentId").is(label1.getContent().getContentId())
        		.and("state").is(MessageState.QUARANTINED));
		List<ShsMessageEntry> list = mongoTemplate.find(queryQuarantinedMessages, ShsMessageEntry.class);
		Assert.assertEquals(list.size(), 0, "no messages should have been quarantined");
		
		// ------------------------------------------------------------
		// Build an error message correlating to the previous two messages
        ShsManagement shsManagement = new ShsManagement();
        shsManagement.setCorrId(label1.getCorrId());
        shsManagement.setContentId(label1.getContent().getContentId());
        Error error = shsManagementFactory.createError();
        error.setErrorcode("ERROR_CODE");
        error.setErrorinfo("ERROR_INFO");
		shsManagement.getConfirmationOrError().add(error);

		DataPart dp = new DataPart();
		dp.setDataPartType("error");
		dp.setContentType("text/xml");
		dp.setFileName("error.xml");
		ShsManagementMarshaller marshaller = new ShsManagementMarshaller();
		dp.setDataHandler(new DataHandler(marshaller.marshal(shsManagement), "text/xml"));

		ShsMessage errorMessage = make(a(ShsMessage, with(ShsMessage.label, make(a(ShsLabel)))));
		errorMessage.getDataParts().clear();
		errorMessage.getDataParts().add(dp);

        // Inject error message
		messageLogService.quarantineCorrelatedMessages(errorMessage);

		// Assert
		list = mongoTemplate.find(queryQuarantinedMessages, ShsMessageEntry.class);
		Assert.assertEquals(list.size(), 2, "both messages should have been quarantined");
    }

    @DirtiesContext
    @Test
    public void receivedConfirmShouldAcknowledgeMessages() {

		// ------------------------------------------------------------
    	// Inject two SHS messages with same corrId & contentId
    	ShsLabel label1 = make(a(ShsLabel));
        ShsMessage message1 = make(a(ShsMessage, with(ShsMessage.label, label1)));
        messageLogService.createEntry(message1);

        ShsLabel label2 = make(a(ShsLabel, 
				with(corrId, label1.getCorrId()),
				with(content, make(a(Content,
						with(Content.contentId, label1.getContent().getContentId()))))));
		ShsMessage message2 = make(a(ShsMessage, with(ShsMessage.label, label2))); 
        messageLogService.createEntry(message2);

        // Assert
		Query queryAcknowledged = new Query(Criteria
				.where("label.corrId").is(label1.getCorrId())
        		.and("label.content.contentId").is(label1.getContent().getContentId())
        		.and("acknowledged").is(true));
		List<ShsMessageEntry> list = mongoTemplate.find(queryAcknowledged, ShsMessageEntry.class);
		Assert.assertEquals(list.size(), 0, "no message should have been acknowledged");

		// ------------------------------------------------------------
		// Build confirm message correlating to the previous two messages
        ShsManagement shsManagement = new ShsManagement();
        shsManagement.setCorrId(label1.getCorrId());
        shsManagement.setContentId(label1.getContent().getContentId());
        Confirmation confirmation = shsManagementFactory.createConfirmation();
		shsManagement.getConfirmationOrError().add(confirmation);
        
		DataPart dp = new DataPart();
		dp.setDataPartType("confirm");
		dp.setContentType("text/xml");
		dp.setFileName("confirm.xml");
		ShsManagementMarshaller marshaller = new ShsManagementMarshaller();
		dp.setDataHandler(new DataHandler(marshaller.marshal(shsManagement), "text/xml"));

		ShsMessage confirmMessage = make(a(ShsMessage, with(ShsMessage.label, make(a(ShsLabel)))));
		confirmMessage.getDataParts().clear();
		confirmMessage.getDataParts().add(dp);

		// Inject confirm message
		messageLogService.acknowledgeCorrelatedMessages(confirmMessage);
		
        // Assert
		list = mongoTemplate.find(queryAcknowledged, ShsMessageEntry.class);
		Assert.assertEquals(list.size(), 2, "both messages should have been acknowledged");
    }
}
