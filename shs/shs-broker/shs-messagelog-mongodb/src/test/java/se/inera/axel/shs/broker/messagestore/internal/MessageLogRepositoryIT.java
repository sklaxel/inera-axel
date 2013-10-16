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
/**
 * 
 */
package se.inera.axel.shs.broker.messagestore.internal;

import com.natpryce.makeiteasy.Maker;

import org.apache.camel.spring.javaconfig.test.JavaConfigContextLoader;
import org.hamcrest.*;
import org.hamcrest.collection.IsIterableWithSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.util.UUID;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static se.inera.axel.shs.broker.messagestore.internal.MessageLogRepositoryIT.MongoMessageLogEntryMatcher.isEqualEntryId;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabel;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.corrId;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.txId;

/**
 * @author Jan Hallonstén, R2M
 *
 */
@ContextConfiguration(locations =
        {"se.inera.axel.shs.broker.messagestore.internal.MongoDBTestContextConfig"},
        loader = JavaConfigContextLoader.class)
public class MessageLogRepositoryIT extends AbstractTestNGSpringContextTests {
	
	@Autowired(required = true)
	private MongoOperations mongoOperations;
	
	@Autowired(required = true)
	private MessageLogRepository repository;
	
	@Test(enabled = true)
    @DirtiesContext
	public void saveMessageStoreEntry() {
        ShsMessageEntry shsMessageEntry = ShsMessageEntry.createNewEntry(make(a(ShsLabel)));
        repository.save(shsMessageEntry);
	}

    @Test(dependsOnMethods = {"saveMessageStoreEntry"})
    @DirtiesContext
    public void testFindByLabelTxId() {
        ShsMessageEntry shsMessageEntry = ShsMessageEntry.createNewEntry(make(a(ShsLabel)));
        repository.save(shsMessageEntry);

        ShsMessageEntry resultEntry = repository.findOneByLabelTxId(shsMessageEntry.getLabel().getTxId());

        MatcherAssert.assertThat(resultEntry, isEqualEntryId(shsMessageEntry));
    }

    @Test(dependsOnMethods = {"saveMessageStoreEntry"})
    @DirtiesContext
    public void testFindByCorrId() {
        String correlationId = UUID.randomUUID().toString();
        Maker<ShsLabel> correlatedLabel = a(ShsLabel,
                with(corrId, correlationId),
                with(txId, new RandomUUIDDonor()));

        Maker<ShsLabel> nonCorrelatedLabel = a(ShsLabel,
                with(corrId, UUID.randomUUID().toString()),
                with(txId, new RandomUUIDDonor()));

        ShsMessageEntry correlated1 = ShsMessageEntry.createNewEntry(make(correlatedLabel));
        ShsMessageEntry correlated2 = ShsMessageEntry.createNewEntry(make(correlatedLabel));
        ShsMessageEntry nonCorrelated1 = ShsMessageEntry.createNewEntry(make(nonCorrelatedLabel));

        repository.save(correlated1);
        repository.save(correlated2);
        repository.save(nonCorrelated1);

        final int maxRelatedEntries = 10;
        Pageable pageable = new PageRequest(0, maxRelatedEntries);
		Iterable<ShsMessageEntry> entries = repository.findByLabelCorrId(correlationId, pageable);

        assertThat(entries, is(IsIterableWithSize.<ShsMessageEntry>iterableWithSize(2)));
        Matcher<Iterable<ShsMessageEntry>> hasItemsMatcher = Matchers.hasItems(isEqualEntryId(correlated1), isEqualEntryId(correlated2));
        assertThat(entries, hasItemsMatcher);
    }

	@BeforeMethod
	public void beforeMethod() {
		initDb();
	}
	
	@SuppressWarnings("unchecked")
	private void initDb() {

	}

    public static class MongoMessageLogEntryMatcher extends TypeSafeMatcher<ShsMessageEntry> {
        private ShsMessageEntry expectedEntry;

        private MongoMessageLogEntryMatcher(ShsMessageEntry expectedEntry) {
            this.expectedEntry = expectedEntry;
        }

        @Override
        public boolean matchesSafely(ShsMessageEntry actualEntry) {
            return actualEntry.getId().equals(expectedEntry.getId());
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(expectedEntry);
        }

        @Factory
        public static Matcher<ShsMessageEntry> isEqualEntryId(ShsMessageEntry expectedEntry) {
            return new MongoMessageLogEntryMatcher(expectedEntry);
        }

    }
}
