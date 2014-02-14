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

import org.apache.camel.spring.javaconfig.test.JavaConfigContextLoader;
import org.apache.commons.io.FileUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.messagestore.MessageStoreService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntryMaker;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static org.testng.Assert.*;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessage;

@ContextConfiguration(locations =
        {"se.inera.axel.shs.broker.messagestore.internal.FileTestContextConfig"},
        loader = JavaConfigContextLoader.class)
public class FileMessageStoreServiceIT extends AbstractTestNGSpringContextTests {

    MessageStoreService messageStore;

    File baseDir;

    @BeforeClass
    public void setupStore() throws IOException {
        baseDir = new File(FileUtils.getTempDirectory(),
                "testng-msgstore- " + new Random().nextLong());
        messageStore = new FileMessageStoreService(baseDir);
    }

    @AfterClass
	public void tearDownStore() throws IOException {
        FileUtils.deleteDirectory(baseDir);
	}

	@Test(groups = "largeTests", enabled = true)
    @DirtiesContext
	public void saveMessage() {
        final ShsMessageEntry entry = make(a(ShsMessageEntryMaker.ShsMessageEntry));
        final se.inera.axel.shs.mime.ShsMessage shsMessage = make(a(ShsMessage));

        messageStore.save(entry, shsMessage);

        File f = new File(baseDir, entry.getId());
        assertTrue(f.exists());
        assertTrue(f.canRead());
	}

    @Test(groups = "largeTests")
    @DirtiesContext
    public void saveAndDeleteMessage() {

        ShsMessageEntry entry = make(a(ShsMessageEntryMaker.ShsMessageEntry));
        se.inera.axel.shs.mime.ShsMessage shsMessage = make(a(ShsMessage));
        File f = new File(baseDir, entry.getId());

        entry = messageStore.save(entry, shsMessage);
        assertTrue(f.exists());
        assertTrue(f.canRead());

        messageStore.delete(entry);
        Assert.assertFalse(f.exists());

    }

    @Test(groups = "largeTests")
    @DirtiesContext
    public void deletingNonExistingFileShouldBeANoOp() {
        messageStore.delete(make(a(ShsMessageEntryMaker.ShsMessageEntry)));
    }

    @Test(groups = "largeTests")
    @DirtiesContext
    public void findOneSavedEntry() {
        final ShsMessageEntry entry = make(a(ShsMessageEntryMaker.ShsMessageEntry));
        final se.inera.axel.shs.mime.ShsMessage shsMessage = make(a(ShsMessage));

        se.inera.axel.shs.mime.ShsMessage message = messageStore.findOne(entry);
        assertNull(message);

        messageStore.save(entry, shsMessage);

        message = messageStore.findOne(entry);
        assertNotNull(message);
    }



}
