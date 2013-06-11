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
package se.inera.axel.shs.broker.messagestore.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.MessageState;
import se.inera.axel.shs.broker.messagestore.MessageStoreService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.mime.ShsMessage;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Jan Hallonstén, R2M
 *
 */
@Service("messageLogService")
public class MongoMessageLogService implements MessageLogService {
    @Resource
	private MessageLogRepository messageLogRepository;

    @Autowired
	private MessageStoreService messageStoreService;

	/* (non-Javadoc)
	 * @see se.inera.axel.shs.messagestore.MessageStore#save(ShsMessage)
	 */
	@Override
	public ShsMessageEntry createEntry(ShsMessage message) {
        ShsMessageEntry entry = ShsMessageEntry.createNewEntry(message.getLabel());
		entry.setState(MessageState.NEW);
		entry.setStateTimeStamp(new Date());
		
		messageLogRepository.save(entry);
		
		messageStoreService.save(entry, message);
		
		return entry;
	}

    @Override
    public ShsMessageEntry messageReceived(ShsMessageEntry entry) {
        entry.setState(MessageState.RECEIVED);
        entry.setStateTimeStamp(new Date());
        return update(entry);
    }

    @Override
    public ShsMessageEntry messageSent(ShsMessageEntry entry) {
        entry.setState(MessageState.SENT);
        entry.setStateTimeStamp(new Date());
        return update(entry);
    }

    @Override
	public ShsMessageEntry findEntry(String id) {
		return messageLogRepository.findOne(id);
	}

	@Override
	public ShsMessageEntry findEntryByTxid(String txid) {
		return messageLogRepository.findOneByLabelTxId(txid);
	}

	@Override
	public ShsMessageEntry update(ShsMessageEntry entry) {
		if (entry instanceof ShsMessageEntry) {
			messageLogRepository.save((ShsMessageEntry) entry);
		} else {
			throw new IllegalArgumentException("The given message store entry is not supported by this message store");
		}

        return entry;
	}

    @Override
    public ShsMessage fetchMessage(ShsMessageEntry entry) {
        return  messageStoreService.findOne(entry);
    }
}
