/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
/**
 * 
 */
package se.inera.axel.shs.messagestore.impl;

import com.natpryce.makeiteasy.Maker;
import org.apache.camel.spring.javaconfig.test.JavaConfigContextLoader;
import org.hamcrest.*;
import org.hamcrest.collection.IsIterableWithSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.util.UUID;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.fail;
import static se.inera.axel.shs.messagestore.impl.MessageLogRepositoryIT.MongoMessageLogEntryMatcher.isEqualEntryId;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.*;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.*;

/**
 * @author Jan Hallonstén, R2M
 *
 */
@ContextConfiguration(
        locations = {"se.inera.axel.shs.messagestore.impl.MongoDBTestContextConfig"},
        loader = JavaConfigContextLoader.class)
public class MessageLogRepositoryIT extends AbstractTestNGSpringContextTests {
	
	@Autowired(required = true)
	private MongoOperations mongoOperations;
	
	@Autowired(required = true)
	private MessageLogRepository repository;
	
	@Test(enabled = true)
    @DirtiesContext
	public void saveMessageStoreEntry() {
        MongoMessageLogEntry shsMessageEntry = MongoMessageLogEntry.createNewEntry(make(a(ShsLabel)));
        repository.save(shsMessageEntry);
	}

    @Test(dependsOnMethods = {"saveMessageStoreEntry"})
    @DirtiesContext
    public void testFindByLabelTxId() {
        MongoMessageLogEntry shsMessageEntry = MongoMessageLogEntry.createNewEntry(make(a(ShsLabel)));
        repository.save(shsMessageEntry);

        MongoMessageLogEntry resultEntry = repository.findOneByLabelTxId(shsMessageEntry.getLabel().getTxId());

        assertThat(resultEntry, isEqualEntryId(shsMessageEntry));
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

        MongoMessageLogEntry correlated1 = MongoMessageLogEntry.createNewEntry(make(correlatedLabel));
        MongoMessageLogEntry correlated2 = MongoMessageLogEntry.createNewEntry(make(correlatedLabel));
        MongoMessageLogEntry nonCorrelated1 = MongoMessageLogEntry.createNewEntry(make(nonCorrelatedLabel));

        repository.save(correlated1);
        repository.save(correlated2);
        repository.save(nonCorrelated1);

        Iterable<MongoMessageLogEntry> entries = repository.findByLabelCorrId(correlationId);

        assertThat(entries, is(IsIterableWithSize.<MongoMessageLogEntry>iterableWithSize(2)));
        Matcher<Iterable<MongoMessageLogEntry>> hasItemsMatcher = hasItems(isEqualEntryId(correlated1), isEqualEntryId(correlated2));
        assertThat(entries, hasItemsMatcher);
    }

	@BeforeMethod
	public void beforeMethod() {
		initDb();
	}
	
	@SuppressWarnings("unchecked")
	private void initDb() {

	}

    public static class MongoMessageLogEntryMatcher extends TypeSafeMatcher<MongoMessageLogEntry> {
        private MongoMessageLogEntry expectedEntry;

        private MongoMessageLogEntryMatcher(MongoMessageLogEntry expectedEntry) {
            this.expectedEntry = expectedEntry;
        }

        @Override
        public boolean matchesSafely(MongoMessageLogEntry actualEntry) {
            return actualEntry.getId().equals(expectedEntry.getId());
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(expectedEntry);
        }

        @Factory
        public static Matcher<MongoMessageLogEntry> isEqualEntryId(MongoMessageLogEntry expectedEntry) {
            return new MongoMessageLogEntryMatcher(expectedEntry);
        }

    }
}
