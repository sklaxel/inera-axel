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

import java.io.InputStream;

import se.inera.axel.shs.mime.ShsMessage;

public interface MessageStoreService {
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

}
