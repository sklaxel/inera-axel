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
package se.inera.axel.shs.agreement.mongo;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.an;
import static com.natpryce.makeiteasy.MakeItEasy.listOf;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.testng.Assert.assertEquals;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.*;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsAgreementInstantiator.*;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsInstantiator.*;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.BillingInstantiator.*;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.GeneralInstantiator.*;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.QoSInstantiator.*;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.OpenInstantiator.*;

import java.util.Arrays;
import java.util.List;

import org.dozer.DozerBeanMapper;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.inera.axel.shs.xml.agreement.ShsAgreement;
import se.inera.axel.shs.xml.agreement.Starttime;


import com.natpryce.makeiteasy.Maker;

@SuppressWarnings("unchecked")
public class AgreementAssemblerTest {
	private AgreementAssembler agreementAssembler = new AgreementAssembler();

	@BeforeMethod
	public void beforeMethod() {
	}

	@AfterMethod
	public void afterMethod() {
	}

	@BeforeClass
	public void beforeClass() {
		ReflectionTestUtils.setField(agreementAssembler, "mapper", new DozerBeanMapper());
		agreementAssembler.configureMapper();
	}

	@AfterClass
	public void afterClass() {
	}

	@Test
	public void assembleShsAgreement() {
		Maker<ShsAgreement> agreementMaker = an(ShsAgreement,
				with(shs, a(Shs, 
					with(products, listOf(a(Product))
			))));
		
		ShsAgreement src = make(agreementMaker);
		
		MongoShsAgreement dest = agreementAssembler.assembleMongoShsAgreement(src);
		ShsAgreement mappedSrc = agreementAssembler.assembleShsAgreement(dest);
		
		assertEquals(mappedSrc.getShs().getProduct().size(), 1);
	}

	@Test
	public void assembleShsAgreementList() {
		List<ShsAgreement> src = Arrays.asList(make(a(ShsAgreement)));
		
		List<MongoShsAgreement> dest = agreementAssembler.assembleMongoShsAgreementList(src);
		List<ShsAgreement> mappedSrc = agreementAssembler.assembleShsAgreementList(dest);
		
		assertThat(mappedSrc, hasSize(1));
		
	}
	
	@Test
	public void agreementWithPerVolumeBillingShouldBeMappable() {
		Maker<ShsAgreement> agreementMaker = an(ShsAgreement,
				with(shs, a(Shs, 
					with(billing, a(Billing,
							with(perExchangeOrPerVolumeOrPerPeriod, listOf(a(PerVolume))))))));
		
		ShsAgreement src = make(agreementMaker);
		
		MongoShsAgreement dest = agreementAssembler.assembleMongoShsAgreement(src);
		ShsAgreement mappedSrc = agreementAssembler.assembleShsAgreement(dest);
		
		assertThat(mappedSrc.getShs().getBilling().getPerExchangeOrPerVolumeOrPerPeriod(), hasSize(1));
		assertThat(mappedSrc.getShs().getBilling().getPerExchangeOrPerVolumeOrPerPeriod(), hasItem(instanceOf(se.inera.axel.shs.xml.agreement.PerVolume.class)));
	}
	
	@Test
	public void agreementWithPerPeriodBillingShouldBeMappable() {
		Maker<ShsAgreement> agreementMaker = an(ShsAgreement,
				with(shs, a(Shs, 
					with(billing, a(Billing,
							with(perExchangeOrPerVolumeOrPerPeriod, listOf(a(PerPeriod))
				))))));
		
		ShsAgreement src = make(agreementMaker);
		
		MongoShsAgreement dest = agreementAssembler.assembleMongoShsAgreement(src);
		ShsAgreement mappedSrc = agreementAssembler.assembleShsAgreement(dest);
		
		assertThat(mappedSrc.getShs().getBilling().getPerExchangeOrPerVolumeOrPerPeriod(), hasSize(1));
		assertThat(mappedSrc.getShs().getBilling().getPerExchangeOrPerVolumeOrPerPeriod(), hasItem(instanceOf(se.inera.axel.shs.xml.agreement.PerPeriod.class)));
	}
	
	@Test
	public void agreementWithPerExchangeBillingShouldBeMappable() {
		Maker<ShsAgreement> agreementMaker = an(ShsAgreement,
				with(shs, a(Shs, 
					with(billing, a(Billing,
							with(perExchangeOrPerVolumeOrPerPeriod, listOf(a(PerExchange))))))));
		
		ShsAgreement src = make(agreementMaker);
		
		MongoShsAgreement dest = agreementAssembler.assembleMongoShsAgreement(src);
		ShsAgreement mappedSrc = agreementAssembler.assembleShsAgreement(dest);
		
		assertThat(mappedSrc.getShs().getBilling().getPerExchangeOrPerVolumeOrPerPeriod(), hasSize(1));
		assertThat(mappedSrc.getShs().getBilling().getPerExchangeOrPerVolumeOrPerPeriod(), hasItem(instanceOf(se.inera.axel.shs.xml.agreement.PerExchange.class)));
	}
	
	@Test
	public void agreementWithStarttimeAndStopTimeShouldBeMappable() {
		Maker<ShsAgreement> agreementMaker = an(ShsAgreement,
				with(general, a(General,
						with(qoS, a(QoS,
								with(open, a(Open,
										with(starttimeOrStoptime, listOf(a(Starttime), a(Stoptime))))))))));
		
		ShsAgreement src = make(agreementMaker);
		
		MongoShsAgreement dest = agreementAssembler.assembleMongoShsAgreement(src);
		ShsAgreement mappedSrc = agreementAssembler.assembleShsAgreement(dest);
		
		assertThat(mappedSrc.getGeneral().getQoS().getOpen().getStarttimeOrStoptime(), hasSize(2));
		assertThat(mappedSrc.getGeneral().getQoS().getOpen().getStarttimeOrStoptime().get(0), instanceOf(se.inera.axel.shs.xml.agreement.Starttime.class));
		assertThat(mappedSrc.getGeneral().getQoS().getOpen().getStarttimeOrStoptime().get(1), instanceOf(se.inera.axel.shs.xml.agreement.Stoptime.class));
		
		Starttime starttime = (Starttime)mappedSrc.getGeneral().getQoS().getOpen().getStarttimeOrStoptime().get(0);
		Starttime orgStarttime = (Starttime)src.getGeneral().getQoS().getOpen().getStarttimeOrStoptime().get(0);
		
		assertEquals(starttime.getvalue(), orgStarttime.getvalue());
	}
}
