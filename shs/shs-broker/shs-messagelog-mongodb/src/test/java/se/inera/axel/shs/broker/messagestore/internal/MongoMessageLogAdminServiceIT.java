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

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessage;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabel;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.To;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.corrId;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.to;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.transferType;

import java.util.Iterator;

import org.apache.camel.spring.javaconfig.test.JavaConfigContextLoader;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import se.inera.axel.shs.broker.messagestore.MessageLogAdminService;
import se.inera.axel.shs.broker.messagestore.MessageState;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.mime.ShsMessageMaker;
import se.inera.axel.shs.xml.label.Data;
import se.inera.axel.shs.xml.label.ShsLabelMaker;
import se.inera.axel.shs.xml.label.To;
import se.inera.axel.shs.xml.label.TransferType;

@ContextConfiguration(locations =
        {"se.inera.axel.shs.broker.messagestore.internal.MongoDBTestContextConfig"},
        loader = JavaConfigContextLoader.class)
public class MongoMessageLogAdminServiceIT extends AbstractMongoMessageLogTest {

    @Autowired
    MessageLogAdminService messageLogAdminService;

    @DirtiesContext
    @Test
    public void findMessageByTxid() throws Exception {
        ShsMessage message = make(a(ShsMessageMaker.ShsMessage));
        ShsMessageEntry entry = messageLogService.saveMessage(message);
        Assert.assertNotNull(entry);

        MessageLogAdminService.Filter filter = new MessageLogAdminService.Filter();
        filter.setTxId(message.getLabel().getTxId());

        Iterable<ShsMessageEntry> results = messageLogAdminService.findMessages(filter);
        Assert.assertNotNull(results);
        Assert.assertTrue(results.iterator().hasNext(), "Result has no entries");

        ShsMessageEntry result = results.iterator().next();
        Assert.assertEquals(result.getId(), entry.getId());

    }

    @DirtiesContext
    @Test
    public void findMessagesByCorrId() throws Exception {

        MessageLogAdminService.Filter filter = new MessageLogAdminService.Filter();
        filter.setCorrId("testing-corrid");

        Iterable<ShsMessageEntry> results = messageLogAdminService.findMessages(filter);
        Assert.assertNotNull(results);
        Assert.assertTrue(results.iterator().hasNext(), "Result has no entries");

        for (ShsMessageEntry result : results) {
            if (!"testing-corrid".equals(result.getLabel().getCorrId())) {
                Assert.fail("Result contains messages that don't match criteria");
            }
        }
    }

    @DirtiesContext
    @Test
    public void findRelatedMessages() throws Exception {
        ShsMessage message = make(a(ShsMessageMaker.ShsMessage));
        ShsMessageEntry entry = messageLogService.saveMessage(message);
        Assert.assertNotNull(entry);

        final int maxRelatedEntries = 5;
		// Inject more messages than MAX_RELATED_ENTRIES
        for (int i = 0; i <  maxRelatedEntries  + 10; i++) {
            messageLogService.messageReceived(
                    messageLogService.saveMessage(
                            make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                    with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                                    with(corrId, entry.getLabel().getTxId()),
                                    with(transferType, TransferType.ASYNCH)))))));
        }

        Iterable<? extends ShsMessageEntry> relatedEntries = messageLogAdminService.findRelatedEntries(entry, maxRelatedEntries);
        Iterator<? extends ShsMessageEntry> iter = relatedEntries.iterator();

        int countRelatedEntries = 0;
        for ( ; iter.hasNext() ; ++countRelatedEntries ) iter.next();
        
        Assert.assertTrue(countRelatedEntries <= maxRelatedEntries, "Exceeded max number of related entries that should get fetched");
    }

    @DirtiesContext
    @Test
    public void findMessagesByProduct() throws Exception {
        String PRODUCT_TERM = "00000";
        MessageLogAdminService.Filter filter = new MessageLogAdminService.Filter();
        filter.setProduct(PRODUCT_TERM);

        Iterable<ShsMessageEntry> results = messageLogAdminService.findMessages(filter);
        Assert.assertNotNull(results);
        Assert.assertTrue(results.iterator().hasNext(), "Result has no entries");

        for (ShsMessageEntry result : results) {

            if (!(StringUtils.containsIgnoreCase(result.getLabel().getProduct().getValue(), PRODUCT_TERM)
                    || StringUtils.containsIgnoreCase(result.getLabel().getProduct().getCommonName(), PRODUCT_TERM)))
            {
                Assert.fail("Result contains messages that don't match criteria");
            }
        }
    }

    @DirtiesContext
    @Test
    public void findMessagesByFrom() throws Exception {

        String FROM_TERM = "0000";
        MessageLogAdminService.Filter filter = new MessageLogAdminService.Filter();
        filter.setFrom(FROM_TERM);

        Iterable<ShsMessageEntry> results = messageLogAdminService.findMessages(filter);
        Assert.assertNotNull(results);
        Assert.assertTrue(results.iterator().hasNext(), "Result has no entries");

        for (ShsMessageEntry result : results) {
            if (!(StringUtils.containsIgnoreCase(result.getLabel().getFrom().getValue(), FROM_TERM)
               || StringUtils.containsIgnoreCase(result.getLabel().getFrom().getCommonName(), FROM_TERM)))
            {
                Assert.fail("Result contains messages that don't match criteria");
            }
        }
    }

    @DirtiesContext
    @Test
    public void findMessagesByTo() throws Exception {

        String TO_TERM = "0000";
        MessageLogAdminService.Filter filter = new MessageLogAdminService.Filter();
        filter.setTo(TO_TERM);

        Iterable<ShsMessageEntry> results = messageLogAdminService.findMessages(filter);
        Assert.assertNotNull(results);
        Assert.assertTrue(results.iterator().hasNext(), "Result has no entries");

        for (ShsMessageEntry result : results) {
            if (!(StringUtils.containsIgnoreCase(result.getLabel().getTo().getValue(), TO_TERM)
               || StringUtils.containsIgnoreCase(result.getLabel().getTo().getCommonName(), TO_TERM)))
            {
                Assert.fail("Result contains messages that don't match criteria");
            }
        }
    }

    @DirtiesContext
    @Test
    public void findMessagesByFilename() throws Exception {

        String FILE_TERM = "test";
        MessageLogAdminService.Filter filter = new MessageLogAdminService.Filter();
        filter.setFilename(FILE_TERM);

        Iterable<ShsMessageEntry> results = messageLogAdminService.findMessages(filter);
        Assert.assertNotNull(results);
        Assert.assertTrue(results.iterator().hasNext(), "Result has no entries");

        for (ShsMessageEntry result : results) {
            Data data = (Data)result.getLabel().getContent().getDataOrCompound().get(0);
            if (!(StringUtils.containsIgnoreCase(data.getFilename(), FILE_TERM)))
            {
                Assert.fail("Result contains messages that don't match criteria: data="
                        + data.getFilename() + " criteria: " + FILE_TERM);
            }
        }
    }

    @DirtiesContext
    @Test
    public void findMessagesByState() throws Exception {

        MessageState STATE_TERM = MessageState.RECEIVED;
        MessageLogAdminService.Filter filter = new MessageLogAdminService.Filter();
        filter.setState(STATE_TERM);

        Iterable<ShsMessageEntry> results = messageLogAdminService.findMessages(filter);
        Assert.assertNotNull(results);
        Assert.assertTrue(results.iterator().hasNext(), "Result has no entries");

        for (ShsMessageEntry result : results) {
            if (!(result.getState().equals(STATE_TERM)))
            {
                Assert.fail("Result contains messages that don't match criteria");
            }
        }
    }

    @DirtiesContext
    @Test
    public void findMessagesByAck() throws Exception {

        Boolean ACK_TERM = true;
        MessageLogAdminService.Filter filter = new MessageLogAdminService.Filter();
        filter.setAcknowledged(ACK_TERM);

        Iterable<ShsMessageEntry> results = messageLogAdminService.findMessages(filter);
        Assert.assertNotNull(results);
        Assert.assertTrue(results.iterator().hasNext(), "Result has no entries");

        for (ShsMessageEntry result : results) {
            if (!(result.isAcknowledged() == ACK_TERM))
            {
                Assert.fail("Result contains messages that don't match criteria");
            }
        }
    }

	@DirtiesContext
	@Test
	public void findMessagesByCorrIdAndAck() throws Exception {

		MessageLogAdminService.Filter filter = new MessageLogAdminService.Filter();
		filter.setCorrId("testing-corrid");

		Boolean ACK_TERM = false;
		filter.setAcknowledged(ACK_TERM);

		Iterable<ShsMessageEntry> results = messageLogAdminService
				.findMessages(filter);
		Assert.assertNotNull(results);
		Assert.assertTrue(results.iterator().hasNext(), "Result has no entries");

		for (ShsMessageEntry result : results) {
			if (!"testing-corrid".equals(result.getLabel().getCorrId())) {
				Assert.fail("Result contains messages that don't match criteria");
			}

			if (!(result.isAcknowledged() == ACK_TERM))
            {
                Assert.fail("Result contains messages that don't match criteria");
            }
		}
	}
}
