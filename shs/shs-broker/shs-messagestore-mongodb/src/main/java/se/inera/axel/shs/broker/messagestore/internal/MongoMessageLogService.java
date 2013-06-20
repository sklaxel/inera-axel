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
/**
 * 
 */
package se.inera.axel.shs.broker.messagestore.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Order;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.MessageState;
import se.inera.axel.shs.broker.messagestore.MessageStoreService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.TransferType;

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

    @Autowired
    MongoTemplate mongoTemplate;

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
    public ShsMessageEntry messageQuarantined(ShsMessageEntry entry, Exception exception) {

        if (exception != null) {
            entry.setStatusCode(exception.getClass().getSimpleName());
            entry.setStatusText(exception.getMessage());
        }

        entry.setState(MessageState.QUARANTINED);
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

    @Override
    public Iterable<ShsMessageEntry> listMessages(String shsTo, Filter filter) {

        Criteria criteria = Criteria.where("label.to.value").is(shsTo).
                and("label.transferType").is(TransferType.ASYNCH).
                and("state").is(MessageState.RECEIVED);

        if (filter.getProductIds() != null && !filter.getProductIds().isEmpty()) {
            criteria = criteria.and("label.product.value").in(filter.getProductIds());
        }

        if (filter.getNoAck() == true) {
            criteria = criteria.and("acknowledged").is(false);
        }

        if (filter.getStatus() != null) {
            criteria = criteria.and("label.status").is(filter.getStatus());
        }

        if (filter.getEndRecipient() != null) {
            criteria = criteria.and("label.endRecipient.value").is(filter.getEndRecipient());
        }

        if (filter.getCorrId() != null) {
            criteria = criteria.and("label.corrId").is(filter.getCorrId());
        }

        if (filter.getContentId() != null) {
            criteria = criteria.and("label.content.contentId").is(filter.getContentId());
        }

        if (filter.getSince() != null) {
            criteria = criteria.and("stateTimeStamp").gte(filter.getSince());
        }

        Query query = Query.query(criteria);

        Order sortOrder = Order.ASCENDING;
        if (filter.getSortOrder() != null) {
            sortOrder = Order.valueOf(filter.getSortOrder().toUpperCase());
        }
        String sortAttribute = filter.getSortAttribute();
        if (sortAttribute != null) {
            if (sortAttribute.equals("originator")) {
                query.sort().on("label.originatorOrFrom.value", sortOrder);
            } else if (sortAttribute.equals("from")) {
                query.sort().on("label.originatorOrFrom.value", sortOrder);
            } else if (sortAttribute.equals("endrecipient")) {
                query.sort().on("label.endRecipient.value", sortOrder);
            } else if (sortAttribute.equals("producttype")) {
                query.sort().on("label.product.value", sortOrder);
            } else if (sortAttribute.equals("subject")) {
                query.sort().on("label.subject", sortOrder);
            } else if (sortAttribute.equals("contentid")) {
                query.sort().on("label.content.contentId", sortOrder);
            } else if (sortAttribute.equals("corrid")) {
                query.sort().on("label.corrId", sortOrder);
            } else if (sortAttribute.equals("sequencetype")) {
                query.sort().on("label.sequenceType", sortOrder);
            } else if (sortAttribute.equals("transfertype")) {
                query.sort().on("label.transferType", sortOrder);
            } else {
                throw new IllegalArgumentException("Unsupported sort attribute: " + sortAttribute);
            }
        }

        Order arrivalOrder = Order.ASCENDING;
        if (filter.getArrivalOrder() != null) {
            arrivalOrder = Order.valueOf(filter.getArrivalOrder().toUpperCase());
        }
        query.sort().on("stateTimeStamp", arrivalOrder);

        if (filter.getMaxHits() != null && filter.getMaxHits() > 0)
            query = query.limit(filter.getMaxHits());

        return mongoTemplate.find(query, ShsMessageEntry.class);
    }
}
