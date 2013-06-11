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
package se.inera.axel.shs.broker.agreement.mongo;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;
import se.inera.axel.shs.broker.agreement.mongo.model.MongoShsAgreement;

public interface MongoShsAgreementRepository extends CrudRepository<MongoShsAgreement, String> {
	@Query("{ 'shs.product.value' : ?0, $or : [{'shs.customer.value' : ?1}, {'shs.principal.value' : ?1}] }")
	public List<MongoShsAgreement> findByProductTypeIdAndFrom(String productTypeId, String from);

	@Query("{ 'shs.product.value' : ?0, " +
			"$or : [" +
				"{$and : [{'shs.customer.value' : ?1}, {'shs.principal.value' : ?2}]}, " +
				"{$and : [{'shs.customer.value' : ?2}, {'shs.principal.value' : ?1}]}, " +
				"{$and : [{'shs.principal.value' : ?1}, {'shs.customer.value' : {$exists : false}}]}" +
				"]" +
			"}")
	public List<MongoShsAgreement> findByProductTypeIdAndFromAndTo(String productTypeId, String from, String to);
}
