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

import java.io.InputStream;

import com.mongodb.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.stereotype.Service;
import se.inera.axel.shs.broker.messagestore.MessageStoreService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.processor.ShsMessageMarshaller;
import se.inera.axel.shs.mime.ShsMessage;

import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import se.inera.axel.shs.xml.label.ShsLabel;

@Service("messageStoreService")
public class MongoMessageStoreService implements MessageStoreService {
	private final GridFS gridFs;

    private final ShsMessageMarshaller shsMessageMarshaller;

    @Autowired(required = true)
    public MongoMessageStoreService (@Qualifier(value = "mongoDbFactorySafe") MongoDbFactory mongoDbFactory) {
        gridFs = new GridFS(mongoDbFactory.getDb());

        // TODO overflow to disk?
        this.shsMessageMarshaller = new ShsMessageMarshaller();
    }

    @Override
    public ShsLabel save(String id, InputStream mimeStream) {
        ShsMessage message = null;

        DB db = gridFs.getDB();

        db.requestStart();
        try {
            db.requestEnsureConnection();
            saveFile(id, mimeStream);

            // TODO make sure that we do not have to parse the complete
            // message to retrieve the label
            message = findOneById(id);
        } finally {
            db.requestDone();
        }

        return message.getLabel();
    }

    @Override
	public void save(ShsMessageEntry entry, ShsMessage message) {
        InputStream messageStream = null;
        try {
            messageStream = shsMessageMarshaller.marshal(message);
        } catch (Exception e) {
            // TODO decide which exception to throw
            throw new RuntimeException("Failed to marshal SHS message", e);
        }

        // TODO decide what the filename should be
        saveFile(entry.getId(), messageStream);
	}

    private void saveFile(String id, InputStream messageStream) {
        GridFSInputFile input = gridFs.createFile(messageStream, id, true);
        input.save();
    }

    @Override
    public ShsMessage findOne(ShsMessageEntry entry) {
        ShsMessage shsMessage = findOneById(entry.getId());
        shsMessage.setLabel(entry.getLabel());

        return shsMessage;
    }

    @Override
	public ShsMessage findOneById(String id) {
        GridFSDBFile file = gridFs.findOne(id);

        if (file == null)  {
            return null;
        }

        ShsMessage message = null;
        try {
			message = shsMessageMarshaller.unmarshal(file.getInputStream());
		} catch (Exception e) {
            // TODO decide which exception to throw
			throw new RuntimeException(e);
		}

        return message;
	}

	@Override
	public InputStream findOneAsStream(ShsMessageEntry entry) {
		GridFSDBFile file = gridFs.findOne(entry.getId());
		return file.getInputStream();
	}

	@Override
	public boolean exists(ShsMessageEntry entry) {
		GridFSDBFile file = gridFs.findOne(entry.getId());
		
		return file == null;
	}

	@Override
	public void delete(ShsMessageEntry entry) {
		gridFs.remove(entry.getId());
	}

}
