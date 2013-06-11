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

import static org.dozer.loader.api.FieldsMappingOptions.customConverter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.dozer.DozerBeanMapper;
import org.dozer.factory.JAXBBeanFactory;
import org.dozer.loader.api.BeanMappingBuilder;
import org.springframework.stereotype.Component;

import se.inera.axel.shs.broker.agreement.mongo.model.*;
import se.inera.axel.shs.xml.agreement.ShsAgreement;

@Component
public class AgreementAssembler {
	private DozerBeanMapper mapper;
	
	@PostConstruct
	public void configureMapper() {
		mapper = new DozerBeanMapper();
		mapper.addMapping(createBeanMappingBuilder());
	}
	
	private BeanMappingBuilder createBeanMappingBuilder() {
		BeanMappingBuilder builder = new BeanMappingBuilder() {
			protected void configure() {
				mapping(type(se.inera.axel.shs.xml.agreement.ShsAgreement.class).beanFactory(JAXBBeanFactory.class),
						MongoShsAgreement.class);
				
				mapping(se.inera.axel.shs.xml.agreement.Billing.class, Billing.class)
					.fields(
						field("perExchangeOrPerVolumeOrPerPeriod").accessible(),
						field("perExchangeOrPerVolumeOrPerPeriod").accessible(),
						customConverter(ObjectListConverter.class));
				
				mapping(se.inera.axel.shs.xml.agreement.Open.class, Open.class)
					.fields(
							field("starttimeOrStoptime").accessible(), 
							field("starttimeOrStoptime").accessible(),
							customConverter(ObjectListConverter.class));
				
				mapping(se.inera.axel.shs.xml.agreement.Shs.class, Shs.class)
					.fields(
							field("product").accessible(), 
							field("product").accessible());
				
				mapping(se.inera.axel.shs.xml.agreement.PerExchange.class, PerExchange.class);
				mapping(se.inera.axel.shs.xml.agreement.PerVolume.class, PerVolume.class);
				mapping(se.inera.axel.shs.xml.agreement.PerPeriod.class, PerPeriod.class);
				
				mapping(se.inera.axel.shs.xml.agreement.Starttime.class, Starttime.class);
				mapping(se.inera.axel.shs.xml.agreement.Stoptime.class, Stoptime.class);
			}
		};

		return builder;
	}
	
	public ShsAgreement assembleShsAgreement(MongoShsAgreement src) {
		return mapper.map(src, ShsAgreement.class);
	}
	
	public MongoShsAgreement assembleMongoShsAgreement(ShsAgreement src) {
		return mapper.map(src, MongoShsAgreement.class);
	}
	
	public List<ShsAgreement> assembleShsAgreementList(Iterable<MongoShsAgreement> src) {
		List<ShsAgreement> dest = new ArrayList<ShsAgreement>();
		
		map(src, dest);
		
		return dest;
	}
	
	private void map(Iterable<MongoShsAgreement> src, List<ShsAgreement> dest) {
		for (MongoShsAgreement agreement : src) {
			dest.add(mapper.map(agreement, ShsAgreement.class));
		}
	}
	
	public List<MongoShsAgreement> assembleMongoShsAgreementList(List<ShsAgreement> src) {
		List<MongoShsAgreement> agreements = new ArrayList<MongoShsAgreement>(src.size());
		for (ShsAgreement agreement : src) {
			agreements.add(mapper.map(agreement, MongoShsAgreement.class));
		}
		
		return agreements;
	}
}
