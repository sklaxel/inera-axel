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
package se.inera.axel.shs.xml.label;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.listOf;
import static com.natpryce.makeiteasy.Property.newProperty;

import java.util.Date;
import java.util.List;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;
import com.natpryce.makeiteasy.SameValueDonor;

/**
 * @author Jan Hallonstén, R2M
 *
 */
@SuppressWarnings("unchecked")
public class ShsLabelMaker {
	private static final ObjectFactory factory = new ObjectFactory();
	
	private static final SameValueDonor<String> NULL_STRING = new SameValueDonor<String>(null);
	private static final SameValueDonor<MessageType> NULL_MESSAGE_TYPE = new SameValueDonor<MessageType>(null);
	private static final SameValueDonor<EndRecipient> NULL_END_RECIPIENT = new SameValueDonor<EndRecipient>(null);
	
	public static final String DEFAULT_TEST_BODY = "Message body";
	public static final String DEFAULT_TEST_TXID = "cee13995-88b8-4f3f-af2a-e5347b05c4c2";
	public static final String DEFAULT_TEST_PRODUCT_ID = "00000000-0000-0000-0002-000000000001";
	public static final String DEFAULT_TEST_CONTENT_ID = "cee13995-88b8-4f3f-af2a-e5347b05c4c1";
	public static final String DEFAULT_TEST_FROM = "0000000000";
	public static final String DEFAULT_TEST_TO = "02020202";
	public static final String DEFAULT_TEST_SUBJECT = "Subject";
	public static final String DEFAULT_TEST_DATAPART_TYPE = "txt";
	public static final String DEFAULT_TEST_DATAPART_CONTENTTYPE = "text/plain";
	public static final String DEFAULT_TEST_DATAPART_FILENAME = "testfile.txt";

	public static class ShsLabelInstantiator implements Instantiator<ShsLabel> {
		public static final Property<ShsLabel, Content> content = newProperty();
		public static final Property<ShsLabel, String> corrId = newProperty();
		public static final Property<ShsLabel, Date> datetime = newProperty();
		public static final Property<ShsLabel, MessageType> documentType = newProperty();
		public static final Property<ShsLabel, EndRecipient> endRecipient = newProperty();
		public static final Property<ShsLabel, MessageType> messageType = newProperty();
		public static final Property<ShsLabel, Product> product = newProperty();
		public static final Property<ShsLabel, SequenceType> sequenceType = newProperty();
		public static final Property<ShsLabel, String> shsAgreement = newProperty();
		public static final Property<ShsLabel, Status> status = newProperty();
		public static final Property<ShsLabel, String> subject = newProperty();
		public static final Property<ShsLabel, To> to = newProperty();
		public static final Property<ShsLabel, TransferType> transferType = newProperty();
		public static final Property<ShsLabel, String> txId = newProperty();
		public static final Property<ShsLabel, String> version = newProperty();
		public static final Property<ShsLabel, List<? extends Object>> originatorOrFrom = newProperty();
		
		@Override
		public ShsLabel instantiate(
				PropertyLookup<ShsLabel> lookup) {
			ShsLabel label = factory.createShsLabel();
            label.setContent(lookup.valueOf(content, a(Content)));
            label.setCorrId(lookup.valueOf(corrId, DEFAULT_TEST_TXID));
            label.setDatetime(lookup.valueOf(datetime, new Date()));
            label.setDocumentType(lookup.valueOf(documentType, NULL_MESSAGE_TYPE));
            label.setEndRecipient(lookup.valueOf(endRecipient, NULL_END_RECIPIENT));
            label.setMessageType(lookup.valueOf(messageType, MessageType.SIMPLE));
            label.setProduct(lookup.valueOf(product, a(Product)));
            label.setSequenceType(lookup.valueOf(sequenceType, SequenceType.EVENT));
            label.setShsAgreement(lookup.valueOf(shsAgreement, NULL_STRING));
            label.setStatus(lookup.valueOf(status, Status.PRODUCTION));
            label.setSubject(lookup.valueOf(subject, DEFAULT_TEST_SUBJECT));
            label.setTo(lookup.valueOf(to, a(To)));
            label.setTransferType(lookup.valueOf(transferType, TransferType.ASYNCH));
            label.setTxId(lookup.valueOf(txId, DEFAULT_TEST_TXID));
            label.setVersion(lookup.valueOf(version, NULL_STRING));
            
            label.getOriginatorOrFrom().addAll(lookup.valueOf(originatorOrFrom, listOf(a(From))));
            // TODO add List properties
            
            return label;
		}
	}
	
	public static final ShsLabelInstantiator ShsLabel = new ShsLabelInstantiator();
	
	public static class ToInstantiator implements Instantiator<To> {
		// To
		public static final Property<To, String> commonName = newProperty();
		public static final Property<To, String> value = newProperty();
		
		@Override
		public To instantiate(
				PropertyLookup<To> lookup) {
			To to = factory.createTo();
            to.setCommonName(lookup.valueOf(commonName, NULL_STRING));
            to.setvalue(lookup.valueOf(value, DEFAULT_TEST_TO));
            
            return to;
		}
	}
	
	public static final ToInstantiator To = new ToInstantiator();
	
	public static class FromInstantiator implements Instantiator<From> {
		public static final Property<From, String> commonName = newProperty();
		public static final Property<From, String> eMail = newProperty();
		public static final Property<From, String> labeledUri = newProperty();
		public static final Property<From, String> value = newProperty();
		
		@Override
		public From instantiate(
				PropertyLookup<From> lookup) {
			From from = factory.createFrom();
            from.setCommonName(lookup.valueOf(commonName, NULL_STRING));
            from.setEMail(lookup.valueOf(eMail, NULL_STRING));
            from.setLabeledURI(lookup.valueOf(labeledUri, NULL_STRING));
            from.setvalue(lookup.valueOf(value, DEFAULT_TEST_FROM));
            
            return from;
		}
	}
	
	public static final FromInstantiator From = new FromInstantiator();
	
	public static class ProductInstantiator implements Instantiator<Product> {
		public static final Property<Product, String> commonName = newProperty();
		public static final Property<Product, String> labeledUri = newProperty();
		public static final Property<Product, String> value = newProperty();
		
		@Override
		public Product instantiate(
				PropertyLookup<Product> lookup) {
			Product product = factory.createProduct();
            product.setCommonName(lookup.valueOf(commonName, NULL_STRING));
            product.setLabeledURI(lookup.valueOf(labeledUri, NULL_STRING));
            product.setvalue(lookup.valueOf(value, DEFAULT_TEST_PRODUCT_ID));
            
            return product;
		}
	}
	
	public static final ProductInstantiator Product = new ProductInstantiator();
	
	public static class ContentInstantiator implements Instantiator<Content> {
		public static final Property<Content, String> comment = newProperty();
		public static final Property<Content, String> contentId = newProperty();
		public static final Property<Content, List<? extends Object>> dataOrCompund = newProperty();
		
		@Override
		public Content instantiate(
				PropertyLookup<Content> lookup) {
			Content content = factory.createContent();
            content.setComment(lookup.valueOf(comment, NULL_STRING));
            content.setContentId(lookup.valueOf(contentId, DEFAULT_TEST_CONTENT_ID));
            content.getDataOrCompound().addAll(lookup.valueOf(dataOrCompund, listOf(a(Data))));
            
            return content;
		}
	}
	
	public static final ContentInstantiator Content = new ContentInstantiator();
	
	public static class DataInstantiator implements Instantiator<Data> {
		public static final Property<Data, String> datapartType = newProperty();
		public static final Property<Data, String> filename = newProperty();
		public static final Property<Data, String> noOfBytes = newProperty();
		public static final Property<Data, String> noOfRecords = newProperty();
		
		@Override
		public Data instantiate(
				PropertyLookup<Data> lookup) {
			Data data = factory.createData();
            data.setDatapartType(lookup.valueOf(datapartType, DEFAULT_TEST_DATAPART_TYPE));
            data.setFilename(lookup.valueOf(datapartType, DEFAULT_TEST_DATAPART_FILENAME));
            data.setNoOfBytes(lookup.valueOf(datapartType, "" + DEFAULT_TEST_BODY.length()));
            data.setNoOfRecords(lookup.valueOf(datapartType, NULL_STRING));
            
            return data;
		}
	}
	
	public static final DataInstantiator Data = new DataInstantiator();
}
