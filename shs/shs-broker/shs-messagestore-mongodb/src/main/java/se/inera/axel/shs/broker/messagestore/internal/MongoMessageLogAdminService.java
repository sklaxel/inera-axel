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

import org.springframework.stereotype.Service;
import se.inera.axel.shs.broker.messagestore.MessageLogAdminService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;

import javax.annotation.Resource;
import java.util.ArrayList;

@Service("messageLogAdminService")
public class MongoMessageLogAdminService implements MessageLogAdminService {

    @Resource
    private MessageLogRepository repository;
	
	@Override
	public Iterable<ShsMessageEntry> findRelatedEntries(
			ShsMessageEntry entry) {
        ArrayList<ShsMessageEntry> related = new ArrayList<ShsMessageEntry>();

        if (entry == null)
            return related;

        for (ShsMessageEntry e : repository.findByLabelCorrId(entry.getLabel().getCorrId())) {
            if (e.getId() != null && e.getId().equals(entry.getId()) == false) {
                related.add(e);
            }
        }

		return related;
	}


    @Override
    public Iterable<ShsMessageEntry> listMessages(String shsAddress) {
        return repository.findAll();
    }

    @Override
    public ShsMessageEntry findById(String messageId) {
        return repository.findOne(messageId);
    }
}
