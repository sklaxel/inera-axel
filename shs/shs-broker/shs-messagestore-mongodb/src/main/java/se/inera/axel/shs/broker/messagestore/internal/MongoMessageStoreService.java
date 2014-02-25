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

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.stereotype.Service;
import se.inera.axel.shs.broker.messagestore.MessageStoreService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.exception.OtherErrorException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsMessageMarshaller;
import se.inera.axel.shs.xml.label.ShsLabel;

import javax.mail.internet.SharedInputStream;
import java.io.InputStream;

@Service("messageStoreService")
public class MongoMessageStoreService implements MessageStoreService {
	private final GridFS gridFs;

    private final ShsMessageMarshaller shsMessageMarshaller;

    @Autowired(required = true)
    public MongoMessageStoreService (@Qualifier(value = "mongoDbFactorySafe") MongoDbFactory mongoDbFactory) {
        gridFs = new GridFS(mongoDbFactory.getDb());

        this.shsMessageMarshaller = new ShsMessageMarshaller();
    }

    @Override
    public ShsMessageEntry save(ShsMessageEntry entry, InputStream mimeStream) {

        DB db = gridFs.getDB();

        db.requestStart();
        try {
            db.requestEnsureConnection();
            saveFile(entry.getId(), mimeStream);

            InputStream originalMessageStream = originalMessageStream(entry);
            if (originalMessageStream == null) {
                try {
                    delete(entry);
                } catch (Exception e) {
                    // ignore
                }

                throw new OtherErrorException("Failed to save message");
            }

            ShsLabel label = shsMessageMarshaller.parseLabel(originalMessageStream);
            entry.setLabel(label);
            return entry;
        } catch (Exception e) {
            // TODO decide which exception to throw
            throw new RuntimeException("Failed to marshal SHS message", e);
        } finally {
            db.requestDone();
        }
    }


    @Override
	public ShsMessageEntry save(ShsMessageEntry entry, ShsMessage message) {
        InputStream messageStream = null;
        try {
            messageStream = shsMessageMarshaller.marshal(message);
            // TODO decide what the filename should be
            saveFile(entry.getId(), messageStream);
            entry.setLabel(message.getLabel());

            return entry;
        } catch (Exception e) {
            // TODO decide which exception to throw
            throw new RuntimeException("Failed to marshal SHS message", e);
        }
	}

    private void saveFile(String id, InputStream messageStream) {
        GridFSInputFile input = gridFs.createFile(messageStream, id, true);
        input.save();
    }

    private ShsMessage loadOriginalMessage(ShsMessageEntry entry) {

        InputStream originalMessageStream = originalMessageStream(entry);

        if (originalMessageStream == null) {
            return null;
        }

        ShsMessage message = null;
        try {
			message = shsMessageMarshaller.unmarshal(originalMessageStream);
		} catch (Exception e) {
            // TODO decide which exception to throw
            throw new RuntimeException(e);
        }

        return message;
    }

    private InputStream originalMessageStream(ShsMessageEntry entry) {
        GridFSDBFile file = gridFs.findOne(entry.getId());

        if (file == null)  {
            return null;
        }

        return new GridFsSharedInputStream(file);
    }

    private GridFSDBFile getFile(String id) {
        return gridFs.findOne(id);
    }


    @Override
    public ShsMessage findOne(ShsMessageEntry entry) {
        ShsMessage original = loadOriginalMessage(entry);

        if (original == null) {
            return null;
        }

        original.setLabel(entry.getLabel());
        return original;
    }

	@Override
	public boolean exists(ShsMessageEntry entry) {
		GridFSDBFile file = getFile(entry.getId());
		return file == null;
	}

	@Override
	public void delete(ShsMessageEntry entry) {
        gridFs.remove(getFile(entry.getId()));
		//gridFs.remove(entry.getId());
	}
}
