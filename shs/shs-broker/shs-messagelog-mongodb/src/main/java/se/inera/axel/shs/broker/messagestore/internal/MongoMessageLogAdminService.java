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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import se.inera.axel.shs.broker.messagestore.MessageLogAdminService;
import se.inera.axel.shs.broker.messagestore.MessageStoreService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.ShsLabel;

@Service("messageLogAdminService")
public class MongoMessageLogAdminService implements MessageLogAdminService {
	
	private final static Logger log = LoggerFactory
			.getLogger(MongoMessageLogAdminService.class);

    @Resource
    private MessageLogRepository repository;

    @Autowired
	private MessageStoreService messageStoreService;

    @Autowired
    MongoTemplate mongoTemplate;

	@Override
	public Iterable<ShsMessageEntry> findRelatedEntries(
			ShsMessageEntry entry, int maxRelatedEntries) {
        ArrayList<ShsMessageEntry> related = new ArrayList<ShsMessageEntry>();

        if (entry == null)
            return related;

        // Fetch one more because one will get thrown away due to being the original message
        Pageable pageable = new PageRequest(0, maxRelatedEntries + 1);
        Page<ShsMessageEntry> page = repository.findByLabelCorrId(entry.getLabel().getCorrId(), pageable);
        List<ShsMessageEntry> pageContent = page.getContent();
        	
		for (ShsMessageEntry e : pageContent) {
            if (e.getId() != null && e.getId().equals(entry.getId()) == false) {
                related.add(e);
            }
        }

		return related;
	}


    @Override
    public Iterable<ShsMessageEntry> findMessages(Filter filter) {

        Criteria criteria = buildCriteria(filter);
        Query query = Query.query(criteria);

        query.with(new Sort(Sort.Direction.DESC, "label.datetime"));

        query = query.limit(filter.getLimit());
        query = query.skip(filter.getSkip());


        return mongoTemplate.find(query, ShsMessageEntry.class);

    }

    @Override
    public int countMessages(Filter filter) {

        Criteria criteria = buildCriteria(filter);
        Query query = Query.query(criteria);

        return (int)mongoTemplate.count(query, ShsMessageEntry.class);

    }

    private Criteria buildCriteria(Filter filter) {
        Criteria criteria = new Criteria();

        if (filter.getTo() != null) {
            criteria = criteria.and("label.to.value").regex("^" + filter.getTo());
        }

        if (filter.getFrom() != null) {
            criteria = criteria.and("label.originatorOrFrom.value").regex("^" + filter.getFrom());
        }

        if (filter.getTxId() != null) {
            criteria = criteria.and("label.txId").regex("^" + filter.getTxId());
        }

        if (filter.getCorrId() != null) {
            criteria = criteria.and("label.corrId").regex("^" + filter.getCorrId());
        }

        if (filter.getFilename() != null) {
            criteria = criteria.and("label.content.dataOrCompound.filename").regex("^" + filter.getFilename());
        }

        if (filter.getProduct() != null) {
            criteria = criteria.and("label.product.value").regex("^" + filter.getProduct());
        }

        if (filter.getAcknowledged() != null) {
            criteria = criteria.and("acknowledged").is(filter.getAcknowledged());
        }
        
        if (filter.getArchived() == null || filter.getArchived() == false) {
        	criteria = criteria.and("archived").is(false);

        } else if (filter.getArchived() == true) {
        	criteria = criteria.and("archived").is(true);
        }

        if (filter.getState() != null) {
            criteria = criteria.and("state").is(filter.getState());
        }

        return criteria;
    }

    @Override
    public ShsMessageEntry findById(String messageId) {
        return repository.findOne(messageId);
    }


    @Override
	public void deleteEntry(String txId) {
        ShsMessageEntry shsMessageEntry = repository.findOneByLabelTxId(txId);
        if (shsMessageEntry == null) {
            throw (new WebApplicationException());
        }

        log.info("Deleting ShsMessageEntry for txId[" + txId + "]");
        repository.delete(shsMessageEntry);

        // When deleting the entry then we even have to delete the associated file
        // in the messageStoreService. Otherwise, we would end up with orphans.
        ShsMessage shsMessage = messageStoreService.findOne(shsMessageEntry);
        if (shsMessage == null) {
            throw (new WebApplicationException());
        }
        log.info("Deleting ShsMessage for txId[" + txId + "]");
		messageStoreService.delete(shsMessageEntry);
	}

	@Override
	public ShsLabel findEntryById(String txId) {
        ShsMessageEntry entry = repository.findOneByLabelTxId(txId);
        if (entry == null) {
            throw (new WebApplicationException());
        } 

        return entry.getLabel();
	}

	@Override
	public void deleteFile(String txId) {
        ShsMessageEntry shsMessageEntry = repository.findOneByLabelTxId(txId);
        if (shsMessageEntry == null) {
            throw (new WebApplicationException());
        }
        
        ShsMessage shsMessage = messageStoreService.findOne(shsMessageEntry);
        if (shsMessage == null) {
            throw (new WebApplicationException());
        }
        log.info("Deleting ShsMessage for txId[" + txId + "]");
		messageStoreService.delete(shsMessageEntry);
	}

	@Override
	public ShsLabel findFileById(String txId) {
        ShsMessageEntry shsMessageEntry = repository.findOneByLabelTxId(txId);
        if (shsMessageEntry == null) {
            throw (new WebApplicationException());
        }
        
        ShsMessage shsMessage = messageStoreService.findOne(shsMessageEntry);
        if (shsMessage == null) {
            throw (new WebApplicationException());
        }
        
        return shsMessage.getLabel();
	}
}
