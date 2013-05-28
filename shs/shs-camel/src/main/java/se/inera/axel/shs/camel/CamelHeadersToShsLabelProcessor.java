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
package se.inera.axel.shs.camel;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import se.inera.axel.shs.protocol.ShsHeaders;
import se.inera.axel.shs.xml.label.Content;
import se.inera.axel.shs.xml.label.EndRecipient;
import se.inera.axel.shs.xml.label.From;
import se.inera.axel.shs.xml.label.MessageType;
import se.inera.axel.shs.xml.label.Meta;
import se.inera.axel.shs.xml.label.Originator;
import se.inera.axel.shs.xml.label.Product;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.Status;
import se.inera.axel.shs.xml.label.To;
import se.inera.axel.shs.xml.label.TransferType;

public class CamelHeadersToShsLabelProcessor implements Processor {

	@SuppressWarnings("unchecked")
	@Override
	public void process(Exchange exchange) throws Exception {
		Message in = exchange.getIn();
		
		ShsLabel label = new ShsLabel();
		
		label.setSubject(in.getHeader(ShsHeaders.SUBJECT, String.class));				
		label.setTo(CamelHeadersToShsLabelProcessor.convertStringToTo(in.getHeader(ShsHeaders.TO, String.class)));
		
		From from = CamelHeadersToShsLabelProcessor.convertStringToFrom(in.getHeader(ShsHeaders.FROM, String.class));		
		if (from != null) {
			label.getOriginatorOrFrom().add(from);						
		} 

		Originator originator = CamelHeadersToShsLabelProcessor.convertStringToOriginator(in.getHeader(ShsHeaders.ORIGINATOR, String.class));
		if (originator != null) {
			label.getOriginatorOrFrom().add(originator);	
		}
		 
		EndRecipient endRecipient = CamelHeadersToShsLabelProcessor.convertStringToEndRecipient(in.getHeader(ShsHeaders.ENDRECIPIENT, String.class));
		label.setEndRecipient(endRecipient);
		label.setProduct(CamelHeadersToShsLabelProcessor.convertStringToProduct(in.getHeader(ShsHeaders.PRODUCT_ID, String.class)));
		label.setTxId(in.getHeader(ShsHeaders.TXID, UUID.randomUUID(), String.class));
		label.setCorrId(in.getHeader(ShsHeaders.CORRID, label.getTxId(), String.class));
		label.setDatetime(in.getHeader(ShsHeaders.DATETIME, new Date(), Date.class));
		
		label.setStatus(in.getHeader(ShsHeaders.STATUS, Status.PRODUCTION, Status.class));
		
		SequenceType sequenceType = in.getHeader(ShsHeaders.SEQUENCETYPE, SequenceType.class);
		if (sequenceType == null) {
			switch (exchange.getPattern()) {
			case InOnly:
			case RobustInOnly:
				sequenceType = SequenceType.EVENT;
				break;
			case InOut:
				sequenceType = SequenceType.REQUEST;
				break;
			default:
				throw new RuntimeException("Unsupported exchange pattern: " + exchange.getPattern());
			}
		}
		label.setSequenceType(sequenceType);
		
		TransferType transferType = in.getHeader(ShsHeaders.TRANSFERTYPE, TransferType.class);
		if (transferType == null) {
			switch (exchange.getPattern()) {
			case InOnly:
				transferType = TransferType.ASYNCH;
				break;
			case InOut:
			case RobustInOnly:
				transferType = TransferType.SYNCH;
				break;
			default:
				throw new RuntimeException("Unsupported exchange pattern: " + exchange.getPattern());
			}
		}
		label.setTransferType(transferType);
		
		label.setMessageType(in.getHeader(ShsHeaders.MESSAGETYPE, MessageType.SIMPLE, MessageType.class));
		
		Content content = new Content();
		content.setContentId(in.getHeader(ShsHeaders.CONTENT_ID, UUID.randomUUID(), String.class));
		content.setComment(in.getHeader(ShsHeaders.CONTENT_COMMENT, String.class));
		
		label.setContent(content);

		addMetaToLabel(label, in.getHeader(ShsHeaders.META, Map.class));
		
		exchange.setProperty(ShsHeaders.LABEL, label);
		in.removeHeaders("ShsLabel*");
		
	}

	private void addMetaToLabel(ShsLabel label, Map<String, String> metaMap) {
		label.getMeta().clear();

		if (metaMap != null) {
			for (Map.Entry<String, String> metaEntry : metaMap.entrySet()) {
				label.getMeta().add(convertToMeta(metaEntry));
			}
		}
	}

	private static Meta convertToMeta(Map.Entry<String, String> metaEntry) {
		Meta meta = new Meta();
		meta.setName(metaEntry.getKey());
		meta.setvalue(metaEntry.getValue());
		return meta;
	}

	private static Product convertStringToProduct(String s) {
		if (s == null)
			return null;
		Product product = new Product();
		product.setvalue(s);
		return product;
	}

	private static From convertStringToFrom(String s) {
		if (s == null)
			return null;
		From from = new From();
		from.setvalue(s);
		return from;
	}

	private static To convertStringToTo(String s) {
		if (s == null)
			return null;
		To to = new To();
		to.setvalue(s);
		return to;
	}

	private static Originator convertStringToOriginator(String s) {
		if (s == null)
			return null;
		Originator originator = new Originator();
		originator.setvalue(s);
		return originator;
	}

	private static EndRecipient convertStringToEndRecipient(String s) {
		if (s == null)
			return null;
		EndRecipient endRecipient = new EndRecipient();
		endRecipient.setvalue(s);
		return endRecipient;
	}

}
