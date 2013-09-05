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
package se.inera.axel.shs.broker.agreement.mongo;


import static org.testng.Assert.assertEquals;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.agreement.mongo.model.MongoShsAgreement;
import se.inera.axel.shs.broker.agreement.mongo.model.Product;

/**
 * @author Jan Hallonstén, R2M
 *
 */
@ContextConfiguration
public class MongoShsAgreementRepositoryIT extends
		AbstractAgreementIT {
	
	@Autowired
	private MongoOperations mongoOperations;
	
	@Autowired
	private MongoShsAgreementRepository mongoShsAgreementRepository;
	
	@Test
	public void defaultProductIdAndFromShouldMatchAgreement1And2() {
		List<MongoShsAgreement> agreements = mongoShsAgreementRepository.findByProductTypeIdAndFrom(TestObjectMother.DEFAULT_TEST_PRODUCT_ID, TestObjectMother.DEFAULT_TEST_FROM);
		assertEquals(agreements.size(), 2);
	}
	
	@Test
	public void agreementsShouldNotBeFoundWithNonExistingProductId() {
		List<MongoShsAgreement> agreements = mongoShsAgreementRepository.findByProductTypeIdAndFrom("NonExisting", TestObjectMother.DEFAULT_TEST_FROM);
		assertEquals(agreements.size(), 0);
	}
	
	@Test
	public void findAgreement1WithFromAndTo() {
		List<MongoShsAgreement> agreements = mongoShsAgreementRepository
				.findByProductTypeIdAndFromAndTo(TestObjectMother.DEFAULT_TEST_PRODUCT_ID,TestObjectMother.DEFAULT_TEST_FROM, TestObjectMother.DEFAULT_TEST_TO);
		
		assertEquals(agreements.size(), 1);
		
		assertEquals(agreements.get(0).getUuid(), TestObjectMother.AGREEMENT_1);
	}
	
	@Test
	public void openAgreementShouldMatchAnyCustomer() {
		List<MongoShsAgreement> agreements = mongoShsAgreementRepository
				.findByProductTypeIdAndFromAndTo(TestObjectMother.PRODUCT_ID_2, TestObjectMother.DEFAULT_TEST_FROM, "NonExisitingCustomer");
		
		assertEquals(agreements.size(), 1);
		
		assertEquals(agreements.get(0).getUuid(), TestObjectMother.AGREEMENT_3);
	}
	
	@BeforeClass
	public void beforeClass() {
		mongoOperations.dropCollection(MongoShsAgreement.class);
		initDb();
	}
	
	private void initDb() {
		MongoShsAgreement agreement = TestObjectMother.createShsAgreement();
		
		mongoShsAgreementRepository.save(agreement);
		
		agreement = TestObjectMother.createShsAgreement();
		agreement.setUuid(TestObjectMother.AGREEMENT_2);
		agreement.getShs().getCustomer().setValue(TestObjectMother.PRINCIPAL_3);
		
		mongoShsAgreementRepository.save(agreement);
		
		// Open agreement that allows any customer
		agreement = TestObjectMother.createShsAgreement();
		agreement.setUuid(TestObjectMother.AGREEMENT_3);
		agreement.getShs().getProduct().clear();
		Product product = new Product();
		product.setValue(TestObjectMother.PRODUCT_ID_2);
		agreement.getShs().getProduct().add(product);
		agreement.getShs().setCustomer(null);
		
		mongoShsAgreementRepository.save(agreement);
	}

	@AfterClass
	public void afterClass() {
		mongoOperations.dropCollection(MongoShsAgreement.class);
	}
}
