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
import com.natpryce.makeiteasy.Maker;
import org.apache.camel.spring.javaconfig.test.JavaConfigContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.messagestore.*;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsManagementMarshaller;
import se.inera.axel.shs.xml.label.*;
import se.inera.axel.shs.xml.management.Confirmation;
import se.inera.axel.shs.xml.management.Error;
import se.inera.axel.shs.xml.management.ObjectFactory;
import se.inera.axel.shs.xml.management.ShsManagement;

import javax.activation.DataHandler;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.testng.Assert.*;
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
	
	@Autowired
	private MessageStoreService messageStoreService;

    @Test(groups = "largeTests")
    public void saveStreamShouldCreateEntry() {
        InputStream mimeMessageStream = new BufferedInputStream(this.getClass().getResourceAsStream("/shsTextMessage.mime"));

        assertNotNull(mimeMessageStream);

        ShsMessageEntry entry = messageLogService.saveMessageStream(null, mimeMessageStream);

        ShsMessage shsMessage = messageLogService.loadMessage(entry);

        assertEquals(shsMessage.getLabel().getTxId(), "4c9fd3e8-b4c4-49aa-926a-52a68864a7b8", "Transaction id does not match");
    }
    
    @Test(groups = "largeTests")
    public void saveMessageShouldCreateEntry() throws Exception {
        ShsMessage message = make(a(ShsMessage));
        ShsMessageEntry entry = messageLogService.saveMessage(message);

        Assert.assertNotNull(entry);
        assertEquals(entry.getLabel().getTxId(), message.getLabel().getTxId());
    }

    @Test(groups = "largeTests")
    public void savedAndFetchedMessageShouldBeTheSame() throws Exception {
        ShsMessage message = make(a(ShsMessage));
        ShsMessageEntry entry = messageLogService.saveMessage(message);

        Assert.assertNotNull(entry);
        assertEquals(entry.getLabel().getTxId(), message.getLabel().getTxId());

        ShsMessage fetchedMessage = messageLogService.loadMessage(entry);
        Assert.assertNotNull(fetchedMessage);
        assertEquals(fetchedMessage.getLabel().getTxId(), entry.getLabel().getTxId());

    }


    @Test(expectedExceptions = MessageAlreadyExistsException.class)
    public void saveAsynchMessageWithSameTxIdShouldThrow() throws Exception {
        ShsMessage message = make(a(ShsMessage));
        ShsMessageEntry entry = messageLogService.saveMessage(message);

        // ok
        Assert.assertNotNull(entry);
        assertEquals(entry.getLabel().getTxId(), message.getLabel().getTxId());

        entry = messageLogService.saveMessage(message);
        // not ok, should throw.
    }

    @Test(groups = "largeTests", expectedExceptions = MessageAlreadyExistsException.class)
    public void saveSynchMessageWithSameTxIdShouldThrow() throws Exception {
        ShsMessage message = make(synchronousMessage());

        ShsMessageEntry entry1 = messageLogService.saveMessage(message);

        // ok
        Assert.assertNotNull(entry1);
        assertEquals(entry1.getLabel().getTxId(), message.getLabel().getTxId());

        // should be ok to save a synchronous reply with the same txId.
        message.getLabel().setSequenceType(SequenceType.REPLY);
        ShsMessageEntry entry2 = messageLogService.saveMessage(message);

        // ok
        Assert.assertNotNull(entry2);
        assertEquals(entry2.getLabel().getTxId(), message.getLabel().getTxId());
        assertEquals(entry2.getLabel().getSequenceType(), SequenceType.REPLY);
        Assert.assertNotEquals(entry2.getId(), entry1.getId());

        // should not be ok to save a new request with the same txId.
        message.getLabel().setSequenceType(SequenceType.REQUEST);
        messageLogService.saveMessage(message);
        // not ok, should throw.
    }


    @Test(groups = "largeTests")
    public void findEntryByShsToAndTxid() throws Exception {
        ShsMessage message = make(a(ShsMessage));
        ShsMessageEntry entry = messageLogService.saveMessage(message);
        Assert.assertNotNull(entry);
        ShsMessageEntry resultEntry = messageLogService.loadEntry(message.getLabel().getTo().getValue(), message.getLabel().getTxId());
        Assert.assertNotNull(resultEntry);
        assertEquals(resultEntry.getLabel().getTo().getValue(), message.getLabel().getTo().getValue());
    }

    @Test(groups = "largeTests", expectedExceptions = MessageNotFoundException.class)
    public void findEntryByShsToAndTxidWithFaultShsToShouldReturnNone() throws Exception {
        ShsMessage message = make(a(ShsMessage));
        ShsMessageEntry entry = messageLogService.saveMessage(message);
        Assert.assertNotNull(entry);
        ShsMessageEntry resultEntry = messageLogService.loadEntry("1111111111", message.getLabel().getTxId());
        Assert.assertNull(resultEntry);
    }


    @Test(groups = "largeTests")
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

    @Test(groups = "largeTests")
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

    @Test(groups = "largeTests")
    public void listMessagesShouldOnlyReturnRECEIVEDMessages() throws Exception {

        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO,
                        new MessageLogService.Filter());


        Assert.assertNotNull(iter);

        for (ShsMessageEntry entry : iter) {
            Assert.assertTrue(entry.getState() == MessageState.RECEIVED, "Only RECEIVED messages should be returned");
        }
    }

    @Test(groups = "largeTests")
    public void listMessagesWithCorrectShsAddress() throws Exception {

        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO,
                        new MessageLogService.Filter());


        Assert.assertNotNull(iter);
        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        assertEquals(list.size(), 4, "correct 'to'-address with empty filter should return 4 messages");
    }

    @Test(groups = "largeTests")
    public void listMessagesWithOneProductId() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();
        filter.getProductIds().add("confirm");

        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO, filter);

        Assert.assertNotNull(iter);
        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        assertEquals(list.size(), 1, "only 1 'confirm' messages should be returned in message list");
    }

    @Test(groups = "largeTests")
    public void listMessagesWithTwoProductId() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();
        filter.getProductIds().add("confirm");
        filter.getProductIds().add("error");

        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO, filter);

        Assert.assertNotNull(iter);
        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        assertEquals(list.size(), 2, "1 'error' and 1 'confirm' should be returned");
    }

    @Test(groups = "largeTests")
    public void listMessagesWithMaxHits() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();

        filter.setMaxHits(2);
        Iterable<ShsMessageEntry> iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO, filter);

        Assert.assertNotNull(iter);
        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        assertEquals(list.size(), 2, "filter with maxHits 2 should return 2 messages");

    }

    @Test(groups = "largeTests")
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
        assertEquals(listOfNotAcked.size(), listOfAll.size() - 1,
                "filter with noAck should return 1 less than all");

    }

    @Test(groups = "largeTests")
    public void listMessagesWithStatus() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();

        filter.setStatus(Status.TEST);
        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_TO, filter);

        Assert.assertNotNull(iter);
        List<ShsMessageEntry> list = Lists.newArrayList(iter);

        assertEquals(list.size(), 1, "exactly 1 test message should exist");

    }

    @Test(groups = "largeTests")
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

        assertEquals(list.size(), 1,
                "exactly 1 message should be addressed to end recipient " + ShsLabelMaker.DEFAULT_TEST_ENDRECIPIENT);

    }

    @Test(groups = "largeTests")
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

        assertEquals(list.size(), 1,
                "exactly 1 message should be addressed from originator " + ShsLabelMaker.DEFAULT_TEST_ORIGINATOR);
    }

    @Test(groups = "largeTests")
    public void listMessagesWithCorrId() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();
        filter.setCorrId("testing-corrid");
        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        assertEquals(list.size(), 1, "exactly 1 message with given corrId should exist");

    }

    @Test(groups = "largeTests")
    public void listMessagesWithContentId() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();
        filter.setContentId("testing-contentid");
        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        assertEquals(list.size(), 1, "exactly 1 message with given contentId should exist");

    }

    @Test(groups = "largeTests")
    public void listMessagesWithMeta() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();
        filter.setMetaName("namn");
        filter.setMetaValue("varde");
        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        List<ShsMessageEntry> list = Lists.newArrayList(iter);
        assertEquals(list.size(), 1, "exactly 1 message with given meta data should exist");


        filter.setMetaName("namn");
        filter.setMetaValue(null);
        iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        list = Lists.newArrayList(iter);
        assertEquals(list.size(), 1, "exactly 1 message with given meta data name should exist");


        filter.setMetaName(null);
        filter.setMetaValue("varde");
        iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        list = Lists.newArrayList(iter);
        assertEquals(list.size(), 1, "exactly 1 message with given meta data value should exist");

        filter.setMetaName(ShsLabelMaker.DEFAULT_TEST_META_NAME);
        filter.setMetaValue(ShsLabelMaker.DEFAULT_TEST_META_VALUE);
        iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        list = Lists.newArrayList(iter);
        Assert.assertTrue(list.size() > 1, "more than 1 message with default meta data value should exist");

    }

    @Test(groups = "largeTests")
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
        assertEquals(sizeWithSince, sizeWithoutSince - 1,
                "sizeWithSince should be one less than sizeWithoutSince");

    }


    @Test(groups = "largeTests")
    public void listMessagesWithArrivalOrder() throws Exception {

    	// s1 will have an EARLIER label.datetime than s2
        ShsMessageEntry s1 = messageLogService.saveMessage(
                make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                		with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                        with(transferType, TransferType.ASYNCH))))));

        ShsMessageEntry s2 = messageLogService.saveMessage(
                make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                		with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),    
                        with(transferType, TransferType.ASYNCH))))));

    	// s1 will have a LATER stateTimeStamp than s2
        messageLogService.messageReceived(s2);
        messageLogService.messageReceived(s1);

        // Test ASCENDING which is the default order
        MessageLogService.Filter filter = new MessageLogService.Filter();
        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        Date lastLabelDateTime = null;
        
        for(Iterator<ShsMessageEntry> i = iter.iterator(); i.hasNext(); ) {
            ShsMessageEntry item = i.next();
            Date labelDateTime = item.getLabel().getDatetime();
            
            if (lastLabelDateTime != null) {
            	boolean isAscending = labelDateTime.compareTo(lastLabelDateTime) >= 0;
            	Assert.assertTrue(isAscending, "sort order is incorrect");
            }

            lastLabelDateTime = labelDateTime;
        }

        // Test ASCENDING with specifically specifying sort order
        filter = new MessageLogService.Filter();
        filter.setArrivalOrder("ascending");
        iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        lastLabelDateTime = null;
        
        for(Iterator<ShsMessageEntry> i = iter.iterator(); i.hasNext(); ) {
            ShsMessageEntry item = i.next();
            Date labelDateTime = item.getLabel().getDatetime();
            
            if (lastLabelDateTime != null) {
            	boolean isAscending = labelDateTime.compareTo(lastLabelDateTime) >= 0;
            	Assert.assertTrue(isAscending, "sort order is incorrect");
            }

            lastLabelDateTime = labelDateTime;
        }

        // Test DESCENDING
        filter = new MessageLogService.Filter();
        filter.setArrivalOrder("descending");
        iter = messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);

        lastLabelDateTime = null;
        
        for(Iterator<ShsMessageEntry> i = iter.iterator(); i.hasNext(); ) {
            ShsMessageEntry item = i.next();
            Date labelDateTime = item.getLabel().getDatetime();
            
            if (lastLabelDateTime != null) {
            	boolean isAscending = labelDateTime.compareTo(lastLabelDateTime) >= 0;
            	Assert.assertFalse(isAscending, "sort order is incorrect");
            }

            lastLabelDateTime = labelDateTime;
        }
    }

    @Test(groups = "largeTests")
    public void fetchTwiceShouldFail() throws Exception {

    	// Create message
        ShsMessage message = make(a(ShsMessage));
        ShsMessageEntry entry = messageLogService.saveMessage(message);
        Assert.assertNotNull(entry);
        assertEquals(entry.getState(), MessageState.NEW);
        
        messageLogService.messageReceived(entry);
        assertEquals(entry.getState(), MessageState.RECEIVED);
        
        // Fetch message
        ShsMessageEntry entry_1 = messageLogService.loadEntryAndLockForFetching(message.getLabel().getTo().getValue(), message.getLabel().getTxId());
        Assert.assertNotNull(entry_1);
        assertEquals(entry_1.getState(), MessageState.FETCHING_IN_PROGRESS);
        
        // Fetch message a second time which should fail
        try {
            ShsMessageEntry entry_2 = messageLogService.loadEntryAndLockForFetching(message.getLabel().getTo().getValue(), message.getLabel().getTxId());
            Assert.fail("loadEntryAndLockForFetching should throw MessageNotFoundException");
        } catch (MessageNotFoundException notfound) {
            
        }
    }

    
    @Test(groups = "largeTests")
    public void releaseFetchingInProgress() {

    	// ------------------------------------------------------------
    	// Message 1
    	// Create message
        ShsMessage message_1 = make(a(ShsMessage));
        ShsMessageEntry entry_1 = messageLogService.saveMessage(message_1);
        Assert.assertNotNull(entry_1);
        assertEquals(entry_1.getState(), MessageState.NEW);
        
        messageLogService.messageReceived(entry_1);
        assertEquals(entry_1.getState(), MessageState.RECEIVED);
        
        // Fetch message
        ShsMessageEntry entry_1_found = messageLogService.loadEntryAndLockForFetching(message_1.getLabel().getTo().getValue(), message_1.getLabel().getTxId());
        Assert.assertNotNull(entry_1_found);
        assertEquals(entry_1_found.getState(), MessageState.FETCHING_IN_PROGRESS);

    	// ------------------------------------------------------------
        // Message 2
    	// Create message
        ShsMessage message_2 = make(a(ShsMessage));
        ShsMessageEntry entry_2 = messageLogService.saveMessage(message_2);
        Assert.assertNotNull(entry_2);
        assertEquals(entry_2.getState(), MessageState.NEW);
        
        messageLogService.messageReceived(entry_2);
        assertEquals(entry_2.getState(), MessageState.RECEIVED);
        
        // Fetch message
        ShsMessageEntry entry_2_found = messageLogService.loadEntryAndLockForFetching(message_2.getLabel().getTo().getValue(), message_2.getLabel().getTxId());
        Assert.assertNotNull(entry_2_found);
        assertEquals(entry_2_found.getState(), MessageState.FETCHING_IN_PROGRESS);

    	// ------------------------------------------------------------
        // Update the timestamp for both messages so that it looks like that they have been in state FETCHING_IN_PROGRESS for a longer time than what messageLogService.releaseStaleFetchingInProgress() expects
		Date stateTimeStamp = new Date(System.currentTimeMillis() - 3600 * 1000 - 1);
        entry_1_found.setStateTimeStamp(stateTimeStamp);
        messageLogService.update(entry_1_found);
        
        entry_2_found.setStateTimeStamp(stateTimeStamp);
        messageLogService.update(entry_2_found);

        // Check that FETCHING_IN_PROGRESS is released for both messages
        messageLogService.releaseStaleFetchingInProgress();

        entry_1_found = messageLogService.loadEntry(message_1.getLabel().getTo().getValue(), message_1.getLabel().getTxId());
        assertEquals(entry_1_found.getState(), MessageState.RECEIVED);

        entry_2_found = messageLogService.loadEntry(message_2.getLabel().getTo().getValue(), message_2.getLabel().getTxId());
        assertEquals(entry_2_found.getState(), MessageState.RECEIVED);
    }
   
    @Test(groups = "largeTests", expectedExceptions = IllegalArgumentException.class)
    public void listMessagesWithFaultyArrivalOrderShouldThrow() throws Exception {

        MessageLogService.Filter filter = new MessageLogService.Filter();
        filter.setArrivalOrder("asc");
        Iterable<ShsMessageEntry> iter =
                messageLogService.listMessages(ShsLabelMaker.DEFAULT_TEST_FROM, filter);
    }

    @Test(groups = "largeTests")
    public void receivedErrorShouldQuarantineMessages() {

		// ------------------------------------------------------------
    	// Inject two SHS messages with same corrId & contentId
    	ShsLabel label1 = make(a(ShsLabel));
        ShsMessage message1 = make(a(ShsMessage, with(ShsMessage.label, label1)));
        messageLogService.saveMessage(message1);

        ShsLabel label2 = make(a(ShsLabel, 
				with(corrId, label1.getCorrId()),
				with(content, make(a(Content,
						with(Content.contentId, label1.getContent().getContentId()))))));
		ShsMessage message2 = make(a(ShsMessage, with(ShsMessage.label, label2))); 
        messageLogService.saveMessage(message2);
    	
        // Assert
		Query queryQuarantinedMessages = new Query(Criteria
				.where("label.corrId").is(label1.getCorrId())
        		.and("label.content.contentId").is(label1.getContent().getContentId())
        		.and("state").is(MessageState.QUARANTINED));
		List<ShsMessageEntry> list = mongoTemplate.find(queryQuarantinedMessages, ShsMessageEntry.class);
		assertEquals(list.size(), 0, "no messages should have been quarantined");
		
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
		assertEquals(list.size(), 2, "both messages should have been quarantined");
    }

    @Test(groups = "largeTests")
    public void receivedConfirmShouldAcknowledgeMessages() {

		// ------------------------------------------------------------
    	// Inject two SHS messages with same corrId & contentId
    	ShsLabel label1 = make(a(ShsLabel));
        ShsMessage message1 = make(a(ShsMessage, with(ShsMessage.label, label1)));
        messageLogService.saveMessage(message1);

        ShsLabel label2 = make(a(ShsLabel, 
				with(corrId, label1.getCorrId()),
				with(content, make(a(Content,
						with(Content.contentId, label1.getContent().getContentId()))))));
		ShsMessage message2 = make(a(ShsMessage, with(ShsMessage.label, label2))); 
        messageLogService.saveMessage(message2);

        // Assert
		Query queryAcknowledged = new Query(Criteria
				.where("label.corrId").is(label1.getCorrId())
        		.and("label.content.contentId").is(label1.getContent().getContentId())
        		.and("acknowledged").is(true));
		List<ShsMessageEntry> list = mongoTemplate.find(queryAcknowledged, ShsMessageEntry.class);
		assertEquals(list.size(), 0, "no message should have been acknowledged");

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
		assertEquals(list.size(), 2, "both messages should have been acknowledged");
    }
    
    @Test(groups = "largeTests")
    public void oldStateTimeStampsShouldArchiveMessages() throws InterruptedException {
        // Drop all previous message entries to have a known state
        mongoTemplate.dropCollection(ShsMessageEntry.class);

    	//Create three messages that should be archived

    	// Message 1-------------------------------------------------------
    	// Create message
        ShsMessageEntry entry1 = messageLogService.saveMessage(make(synchronousMessage()));
        
        //set the state of the message to SENT
        entry1.setState(MessageState.SENT);
        messageLogService.messageSent(entry1);
        
        entry1 = messageLogService.update(entry1);
        
        Assert.assertNotNull(entry1);
        assertEquals(entry1.getState(), MessageState.SENT);
        
        // Message 2-------------------------------------------------------
    	// Create message

        ShsMessageEntry entry2 = messageLogService.saveMessage(make(synchronousMessage()));
        
        //set the state of the message to RECEIVED
        entry2.setState(MessageState.RECEIVED);
        messageLogService.messageReceived(entry2);
        
        entry2 = messageLogService.update(entry2);
        
        Assert.assertNotNull(entry2);
        assertEquals(entry2.getState(), MessageState.RECEIVED);
        
        // Message 3-------------------------------------------------------
    	// Create message
        ShsMessageEntry entry3 = messageLogService.saveMessage(make(synchronousMessage()));
        
        //set the state of the message to FETCHED
        entry3.setState(MessageState.FETCHED);
        messageLogService.messageFetched(entry3);
        
        entry3 = messageLogService.update(entry3);
        
        Assert.assertNotNull(entry3);
        assertEquals(entry3.getState(), MessageState.FETCHED);
        
        // Archive messages that are older than a second
        Thread.sleep(3L * 1000); // Sleep so that the messages are older than a second
        messageLogService.archiveMessages(1L);
        
        //assert
        Query queryArchivedMessages = 
        		new Query(Criteria
        				.where("archived").is(true));
        
		List<ShsMessageEntry> list = mongoTemplate.find(queryArchivedMessages, ShsMessageEntry.class);
		assertEquals(list.size(), 3, "3 messages should have been archived");
    }

    protected Maker<ShsMessage> synchronousMessage() {
        return a(ShsMessage,with(ShsMessage.label, a(ShsLabel,with(sequenceType, SequenceType.REQUEST),with(transferType, TransferType.SYNCH))));
    }

    @Test(groups = "largeTests")
    public void oldArchivedShouldRemoveMessages() {
    	
    	//limit
    	long old = 3600L; 
    	
    	// Message 1-------------------------------------------------------
    	// Create message
        ShsMessage message_1 = make(a(ShsMessage));
        ShsMessageEntry entry_1 = messageLogService.saveMessage(message_1);
        
        //mark as sent
        entry_1.setState(MessageState.SENT);
        messageLogService.messageSent(entry_1);
        
        //mark as archived
        entry_1.setArchived(true);
        
        //set stateTimeStamp
        Date stateTimeStamp_1 = new Date(System.currentTimeMillis() - 3600 * 1000 * 10);
        entry_1.setStateTimeStamp(stateTimeStamp_1);
        messageLogService.update(entry_1);
        
        
        messageLogService.archiveMessages(old);
        
        //check that the parameters is set correctly
        Assert.assertNotNull(entry_1);
        assertEquals(entry_1.getState(), MessageState.SENT);
        assertEquals(entry_1.isArchived(), true);
        assertNotNull(entry_1.getId(), "entry ID not found");
        assertNotNull(messageLogService.loadMessage(entry_1), "no message found");
                
        //inject 
        messageLogService.removeArchivedMessages(old);
        
        //assert
        Query queryArchivedMessages = 
        		new Query(Criteria
        				.where("archived").is(true));
        

		List<ShsMessageEntry> list = mongoTemplate.find(queryArchivedMessages, ShsMessageEntry.class);
//
		assertFalse(messageStoreService.exists(entry_1), "messagefile not removed");
		
		
		
		
    }
    
    @Test(groups = "largeTests")
    public void succesfullMessagesShouldBeRemoved() {
    	
    	//Create three messages that are successfull and should be removed
    	
    	// Message 1-------------------------------------------------------
    	// Create message
        ShsMessage message_1 = make(a(ShsMessage));
        ShsMessageEntry entry_1 = messageLogService.saveMessage(message_1);
        
        //mark the message as SENT
        entry_1.setState(MessageState.SENT);
        messageLogService.messageSent(entry_1);

        messageLogService.update(entry_1);
        
        Assert.assertNotNull(entry_1);
//        assertTrue(messageStoreService.exists(entry_1), "Message is expected to exist");
        assertEquals(entry_1.getState(), MessageState.SENT);
    	
    	// Message 2-------------------------------------------------------
    	// Create message
        ShsMessage message_2 = make(a(ShsMessage));
        ShsMessageEntry entry_2 = messageLogService.saveMessage(message_2);
        
        //mark the message as RECEIVED
        entry_2.setState(MessageState.RECEIVED);
        messageLogService.messageReceived(entry_2);
        
        messageLogService.update(entry_2);
        
        Assert.assertNotNull(entry_2);
//        assertTrue(messageStoreService.exists(entry_2), "Message is expected to exist");
        assertEquals(entry_2.getState(), MessageState.RECEIVED);
    	
    	// Message 3-------------------------------------------------------
    	// Create message
        ShsMessage message_3 = make(a(ShsMessage));
        ShsMessageEntry entry_3 = messageLogService.saveMessage(message_3);
        
        //mark the message as QUARANTINED
        entry_3.setState(MessageState.QUARANTINED);
        messageLogService.messageQuarantined(entry_3, new Exception());
        
        messageLogService.update(entry_3);
        
        Assert.assertNotNull(entry_3);
//        assertTrue(messageStoreService.exists(entry_3), "Message is expected to exist");
        assertEquals(entry_3.getState(), MessageState.QUARANTINED);
        
        //inject 
        messageLogService.removeSuccessfullyTransferredMessages();
        
        //assert
        assertFalse(messageStoreService.exists(entry_1), "not removed correctly");
        assertFalse(messageStoreService.exists(entry_2), "not removed correctly");
        assertFalse(messageStoreService.exists(entry_3), "not removed correctly");
        
		
    	
    }

    @Test(groups = "largeTests")
    public void archivedAndRemovedMessagesShouldRemoveEntries () {
    	
    	//how old before considered old
    	Long oldLimit = 3600L;
    	
    	//create messages that will simulate that a message have been removed from the entry
    	// Message 1-------------------------------------------------------
    	// Create message
        ShsMessage message_1 = make(a(ShsMessage));
        ShsMessageEntry entry_1 = messageLogService.saveMessage(message_1);

        //set timestamp for messagentry
        entry_1.setState(MessageState.SENT);
        entry_1.setArchived(true);
        Date stateTimeStamp_1 = new Date(System.currentTimeMillis() - 3600 * 10000); //old message
        entry_1.setStateTimeStamp(stateTimeStamp_1);
       
        messageLogService.deleteMessage(entry_1);
        messageLogService.update(entry_1);
        assertEquals(entry_1.getState(), MessageState.SENT);
        
        // Message 2-------------------------------------------------------
    	// Create message
        ShsMessage message_2 = make(a(ShsMessage));
        ShsMessageEntry entry_2 = messageLogService.saveMessage(message_2);

        //set timestamp for messagentry
        entry_2.setState(MessageState.SENT);
        entry_2.setArchived(true);
        Date stateTimeStamp_2 = new Date(System.currentTimeMillis() - 3600 * 10000); //old message
        entry_2.setStateTimeStamp(stateTimeStamp_2);
       
        messageLogService.deleteMessage(entry_2);
        messageLogService.update(entry_2);
        assertEquals(entry_2.getState(), MessageState.SENT);
        
        messageLogService.removeArchivedMessageEntries(oldLimit);
        
        //assert
        Query query = 
        		new Query(Criteria
        				.where("id").ne(null).orOperator(Criteria.where("id").is("testEntry"),
        												  Criteria.where("id").is("testEntry2")));
        
        List<ShsMessageEntry> list = mongoTemplate.find(query, ShsMessageEntry.class);
        
        assertEquals(list.size(), 0, "0 entries should have been found");
    	
    }
    
    
    
}
