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
package se.inera.axel.shs.broker.messagestore.internal;

import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import org.apache.camel.spring.javaconfig.test.JavaConfigContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.DbCallback;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.messagestore.MessageStoreService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntryMaker;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsMessageMarshaller;

import java.io.IOException;
import java.io.OutputStream;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessage;

@ContextConfiguration(locations =
        {"se.inera.axel.shs.broker.messagestore.internal.MongoDBTestContextConfig"},
        loader = JavaConfigContextLoader.class)
public class MongoMessageStoreServiceIT extends AbstractTestNGSpringContextTests {

	@Autowired
    MessageStoreService messageStore;

    @Autowired
    MongoOperations mongoOperations;

    ShsMessageEntry entry1;
	
	@Test(enabled = true)
    @DirtiesContext
	public void testSave() {
        final ShsMessageEntry entry = make(a(ShsMessageEntryMaker.ShsMessageEntry));
        final se.inera.axel.shs.mime.ShsMessage shsMessage = make(a(ShsMessage));

        messageStore.save(entry, shsMessage);

        mongoOperations.execute(new DbCallback<Object>() {
            @Override
            public Object doInDB(DB db) throws MongoException, DataAccessException {
                GridFS gridFs = new GridFS(db);

                assertNotNull(gridFs.findOne(entry.getId()), "Saved file was not found in grid");

                return null;
            }
        });
	}

    @DirtiesContext
    public void existingFileShouldBeDeleted() {
        messageStore.delete(entry1);

        mongoOperations.execute(new DbCallback<Object>() {
            @Override
            public Object doInDB(DB db) throws MongoException, DataAccessException {
                GridFS gridFs = new GridFS(db);

                assertNull(gridFs.findOne(entry1.getId()), "Entry was not deleted");

                return null;
            }
        });
    }

    @DirtiesContext
    public void deletingNonExistingFileShouldBeANoOp() {
        messageStore.delete(make(a(ShsMessageEntryMaker.ShsMessageEntry)));

        mongoOperations.execute(new DbCallback<Object>() {
            @Override
            public Object doInDB(DB db) throws MongoException, DataAccessException {
                GridFS gridFs = new GridFS(db);
                DBCursor dbCursor = gridFs.getFileList();

                assertThat(dbCursor.count(), is(0));

                return null;
            }
        });
    }

    @DirtiesContext
    public void findOneSavedEntry() {
        se.inera.axel.shs.mime.ShsMessage message = messageStore.findOne(entry1);

        assertNotNull(message, "Saved message was not found");
    }

	@BeforeMethod
	public void beforeMethod() {
        mongoOperations.execute(new DbCallback<Object>() {
            @Override
            public Object doInDB(DB db) throws MongoException, DataAccessException {
                ShsMessage message = make(a(ShsMessage));
                entry1 = make(a(ShsMessageEntryMaker.ShsMessageEntry));

                saveMessage(entry1, message, db);

                return null;
            }
        });
	}

    private void saveMessage(ShsMessageEntry entry, ShsMessage message, DB db) {
        GridFS gridFs = new GridFS(db);
        GridFSInputFile inputFile = gridFs.createFile(entry.getId());

        OutputStream out = null;
        try {
            out = inputFile.getOutputStream();

            new ShsMessageMarshaller().marshal(message, out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    @AfterMethod
	public void afterMethod() {
	}

}
