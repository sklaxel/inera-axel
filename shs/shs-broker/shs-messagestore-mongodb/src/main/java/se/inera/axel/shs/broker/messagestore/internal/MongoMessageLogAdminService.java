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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Order;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import se.inera.axel.shs.broker.messagestore.MessageLogAdminService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;

import javax.annotation.Resource;
import java.util.ArrayList;

@Service("messageLogAdminService")
public class MongoMessageLogAdminService implements MessageLogAdminService {

    @Resource
    private MessageLogRepository repository;

    @Autowired
    MongoTemplate mongoTemplate;

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
    public Iterable<ShsMessageEntry> findMessages(Filter filter) {
        Criteria criteria = Criteria.where("label.to.value").gt("");
//
//        if (filter.getProductIds() != null && !filter.getProductIds().isEmpty()) {
//            criteria = criteria.and("label.product.value").in(filter.getProductIds());
//        }
//
//        if (filter.getNoAck() == true) {
//            criteria = criteria.and("acknowledged").ne(true);
//        }
//
//        if (filter.getStatus() != null) {
//            criteria = criteria.and("label.status").is(filter.getStatus());
//        }


        Query query = Query.query(criteria);

//
//                Order sortOrder = Order.ASCENDING;
//                if (filter.getSortOrder() != null) {
//                    sortOrder = Order.valueOf(filter.getSortOrder().toUpperCase());
//                }
//                String sortAttribute = filter.getSortAttribute();



        query.sort().on("stateTimeStamp",Order.DESCENDING);

        query = query.limit(filter.getLimit());
        query = query.skip(filter.getSkip());


        return mongoTemplate.find(query, ShsMessageEntry.class);

    }

    @Override
    public int countMessages(Filter filter) {
        Criteria criteria = Criteria.where("label.to.value").gt("");
//
//        if (filter.getProductIds() != null && !filter.getProductIds().isEmpty()) {
//            criteria = criteria.and("label.product.value").in(filter.getProductIds());
//        }
//
//        if (filter.getNoAck() == true) {
//            criteria = criteria.and("acknowledged").ne(true);
//        }
//
//        if (filter.getStatus() != null) {
//            criteria = criteria.and("label.status").is(filter.getStatus());
//        }


        Query query = Query.query(criteria);

        return (int)mongoTemplate.count(query, ShsMessageEntry.class);

    }

    @Override
    public ShsMessageEntry findById(String messageId) {
        return repository.findOne(messageId);
    }
}
