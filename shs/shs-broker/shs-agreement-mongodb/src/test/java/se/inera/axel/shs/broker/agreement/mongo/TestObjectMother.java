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

import se.inera.axel.shs.broker.agreement.mongo.model.*;
import se.inera.axel.shs.broker.agreement.mongo.model.Product;
import se.inera.axel.shs.xml.label.*;

import java.util.Date;

public class TestObjectMother {
	public static final String AGREEMENT_1 = "39b8dc03-8ca9-4936-9017-064c7ea151a1";
	public static final String AGREEMENT_2 = "39b8dc03-8ca9-4936-9017-064c7ea151a2";
	public static final String AGREEMENT_3 = "39b8dc03-8ca9-4936-9017-064c7ea151a3";
	// public static final String DEFAULT_TEST_BODY = "Message body";
	public static final String DEFAULT_TEST_TXID = "cee13995-88b8-4f3f-af2a-e5347b05c4c2";
	public 	static final String DEFAULT_TEST_PRODUCT_ID = "cee13995-88b8-4f3f-af2a-e5347b05c4c3";
	public static final String DEFAULT_TEST_PRODUCT_NAME = "testProduct1";
	public static final String PRODUCT_ID_2 = "cee13995-88b8-4f3f-af2a-e5347b05c4c2";
//	public static final String DEFAULT_TEST_CONTENT_ID = "cee13995-88b8-4f3f-af2a-e5347b05c4c1";
	public static final String DEFAULT_TEST_FROM = "02020202";
	public static final String DEFAULT_TEST_TO = "01010101";
	public static final String PRINCIPAL_3 = "03030303";
	public static final String DEFAULT_TEST_SUBJECT = "Subject";
//	public static final String DEFAULT_TEST_DATAPART_TYPE = "txt";
//	public static final String DEFAULT_TEST_DATAPART_CONTENTTYPE = "text/plain";
//	public static final String DEFAULT_TEST_DATAPART_FILENAME = "testfile.txt";
//	public static final String DEFAULT_TEST_DATAPART_TRANSFERENCODING = "binary";
	
	
	public static ShsLabel createShsLabel() {
		ShsLabel shsLabel = new ShsLabel();
		shsLabel.setSubject(DEFAULT_TEST_SUBJECT);
		To to = new To(); to.setvalue(DEFAULT_TEST_TO);
		shsLabel.setTo(to);
		From from = new From(); from.setvalue(DEFAULT_TEST_FROM);		
		shsLabel.getOriginatorOrFrom().add(from);		
		se.inera.axel.shs.xml.label.Product product = new se.inera.axel.shs.xml.label.Product(); product.setvalue(DEFAULT_TEST_PRODUCT_ID);
		shsLabel.setProduct(product);		
		
		Date now = new Date();
		shsLabel.setTxId(DEFAULT_TEST_TXID);
		shsLabel.setCorrId(shsLabel.getTxId());
		shsLabel.setDatetime(now);
		shsLabel.setSequenceType(SequenceType.EVENT);
		shsLabel.setStatus(Status.PRODUCTION);
		shsLabel.setTransferType(TransferType.ASYNCH);
		
		shsLabel.setMessageType(MessageType.SIMPLE);
		
		return shsLabel;
	}


    public static ShsLabel createErrorShsLabel() {
        ShsLabel shsLabel = new ShsLabel();
        shsLabel.setSubject(DEFAULT_TEST_SUBJECT);
        To to = new To(); to.setvalue(DEFAULT_TEST_TO);
        shsLabel.setTo(to);
        From from = new From(); from.setvalue(DEFAULT_TEST_FROM);
        shsLabel.getOriginatorOrFrom().add(from);
        se.inera.axel.shs.xml.label.Product product = new se.inera.axel.shs.xml.label.Product(); product.setvalue("error");
        shsLabel.setProduct(product);

        Date now = new Date();
        shsLabel.setTxId(DEFAULT_TEST_TXID);
        shsLabel.setCorrId(shsLabel.getTxId());
        shsLabel.setDatetime(now);
        shsLabel.setSequenceType(SequenceType.ADM);
        shsLabel.setStatus(Status.PRODUCTION);
        shsLabel.setTransferType(TransferType.ASYNCH);

        shsLabel.setMessageType(MessageType.SIMPLE);

        return shsLabel;
    }
	
	public static MongoShsAgreement createShsAgreement() {
		MongoShsAgreement agreement = new MongoShsAgreement();
		agreement.setUuid(AGREEMENT_1);
		
		Shs shs = new Shs();
		Product product = new Product();
		product.setvalue(DEFAULT_TEST_PRODUCT_ID);
		shs.getProduct().add(product);
		Principal principal = new Principal();
		principal.setvalue(DEFAULT_TEST_FROM);
		shs.setPrincipal(principal);
		Customer customer = new Customer();
		customer.setvalue(DEFAULT_TEST_TO);
		shs.setCustomer(customer);
		Direction direction = new Direction();
		direction.setFlow("any");
		shs.setDirection(direction);
		Confirm confirm = new Confirm();
		confirm.setRequired(false);
		shs.setConfirm(confirm);
		agreement.setShs(shs);
		
		General general = new General();
		Valid valid = new Valid();
		ValidFrom validFrom = new ValidFrom();
		validFrom.setDate(new Date());
		valid.setValidFrom(validFrom);
		general.setValid(valid);
		Schedule schedule = new Schedule();
		Intervaltime intervaltime = new Intervaltime();
		schedule.setIntervaltime(intervaltime);
		general.setSchedule(schedule);
		general.setDescription("theDescription");
		QoS qos = new QoS();
		Open open = new Open();
		When when = new When();
		when.setHours("all");
		when.setDay("every");
		open.setWhen(when);
		qos.setOpen(open);
		general.setQoS(qos);
		agreement.setGeneral(general);
		return agreement;
	}
}
