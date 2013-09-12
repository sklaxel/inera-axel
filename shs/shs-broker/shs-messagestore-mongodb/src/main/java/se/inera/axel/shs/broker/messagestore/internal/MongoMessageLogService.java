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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Order;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.MessageState;
import se.inera.axel.shs.broker.messagestore.MessageStoreService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsManagementMarshaller;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.TransferType;
import se.inera.axel.shs.xml.management.ShsManagement;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

/**
 * @author Jan Hallonstén, R2M
 *
 */
@Service("messageLogService")
public class MongoMessageLogService implements MessageLogService {
    Logger log = LoggerFactory.getLogger(MongoMessageLogService.class);

    @Resource
	private MessageLogRepository messageLogRepository;

    @Autowired
	private MessageStoreService messageStoreService;

    @Autowired
    MongoTemplate mongoTemplate;
    
    private final ShsManagementMarshaller marshaller = new ShsManagementMarshaller();

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
    public ShsMessageEntry messageFetched(ShsMessageEntry entry) {
        entry.setState(MessageState.FETCHED);
        entry.setStateTimeStamp(new Date());
        return update(entry);
    }

    @Override
    public ShsMessageEntry acknowledge(ShsMessageEntry entry) {
        entry.setAcknowledged(true);
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
	public ShsMessage quarantineCorrelatedMessages(ShsMessage shsMessage) {
		
		DataPart dp = shsMessage.getDataParts().get(0);
		
		ShsManagement shsManagement = null;
		try {
			shsManagement = marshaller.unmarshal(dp.getDataHandler().getInputStream());
        } catch (Exception e) {
            // TODO decide which exception to throw
            throw new RuntimeException("Failed to marshal SHS message", e);
        }
		if (shsManagement != null) {
			if (shsManagement.getError() != null) {
				Criteria criteria = Criteria
		        		.where("label.corrId").is(shsManagement.getCorrId())
		        		.and("label.content.contentId").is(shsManagement.getContentId())
		        		.and("label.sequenceType").ne(SequenceType.ADM);
		        
		        Query query = Query.query(criteria);
		        List<ShsMessageEntry> list = mongoTemplate.find(query, ShsMessageEntry.class);
		        for (ShsMessageEntry relatedEntry : list) {
		       
					relatedEntry.setState(MessageState.QUARANTINED);
					relatedEntry.setStateTimeStamp(new Date());

					if (shsManagement.getError().getErrorcode() != null) {
						relatedEntry.setStatusCode(shsManagement.getError().getErrorcode());
					}
					if (shsManagement.getError().getErrorinfo() != null) {
						relatedEntry.setStatusText(shsManagement.getError().getErrorinfo());
					}

			        update(relatedEntry);
		        }	        
			}
		}

		return shsMessage;
	}

	@Override
	public ShsMessage acknowledgeCorrelatedMessages(ShsMessage shsMessage) {

		DataPart dp = shsMessage.getDataParts().get(0);
		
		ShsManagement shsManagement = null;
		try {
			shsManagement = marshaller.unmarshal(dp.getDataHandler().getInputStream());
        } catch (Exception e) {
            // TODO decide which exception to throw
            throw new RuntimeException("Failed to marshal SHS message", e);
        }
		if (shsManagement != null) {
			if (shsManagement.getConfirmation() != null) {
				Criteria criteria = Criteria
		        		.where("label.corrId").is(shsManagement.getCorrId())
		        		.and("label.content.contentId").is(shsManagement.getContentId())
		        		.and("label.sequenceType").ne(SequenceType.ADM);
		        
		        Query query = Query.query(criteria);
		        List<ShsMessageEntry> list = mongoTemplate.find(query, ShsMessageEntry.class);
		        for (ShsMessageEntry relatedEntry : list) {

		        	relatedEntry.setAcknowledged(true);
			        update(relatedEntry);
		        }	        
			}
		}

		return shsMessage;
	}

	@Override
	public ShsMessageEntry findEntryByShsToAndTxid(String shsTo, String txid) {
        ShsMessageEntry entry = messageLogRepository.findOneByLabelTxId(txid);
        if (entry != null && entry.getLabel() != null && entry.getLabel().getTo() != null
                && entry.getLabel().getTo().getValue() != null
                && entry.getLabel().getTo().getValue().equals(shsTo)) {
            return entry;
        } else {
            return null;
        }
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
            criteria = criteria.and("acknowledged").ne(true);
        }

        if (filter.getStatus() != null) {
            criteria = criteria.and("label.status").is(filter.getStatus());
        }

        if (filter.getEndRecipient() != null) {
            criteria = criteria.and("label.endRecipient.value").is(filter.getEndRecipient());
        }

        if (filter.getOriginator() != null) {
            criteria = criteria.and("label.originatorOrFrom.value").is(filter.getOriginator());
        }

        if (filter.getCorrId() != null) {
            criteria = criteria.and("label.corrId").is(filter.getCorrId());
        }

        if (filter.getContentId() != null) {
            criteria = criteria.and("label.content.contentId").is(filter.getContentId());
        }

        if (filter.getMetaName() != null) {
            criteria = criteria.and("label.meta.name").is(filter.getMetaName());
        }

        if (filter.getMetaValue() != null) {
            criteria = criteria.and("label.meta.value").is(filter.getMetaValue());
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
            } else if (sortAttribute.startsWith("meta-")) {
                // for now: lets sort on the meta name instead of the meta name's value
                log.warn("Sorting on meta name instead of value corresponding to meta name.");
                query.sort().on("label.meta.name", sortOrder);
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

	@Override
	public ShsMessageEntry findEntryByShsToAndTxidAndLockMessageForFetching(String shsTo, String txid) {
		
		Query query = new Query(Criteria
				.where("label.txId").is(txid)
				.and("state").is(MessageState.RECEIVED)
				.and("label.to.value").is(shsTo));
		
		Update update = new Update();
		update.set("stateTimeStamp", new Date());
		update.set("state", MessageState.FETCHING_IN_PROGRESS);

		// Enforces that the found object is returned by findAndModify(), i.e. not the original input object
		// It returns null if no document could be updated
		FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
		
		ShsMessageEntry entry = mongoTemplate.findAndModify(query, update, options,
				ShsMessageEntry.class);

		return entry;
	}

	@Override
	public void releaseFetchingInProgress() {
		// Anything older than one hour
		Date dateTime = new Date(System.currentTimeMillis() - 3600 * 1000);

		// List all FETCHING_IN_PROGRESS messages
        Query queryList = Query.query(Criteria
        		.where("label.transferType").is(TransferType.ASYNCH)
                .and("state").is(MessageState.FETCHING_IN_PROGRESS)
                .and("stateTimeStamp").lt(dateTime));
        List<ShsMessageEntry> list = mongoTemplate.find(queryList, ShsMessageEntry.class);
        
        for (ShsMessageEntry item : list) {
        	
        	// Doublecheck that it is still FETCHING_IN_PROGRESS
    		Query queryItem = new Query(Criteria
    				.where("label.txId").is(item.getLabel().getTxId())
                    .and("state").is(MessageState.FETCHING_IN_PROGRESS)
                    .and("stateTimeStamp").lt(dateTime));
    		
    		Update update = new Update();
    		update.set("stateTimeStamp", new Date());
    		update.set("state", MessageState.RECEIVED);

    		// Enforces that the found object is returned by findAndModify(), i.e. not the original input object
    		// It returns null if no document could be updated
    		FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
    		
    		ShsMessageEntry entry = mongoTemplate.findAndModify(queryItem, update, options,
    				ShsMessageEntry.class);

    		if (entry != null) {
    			log.info("ShsMessageEntry with state FETCHING_IN_PROGRESS moved back to RECEIVED [txId: " + entry.getLabel().getTxId() + "]");
    		}
        }
	}
}
