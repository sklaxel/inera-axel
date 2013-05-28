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
/**
 * 
 */
package se.inera.axel.shs.agreement.mongo;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.axel.shs.agreement.AgreementAdminService;
import se.inera.axel.shs.xml.agreement.ShsAgreement;

/**
 * @author Jan Hallonstén, R2M
 *
 */
@Service("agreementAdminService")
public class MongoAgreementAdminService implements AgreementAdminService {
	@Resource
	private MongoShsAgreementRepository mongoShsAgreementRepository;
	
	@Autowired
	private AgreementAssembler assembler;

	@Override
	public ShsAgreement findOne(String agreementId) {
		ShsAgreement agreement = null;
		MongoShsAgreement mongoShsAgreement = mongoShsAgreementRepository.findOne(agreementId);
		
		if (mongoShsAgreement != null) {
			agreement = assembler.assembleShsAgreement(mongoShsAgreement); 
		}
		
		return agreement;
	}

	@Override
	public List<ShsAgreement> findAll() {
		Iterable<MongoShsAgreement> allAgreements = mongoShsAgreementRepository.findAll();
		return assembler.assembleShsAgreementList(allAgreements);
	}

	@Override
	public void save(ShsAgreement entity) {
		MongoShsAgreement Agreement = assembler.assembleMongoShsAgreement(entity);
		mongoShsAgreementRepository.save(Agreement);
	}

	@Override
	public void delete(ShsAgreement entity) {
		MongoShsAgreement Agreement = assembler.assembleMongoShsAgreement(entity);
		mongoShsAgreementRepository.delete(Agreement);
	}

	@Override
	public void delete(String agreementId) {
		mongoShsAgreementRepository.delete(agreementId);
	}


}
