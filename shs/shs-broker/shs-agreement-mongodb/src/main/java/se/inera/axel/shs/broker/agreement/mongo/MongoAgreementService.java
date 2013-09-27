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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.axel.shs.broker.agreement.AgreementService;
import se.inera.axel.shs.broker.agreement.mongo.model.MongoShsAgreement;
import se.inera.axel.shs.broker.directory.Agreement;
import se.inera.axel.shs.broker.directory.DirectoryService;
import se.inera.axel.shs.exception.IllegalAgreementException;
import se.inera.axel.shs.exception.MissingAgreementException;
import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.exception.UnknownReceiverException;
import se.inera.axel.shs.xml.UrnProduct;
import se.inera.axel.shs.xml.agreement.*;
import se.inera.axel.shs.xml.label.*;
import se.inera.axel.shs.xml.label.Product;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("agreementService")
public class MongoAgreementService implements AgreementService {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MongoAgreementService.class);
	
	@Resource
	private MongoShsAgreementRepository mongoShsAgreementRepository;
	
	@Autowired
	private AgreementAssembler assembler;
	
	@Autowired
	private DirectoryService directoryService;
	
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
	public List<ShsAgreement> findAgreements(ShsLabel label) {

		Product product = label.getProduct();
		String productId = product.getValue();
		
		From from = label.getFrom();

		if (from == null || StringUtils.isEmpty(from.getValue())) {
			return new ArrayList<ShsAgreement>();
		}

		String fromOrgNumber = from.getValue();
		String toOrgNumber = label.getTo() != null ? label.getTo().getValue() : null;

		List<MongoShsAgreement> agreements = null;
		if (toOrgNumber == null || toOrgNumber.isEmpty()) {
			agreements = mongoShsAgreementRepository.findByProductTypeIdAndFrom(productId, fromOrgNumber);
		} else {
			agreements = mongoShsAgreementRepository.findByProductTypeIdAndFromAndTo(productId, fromOrgNumber, toOrgNumber);
		}

        if (agreements.size() > 0) {
		    return assembler.assembleShsAgreementList(agreements);
        }

        log.debug("Looking for public agreements");
        List<Agreement> publicAgreements = directoryService.findAgreements(toOrgNumber, productId);
        List<ShsAgreement> shsAgreements = new ArrayList<ShsAgreement>(publicAgreements.size());
        if (log.isDebugEnabled()) {
            log.debug("Found {} public agreements", publicAgreements.size());
        }

        for(Agreement agreement : publicAgreements) {
            // TODO catch exception and ignore?
            ShsAgreement shsAgreement = assembler.assembleShsAgreement(agreement);

            shsAgreements.add(shsAgreement);
        }

        return shsAgreements;
	}


	private List<ShsAgreement> getAgreements(String actor1, String actor2, String product) {
		List<MongoShsAgreement> agreements = mongoShsAgreementRepository.findByProductTypeIdAndFromAndTo(product, actor1, actor2);
		return assembler.assembleShsAgreementList(agreements);
	}


	@Override
	public void validateAgreement(ShsLabel label) throws ShsException {
        log.trace("validating agreement for message with label {}", label);

        if (label == null) {
            throw new IllegalArgumentException("label must be provided");
        }

        if (label.getSequenceType() == SequenceType.ADM) {
            return;
        }

		if (label.getTo() == null || StringUtils.isBlank(label.getTo().getValue())) {
			throw new UnknownReceiverException("To-address missing in message");
		}

		String recipientOrgNumber = label.getTo().getOrgNumber();
		
		log.debug("validating agreement for orgNumber {}", recipientOrgNumber);
		
		if (hasValidExplicitAgreement(label, recipientOrgNumber))
			return;
		
		if (hasValidLocalAgreement(label, recipientOrgNumber))
			return;
		
		// Look for a valid public agreement
		if (hasValidPublicAgreement(label, recipientOrgNumber))
			return;

        MissingAgreementException missingAgreementException =
                new MissingAgreementException("No valid agreement found for the message");

        missingAgreementException.setCorrId(label.getCorrId());
        if (label.getContent() != null)
            missingAgreementException.setContentId(label.getContent().getContentId());

		throw missingAgreementException;
	}

	private boolean hasValidLocalAgreement(ShsLabel label,
			String recipientOrgNumber) {
		String senderOrgNumber = getSenderOrgNumber(label);
		
		String product = label.getProduct().getValue();
		
		if (log.isDebugEnabled())
			log.debug("searching for agreement involving actor {}, actor {} and product {}", new String[] {senderOrgNumber, recipientOrgNumber, product});
		
		List<ShsAgreement> agreements = getAgreements(senderOrgNumber, recipientOrgNumber, product);
		
		for (ShsAgreement a : agreements) {
			try {
				validate(label, recipientOrgNumber, a);
			} catch (Exception e) {
				log.warn("validation failed for one agreement, continuing..", e);
				continue;
			}
			
			// Valid agreement found
			return true;
		}
		
		return false;
	}

	private String getSenderOrgNumber(ShsLabel label) {
		return label.getFrom() == null ? null : label.getFrom().getOrgNumber();
	}

	private boolean hasValidExplicitAgreement(ShsLabel label,
			String recipientOrgNumber) {
		if (label.getShsAgreement() != null) {
			ShsAgreement agreement = findOne(label.getShsAgreement());
			if (agreement == null) {
				throw new MissingAgreementException("given agreement can not be found in repository");
			}
			validate(label, recipientOrgNumber, agreement);
			return true;
		}
		
		return false;
	}

	private boolean hasValidPublicAgreement(ShsLabel label, String orgNumber) {
		List<Agreement> publicAgreements = directoryService.findAgreements(orgNumber, label.getProduct().getValue());
		
		for (Agreement publicAgreement : publicAgreements) {
			String publicTransferType = publicAgreement.getTransferType().toUpperCase(); 
			if ("ANY".equals(publicTransferType)
					|| StringUtils.isBlank(publicTransferType) // null or empty is considered == any TODO validate with specification
					|| TransferType.valueOf(publicTransferType).equals(label.getTransferType())) {
				// Valid public agreement found
				return true;
			}
		}
		
		return false;
	}

	private void validate(ShsLabel label, String orgNumber, ShsAgreement agreement) {
		
		// check if products match
		checkThatProductMatchAgreement(label, agreement);
		
		checkThatActorsMatchAgreement(label, orgNumber, agreement);
		
		isValidNow(agreement);

	}

	private void checkThatActorsMatchAgreement(ShsLabel label,
			String receiverOrgNumber, ShsAgreement agreement) {
		String flow = getFlow(agreement);
		
		String expectedSender = null;
		String expectedReceiver = null;
		
		String senderOrgNumber = getSenderOrgNumber(label);
		String principalOrgNumber = getPrincipalOrgNumber(agreement);
		String customerOrgNumber = getCustomerOrgNumber(agreement);
		
		if ("any".equalsIgnoreCase(flow)) {
			checkAnyAgreementActors(receiverOrgNumber, senderOrgNumber,
					principalOrgNumber, customerOrgNumber);
		} else {
			if ("to-customer".equalsIgnoreCase(flow)) {
				expectedSender = principalOrgNumber;
				expectedReceiver = customerOrgNumber;
			} else if ("from-customer".equalsIgnoreCase(flow)) {
				expectedSender = customerOrgNumber;
				expectedReceiver = principalOrgNumber; 
			}
		
			if (label.getSequenceType() == SequenceType.REPLY) {
				String temp = expectedSender;
				expectedSender = expectedReceiver;
				expectedReceiver = temp;
			}
			
			if (expectedSender != null && !expectedSender.equalsIgnoreCase(getSenderOrgNumber(label)))
				throw new IllegalAgreementException("Sender does not match agreement");
		
			if (expectedReceiver != null && !expectedReceiver.equalsIgnoreCase(receiverOrgNumber))
				throw new IllegalAgreementException("Receiver does not match agreement");
		}
		
	}

	private void checkAnyAgreementActors(String receiverOrgNumber,
			String senderOrgNumber, String principalOrgNumber,
			String customerOrgNumber) {
		if (senderOrgNumber.equalsIgnoreCase(principalOrgNumber)) {
			if (customerOrgNumber == null || receiverOrgNumber.equalsIgnoreCase(customerOrgNumber)) {
				return;
			}
		} else if (receiverOrgNumber.equalsIgnoreCase(principalOrgNumber)) {
			if (customerOrgNumber == null || senderOrgNumber.equalsIgnoreCase(customerOrgNumber)) {
				return;
			}
		}
		
		throw new IllegalAgreementException("Actors do not match the agreement");
	}
	
	private String getPrincipalOrgNumber(ShsAgreement agreement) {
		Shs shs = agreement.getShs();
		Principal principal = shs == null ? null : shs.getPrincipal();
		
		return principal == null ? null : principal.getValue();
	}
	
	private String getCustomerOrgNumber(ShsAgreement agreement) {
		Shs shs = agreement.getShs();
		Customer customer = shs == null ? null : shs.getCustomer();
		
		return customer == null ? null : customer.getValue();
	}

	private String getFlow(ShsAgreement agreement) {
		Shs shs = agreement.getShs();
		Direction direction = shs == null ? null : shs.getDirection();
		
		// TODO should any be default?
		return direction == null ? "any" : direction.getFlow();
	}

	private void checkThatProductMatchAgreement(ShsLabel label,
			ShsAgreement agreement) {
		String productId = label.getProduct().getValue();
		UrnProduct labelProduct = UrnProduct.valueOf(productId);
		
		List<se.inera.axel.shs.xml.agreement.Product> agreementProducts = agreement.getShs().getProduct();
		UrnProduct agreementProduct = null;
		
		for (se.inera.axel.shs.xml.agreement.Product p : agreementProducts) {
			agreementProduct = UrnProduct.valueOf(p.getValue());
			if (labelProduct.equals(agreementProduct)) {
				break;
			}
			agreementProduct = null;
		}
		
		if (agreementProduct == null) {
			throw new IllegalAgreementException("product in label does not match any product in agreement");
		}
	}

	private void isValidNow(ShsAgreement agreement) {
		General general = agreement.getGeneral();
		if (general == null)
			return;
		
		Valid valid = general.getValid();
		
		if (valid == null)
			return;
		
		ValidFrom validFrom = valid.getValidFrom();
		
		Date fromDate = validFrom == null ? null : validFrom.getDate();
		ValidTo validTo = agreement.getGeneral().getValid().getValidTo();
		Date toDate = validTo == null ? null : validTo.getDate();
		
		Date today = new Date();
		if ((fromDate != null && today.before(fromDate)) 
				|| (toDate != null && today.after(toDate))) {
			throw new IllegalAgreementException("the current time is outside validity period of agreement");
		}
	}

	
}
