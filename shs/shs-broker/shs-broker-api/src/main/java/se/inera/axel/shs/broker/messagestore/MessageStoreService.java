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
package se.inera.axel.shs.broker.messagestore;

import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.io.InputStream;

/**
 * Handles the physical storage of SHS messages. <p/>
 * By contrast to {@link MessageLogService}, that only stores the message headers
 * and status, this service handles the storage of the actual message.
 * <p/>
 * This allows for these two different kind of services to be implemented with different storage backends, depending on requirements.
 */
public interface MessageStoreService {
    ShsLabel save(String id, InputStream mimeStream);

	void save(ShsMessageEntry entry, ShsMessage message);
	
	ShsMessage findOne(ShsMessageEntry entry);

    /**
     * Retrieves a message as a stream by entry.
     *
     * @param entry the entry for the message, must not be null
     * @return a stream from which the message can be read or <code>null</code>
     * if the message was not found.
     */
	InputStream findOneAsStream(ShsMessageEntry entry);


	boolean exists(ShsMessageEntry entry);
	
	void delete(ShsMessageEntry entry);

    ShsMessage findOneById(String id);
}
