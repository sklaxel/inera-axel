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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.Billing;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.General;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.Open;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.PerExchange;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.PerPeriod;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.PerVolume;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.Product;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.QoS;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.Shs;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsAgreement;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.Starttime;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.Stoptime;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.BillingInstantiator.perExchangeOrPerVolumeOrPerPeriod;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.GeneralInstantiator.qoS;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.OpenInstantiator.starttimeOrStoptime;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.QoSInstantiator.open;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsAgreementInstantiator.general;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsAgreementInstantiator.shs;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsInstantiator.billing;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsInstantiator.products;

import java.util.Arrays;
import java.util.List;

import org.dozer.DozerBeanMapper;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import se.inera.axel.shs.broker.agreement.mongo.model.Billing;
import se.inera.axel.shs.broker.agreement.mongo.model.Confirm;
import se.inera.axel.shs.broker.agreement.mongo.model.MongoShsAgreement;
import se.inera.axel.shs.broker.agreement.mongo.model.When;
import se.inera.axel.shs.broker.directory.Agreement;
import se.inera.axel.shs.xml.agreement.ShsAgreement;
import se.inera.axel.shs.xml.agreement.Starttime;

import com.natpryce.makeiteasy.Maker;

@SuppressWarnings("unchecked")
public class AgreementAssemblerTest {
	private AgreementAssembler agreementAssembler = new AgreementAssembler();
	private DozerBeanMapper mapper;

	@BeforeMethod
	public void beforeMethod() {
	}

	@AfterMethod
	public void afterMethod() {
	}

	@BeforeClass
	public void beforeClass() {
		agreementAssembler.configureMapper();
		mapper = (DozerBeanMapper)ReflectionTestUtils.getField(agreementAssembler, "mapper");
	}

	@AfterClass
	public void afterClass() {
	}

	@Test
	public void mapConfirmRequiredWithDefault() {
		se.inera.axel.shs.xml.agreement.Confirm src = new se.inera.axel.shs.xml.agreement.Confirm();
		src.setRequired(true);
		Confirm dst = mapper.map(src, Confirm.class);
		
		Boolean dstField = (Boolean)ReflectionTestUtils.getField(dst, "required");
		assertTrue(dstField);
	}
	
	@Test
	public void mapBillingRequiredWithDefault() {
		se.inera.axel.shs.xml.agreement.Billing src = new se.inera.axel.shs.xml.agreement.Billing();
		String value = "yes";
		src.setRequired(value);
		Billing dst = mapper.map(src, Billing.class);
		
		String dstField = (String)ReflectionTestUtils.getField(dst, "required");
		assertEquals(dstField, value);
	}
	
	@Test
	public void mapHoursWithDefault() {
		se.inera.axel.shs.xml.agreement.When src = new se.inera.axel.shs.xml.agreement.When();
		String value = "all";
		src.setHours(value);
		When dst = mapper.map(src, When.class);
		
		String dstField = (String)ReflectionTestUtils.getField(dst, "hours");
		assertEquals(dstField, value);
	}

	@Test
	public void mapDayWithDefault() {
		se.inera.axel.shs.xml.agreement.When src = new se.inera.axel.shs.xml.agreement.When();
		String value = "every";
		src.setDay(value);
		When dst = mapper.map(src, When.class);
		
		String dstField = (String)ReflectionTestUtils.getField(dst, "day");
		assertEquals(dstField, value);
	}

	@Test
	public void mapShsAgreement() {
		Maker<ShsAgreement> agreementMaker = an(ShsAgreement);
		ShsAgreement shsAgreement = make(agreementMaker);
		
		shsAgreement.getGeneral().getQoS().getOpen().getWhen().setHours("all");
		shsAgreement.getGeneral().getQoS().getOpen().getWhen().setDay("every");

		MongoShsAgreement mongoShsAgreement = agreementAssembler.assembleMongoShsAgreement(shsAgreement);
		ShsAgreement shsAgreement2 = agreementAssembler.assembleShsAgreement(mongoShsAgreement);
		
		ReflectionAssert.assertReflectionEquals(shsAgreement, shsAgreement2);
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
		
		assertEquals(starttime.getValue(), orgStarttime.getValue());
	}

    @Test
    public void assembleShsAgreementFromDirectoryAgreement() {
        Agreement src = new Agreement();
        src.setDeliveryConfirmation("yes");
        src.setDescription("description");
        src.setError("error");
        src.setPrincipal("principal");
        src.setProductId("productId");
        src.setProductName("productName");
        src.setSerialNumber("serialNumber");
        src.setTransferType("transferType");

        ShsAgreement dest = agreementAssembler.assembleShsAgreement(src);

        assertTrue(dest.getShs().getConfirm().getRequired(), "Confirm should be required");
        assertEquals(dest.getGeneral().getDescription(), "description");
        assertEquals(dest.getShs().getPrincipal().getValue(), "principal");
        assertEquals(dest.getShs().getProduct().get(0).getValue(), "productId");
        assertEquals(dest.getShs().getProduct().get(0).getCommonName(), "productName");
        assertEquals(dest.getUuid(), "serialNumber");
        assertEquals(dest.getTransferType(), "transferType");
    }
}
