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
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import se.inera.axel.shs.broker.messagestore.*;
import se.inera.axel.shs.exception.OtherErrorException;
import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsManagementMarshaller;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.TransferType;
import se.inera.axel.shs.xml.management.ShsManagement;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

    @Resource
    MongoTemplate mongoTemplate;
    
    private final ShsManagementMarshaller marshaller = new ShsManagementMarshaller();

    @Override
    public ShsMessageEntry saveMessageStream(InputStream mimeMessageStream) {
        String id = UUID.randomUUID().toString();

        ShsMessageEntry shsMessageEntry = new ShsMessageEntry(id);

        log.debug("saveMessageStream(InputStream) saving file with id {}", id);
        shsMessageEntry = messageStoreService.save(shsMessageEntry, mimeMessageStream);

        if (shsMessageEntry.getLabel() == null)
            throw new OtherErrorException(String.format("Could not find message with id %s after save", id));

        shsMessageEntry = saveShsMessageEntry(shsMessageEntry);

        return shsMessageEntry;
    }

	@Override
	public ShsMessageEntry saveMessage(ShsMessage message) {
        String id = UUID.randomUUID().toString();
        ShsLabel label = message.getLabel();

        ShsMessageEntry shsMessageEntry = new ShsMessageEntry(id, label);
        ShsMessageEntry entry = saveShsMessageEntry(shsMessageEntry);
		
		messageStoreService.save(entry, message);
		
		return entry;
	}

    private ShsMessageEntry saveShsMessageEntry(ShsMessageEntry entry) {
        if (entry.getLabel() == null)
            throw new IllegalArgumentException("Label must not be null");

        entry.setState(MessageState.NEW);
        entry.setStateTimeStamp(new Date());

        ShsMessageEntry existing = null;
        ShsLabel label = entry.getLabel();

        switch (label.getTransferType()) {
            case ASYNCH:
                existing = messageLogRepository.findOneByLabelTxId(label.getTxId());
                break;
            case SYNCH:
                existing = messageLogRepository.findOneByLabelSequenceTypeAndTxId(
                        label.getSequenceType(), label.getTxId());
        }

        if (existing != null) {
            messageStoreService.delete(entry);
            throw new MessageAlreadyExistsException(label, existing.getStateTimeStamp());
        }

        messageLogRepository.save(entry);
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
    public ShsMessageEntry messageAcknowledged(ShsMessageEntry entry) {
        entry.setAcknowledged(true);
        return update(entry);
    }

    @Override
    public ShsMessageEntry messageOneToMany(ShsMessageEntry entry) {
        entry.setState(MessageState.ONE_TO_MANY);
        entry.setStateTimeStamp(new Date());
        return update(entry);
    }

    @Override
    public ShsMessageEntry messageQuarantined(ShsMessageEntry entry, Exception exception) {

        if (exception instanceof ShsException) {
            ShsException shsException = (ShsException)exception;
            entry.setStatusCode(shsException.getErrorCode());
            if (shsException.getCause() != null) {
                entry.setStatusText(shsException.getErrorInfo() +
                        " (" + shsException.getCause().getMessage() + ")");
            } else {
                entry.setStatusText(shsException.getErrorInfo());
            }
        } else if (exception instanceof Exception) {
            entry.setStatusCode(exception.getClass().getSimpleName());
            if (exception.getCause() != null) {
                entry.setStatusText(exception.getMessage() +
                        " (" + exception.getCause().getMessage() + ")");
            } else {
                entry.setStatusText(exception.getMessage());
            }
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
		        		.and("label.sequenceType").ne(SequenceType.ADM)
                        .and("state").ne(MessageState.QUARANTINED);
		        
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
	public ShsMessageEntry loadEntry(String shsTo, String txId) {
        ShsMessageEntry entry = messageLogRepository.findOneByLabelTxId(txId);
        if (entry != null && entry.getLabel() != null && entry.getLabel().getTo() != null
                && entry.getLabel().getTo().getValue() != null
                && entry.getLabel().getTo().getValue().equals(shsTo)) {
            return entry;
        } else {
            throw new MessageNotFoundException(txId);
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
    public ShsMessage loadMessage(ShsMessageEntry entry) {
        ShsMessage message = messageStoreService.findOne(entry);
        if (message == null) {
            throw new MessageNotFoundException(entry.getLabel().getTxId());
        }
        return message;
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

        Sort sort = createAttributeSort(filter);

        Sort arrivalOrderSort = createArrivalOrderSort(filter);

        if (sort != null)
            sort.and(arrivalOrderSort);
        else
            sort = arrivalOrderSort;

        query.with(sort);

        if (filter.getMaxHits() != null && filter.getMaxHits() > 0)
            query = query.limit(filter.getMaxHits());
        else
            query = query.limit(200);

        return mongoTemplate.find(query, ShsMessageEntry.class);
    }

    private Sort createArrivalOrderSort(Filter filter) {
        Sort.Direction arrivalOrderDirection = Sort.Direction.ASC;

        if ("descending".equalsIgnoreCase(filter.getArrivalOrder())) {
            arrivalOrderDirection = Sort.Direction.DESC;
        }

        return new Sort(arrivalOrderDirection, "stateTimeStamp");
    }

    private Sort createAttributeSort(Filter filter) {
        Sort.Direction direction = Sort.Direction.ASC;

        if (filter.getSortOrder() == Filter.SortOrder.DESCENDING) {
            direction = Sort.Direction.DESC;
        }

        String sortAttribute = filter.getSortAttribute();

        if (sortAttribute != null) {
            if (sortAttribute.equals("originator")) {
                return new Sort(direction, "label.originatorOrFrom.value");
            } else if (sortAttribute.equals("from")) {
                return new Sort(direction, "label.originatorOrFrom.value");
            } else if (sortAttribute.equals("endrecipient")) {
                return new Sort(direction, "label.endRecipient.value");
            } else if (sortAttribute.equals("producttype")) {
                return new Sort(direction, "label.product.value");
            } else if (sortAttribute.equals("subject")) {
                return new Sort(direction, "label.subject");
            } else if (sortAttribute.equals("contentid")) {
                return new Sort(direction, "label.content.contentId");
            } else if (sortAttribute.equals("corrid")) {
                return new Sort(direction, "label.corrId");
            } else if (sortAttribute.equals("sequencetype")) {
                return new Sort(direction, "label.sequenceType");
            } else if (sortAttribute.equals("transfertype")) {
                return new Sort(direction, "label.transferType");
            } else if (sortAttribute.startsWith("meta-")) {
                // for now: lets sort on the meta name instead of the meta name's value
                log.warn("Sorting on meta name instead of value corresponding to meta name.");
                return new Sort(direction, "label.meta.name");
            } else {
                throw new IllegalArgumentException("Unsupported sort attribute: " + sortAttribute);
            }
        }

        return null;
    }

    @Override
	public ShsMessageEntry loadEntryAndLockForFetching(String shsTo, String txId) {
		
		Query query = new Query(Criteria
				.where("label.txId").is(txId)
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

        if (entry == null) {
            throw new MessageNotFoundException(txId);
        }

		return entry;
	}

	@Override
	public void releaseStaleFetchingInProgress() {
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
