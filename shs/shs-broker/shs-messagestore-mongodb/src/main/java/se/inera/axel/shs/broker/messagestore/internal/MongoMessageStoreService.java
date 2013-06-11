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

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.stereotype.Service;
import se.inera.axel.shs.broker.messagestore.MessageStoreService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.processor.ShsMessageMarshaller;
import se.inera.axel.shs.mime.ShsMessage;

import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

@Service("messageStoreService")
public class MongoMessageStoreService implements MessageStoreService {
	private final GridFS gridFs;

    private final ShsMessageMarshaller shsMessageMarshaller;

    @Autowired
    public MongoMessageStoreService (MongoDbFactory mongoDbFactory) {
        gridFs = new GridFS(mongoDbFactory.getDb());
        // TODO overflow to disk?
        this.shsMessageMarshaller = new ShsMessageMarshaller();
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
		GridFSInputFile input = gridFs.createFile(messageStream, entry.getId(), true);
        input.save();
	}

	@Override
	public ShsMessage findOne(ShsMessageEntry entry) {
        GridFSDBFile file = gridFs.findOne(entry.getId());
        ShsMessage message = null;
        try {
			message = shsMessageMarshaller.unmarshal(file.getInputStream());
		} catch (Exception e) {
            // TODO decide which exception to throw
			throw new RuntimeException(e);
		}

        message.setLabel(entry.getLabel());

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
