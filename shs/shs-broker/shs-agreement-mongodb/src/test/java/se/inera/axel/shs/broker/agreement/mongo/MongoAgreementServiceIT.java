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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.inera.axel.shs.xml.agreement.ObjectFactory;
import se.inera.axel.shs.xml.agreement.ShsAgreement;

@ContextConfiguration
public class MongoAgreementServiceIT extends AbstractAgreementIT {
	@Autowired
	private MongoShsAgreementRepository repository;
	
	@Autowired
	private AgreementAssembler assembler;
	
	private MongoAgreementAdminService agreementAdminService;
	
	@Test(enabled=true)
	public void testMongoAgreementService() {
		ShsAgreement agreement = agreementAdminService.findOne("123456789");
		Assert.assertEquals(agreement.getUuid(), "123456789");
	}
	
	@BeforeMethod
	public void beforeMethod() {
	}

	@AfterMethod
	public void afterMethod() {
	}

	@BeforeClass
	public void beforeClass() {
		ObjectFactory objectFactory = new ObjectFactory();
		ShsAgreement agreement = objectFactory.createShsAgreement();
		agreement.setUuid("123456789");
	    
		repository.save(assembler.assembleMongoShsAgreement(agreement));
		
		agreementAdminService = new MongoAgreementAdminService();
		Assert.assertNotNull(repository);
		ReflectionTestUtils.setField(agreementAdminService, "mongoShsAgreementRepository", repository);
		ReflectionTestUtils.setField(agreementAdminService, "assembler", assembler);
	}
	
	@AfterClass
	public void afterClass() {
	}

}
