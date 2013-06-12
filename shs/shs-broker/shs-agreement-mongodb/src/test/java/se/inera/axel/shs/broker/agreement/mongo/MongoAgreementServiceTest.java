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
package se.inera.axel.shs.broker.agreement.mongo;

import com.natpryce.makeiteasy.Maker;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.agreement.mongo.model.MongoShsAgreement;
import se.inera.axel.shs.broker.directory.Agreement;
import se.inera.axel.shs.broker.directory.DirectoryService;
import se.inera.axel.shs.exception.MissingAgreementException;
import se.inera.axel.shs.xml.agreement.ShsAgreement;
import se.inera.axel.shs.xml.agreement.ShsAgreementMaker;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.util.Arrays;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.*;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.DirectionInstantiator.flow;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsAgreementInstantiator.shs;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsInstantiator.direction;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.*;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.originatorOrFrom;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.to;

public class MongoAgreementServiceTest {
	private Agreement anyAgreement;
	private Agreement synchAgreement;
	private MongoAgreementService agreementService;
	private AgreementAssembler assembler;
	private MongoShsAgreementRepository repository;
	private DirectoryService directoryService;
	
	
	@Test
	public void validateAgreementShouldLookForPublicAgreementIfNoLocalAgreementIsFound() {
		when(directoryService.findAgreements(anyString(), anyString())).thenReturn(Arrays.asList(anyAgreement));
		
		agreementService.validateAgreement(TestObjectMother.createShsLabel());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void aFromCustomerAgreementWithCorrectCustomerAndPrincipalShouldBeValid() {
		Maker<ShsAgreement> agreementMaker = an(ShsAgreement,
				with(shs, a(Shs,
						with(direction, a(Direction,
								with(flow, "from-customer"))))));
		
		MongoShsAgreement agreement = assembler.assembleMongoShsAgreement(make(agreementMaker));
		
		when(repository.findByProductTypeIdAndFromAndTo(anyString(), anyString(), anyString())).thenReturn(Arrays.asList(agreement));
		
		Maker<ShsLabel> labelMaker = an(ShsLabel,
				with(originatorOrFrom, listOf(a(From,
						with(From.value, ShsAgreementMaker.DEFAULT_CUSTOMER)))),
				with(to, a(To,
						with(To.value, ShsAgreementMaker.DEFAULT_PRINCIPAL))));
		
		agreementService.validateAgreement(make(labelMaker));
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expectedExceptions=MissingAgreementException.class)
	public void aFromCustomerAgreementShouldNotBeValidWhenCustomerAndPrincipalAreReversed() {
		Maker<ShsAgreement> agreementMaker = an(ShsAgreement,
				with(shs, a(Shs,
						with(direction, a(Direction,
								with(flow, "from-customer"))))));
		
		MongoShsAgreement agreement = assembler.assembleMongoShsAgreement(make(agreementMaker));
		
		when(repository.findByProductTypeIdAndFromAndTo(anyString(), anyString(), anyString())).thenReturn(Arrays.asList(agreement));
		
		Maker<ShsLabel> labelMaker = an(ShsLabel,
				with(originatorOrFrom, listOf(a(From,
						with(From.value, ShsAgreementMaker.DEFAULT_PRINCIPAL)))),
				with(to, a(To,
						with(To.value, ShsAgreementMaker.DEFAULT_CUSTOMER))));
		
		agreementService.validateAgreement(make(labelMaker));
		
	}
	
	@Test(expectedExceptions=MissingAgreementException.class)
	public void validateAgreementShouldThrowMissingAgreementExceptionIfNoValidAgreementIsFound() {
		agreementService.validateAgreement(TestObjectMother.createShsLabel());
	}
	
	@Test(expectedExceptions=MissingAgreementException.class)
	public void validateAgreementShouldThrowMissingAgreementExceptionIfPublicAgreementHasIncorrectTransferType() {
		when(directoryService.findAgreements(anyString(), anyString())).thenReturn(Arrays.asList(synchAgreement));
		
		agreementService.validateAgreement(TestObjectMother.createShsLabel());
	}

    @Test
    public void validateAgreementShouldNotThrowMissingAgreementExceptionIfAdminMessage() {
        //when(directoryService.findAgreements(anyString(), anyString())).thenReturn(Arrays.asList(synchAgreement));

        agreementService.validateAgreement(TestObjectMother.createErrorShsLabel());
    }

    @Test
    public void validateAgreementShouldThrowMissingAgreementExceptionWithFields() {
        when(directoryService.findAgreements(anyString(), anyString())).thenReturn(Arrays.asList(synchAgreement));
        ShsLabel label = TestObjectMother.createShsLabel();
        try {
            agreementService.validateAgreement(label);
            Assert.fail("Should have thrown exception");
        } catch (MissingAgreementException e) {
            Assert.assertEquals(e.getCorrId(), label.getCorrId());
        }

    }
	
	@BeforeClass
	public void beforeClass() {
		assembler = new AgreementAssembler();
		assembler.configureMapper();
		
		agreementService = new MongoAgreementService();
		
		ReflectionTestUtils.setField(agreementService, "assembler", assembler);
	}
	
	@BeforeMethod
	public void beforeMethod() {
		repository = mock(MongoShsAgreementRepository.class);
		ReflectionTestUtils.setField(agreementService, "mongoShsAgreementRepository", repository);
		
		directoryService = mock(DirectoryService.class);
		ReflectionTestUtils.setField(agreementService, "directoryService", directoryService);
		
		createNewAgreements();
	}
	
	public void createNewAgreements() {
		anyAgreement = new Agreement();
		anyAgreement.setSerialNumber(TestObjectMother.AGREEMENT_3);
		anyAgreement.setPrincipal(TestObjectMother.DEFAULT_TEST_TO);
		anyAgreement.setProductName(TestObjectMother.DEFAULT_TEST_PRODUCT_NAME);
		anyAgreement.setProductId(TestObjectMother.DEFAULT_TEST_PRODUCT_ID);
		anyAgreement.setTransferType("any");
		
		synchAgreement = new Agreement();
		synchAgreement.setSerialNumber(TestObjectMother.AGREEMENT_3);
		synchAgreement.setPrincipal(TestObjectMother.DEFAULT_TEST_TO);
		synchAgreement.setProductName(TestObjectMother.DEFAULT_TEST_PRODUCT_NAME);
		synchAgreement.setProductId(TestObjectMother.DEFAULT_TEST_PRODUCT_ID);
		synchAgreement.setTransferType("synch");
	}

}
