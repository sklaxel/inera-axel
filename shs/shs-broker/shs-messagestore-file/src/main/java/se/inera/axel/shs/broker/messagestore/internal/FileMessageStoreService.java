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

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.axel.shs.broker.messagestore.MessageNotFoundException;
import se.inera.axel.shs.broker.messagestore.MessageStoreService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsMessageMarshaller;
import se.inera.axel.shs.xml.label.ShsLabel;

import javax.mail.util.SharedFileInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Service("messageStoreService")
public class FileMessageStoreService implements MessageStoreService {
    Logger log = LoggerFactory.getLogger(FileMessageStoreService.class);
	File baseDirectory;

    private final ShsMessageMarshaller shsMessageMarshaller = new ShsMessageMarshaller();

    @Autowired(required = true)
    public FileMessageStoreService(@Qualifier(value = "fileMessageStoreBaseDir") File baseDirectory) throws IOException {

        FileUtils.forceMkdir(baseDirectory);
        log.info("Using {} as message store base directory", baseDirectory);
        this.baseDirectory = baseDirectory;
    }

    @Override
    public ShsMessageEntry save(ShsMessageEntry entry, InputStream mimeStream) {

        try {

            saveFile(entry.getId(), new BufferedInputStream(mimeStream));

            // TODO make sure that we do not have to parse the complete
            // message to retrieve the label
            ShsMessage message = loadOriginalMessage(entry);
            entry.setLabel(message.getLabel());
            return entry;
        } catch (Exception e) {
            // TODO decide which exception to throw
            throw new RuntimeException("Failed to marshal SHS message", e);
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

    private void saveFile(String id, InputStream messageStream) throws IOException {
        File file = getFile(id);
        FileUtils.copyInputStreamToFile(messageStream, file);
    }

    private File getFile(String id) {
        return new File(baseDirectory, id);
    }


    private ShsMessage loadOriginalMessage(ShsMessageEntry entry) {
        if (!exists(entry)) {
            return null;
        }

        File file = getFile(entry.getId());

        try {
            return shsMessageMarshaller.unmarshal(new BufferedInputStream( new SharedFileInputStream(file)));

        } catch (IOException e) {
               // TODO decide which exception to throw
            throw new RuntimeException(e);
        }
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

        File file = getFile(entry.getId());

        return file.exists() && file.isFile();
	}

	@Override
	public void delete(ShsMessageEntry entry) {
        if (exists(entry)) {
            File file = getFile(entry.getId());
            FileUtils.deleteQuietly(file);
        }
	}

}
