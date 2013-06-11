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
package se.inera.axel.shs.camel;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.From;
import se.inera.axel.shs.xml.label.Meta;
import se.inera.axel.shs.xml.label.Originator;
import se.inera.axel.shs.xml.label.ShsLabel;

/**
 * Converts an {@link ShsLabel} on the Camel Exchange property named {@value se.inera.axel.shs.processor.ShsHeaders#LABEL}
 * into Camel Headers defined by {@link se.inera.axel.shs.processor.ShsHeaders}.
 * <p>
 * The label object in the camel property is removed.
 */
public class ShsLabelToCamelHeadersProcessor implements Processor {
	
	@Override
	public void process(Exchange exchange) throws Exception {
		ShsLabel label = exchange.getProperty(ShsHeaders.LABEL, ShsLabel.class);
		
		Map<String, Object> headers = exchange.getIn().getHeaders();
		
		From from = null;
		Originator originator = null;
		if (!label.getOriginatorOrFrom().isEmpty()) {
			if (label.getOriginatorOrFrom().get(0) instanceof From)
				from = (From)label.getOriginatorOrFrom().get(0);
			else if (label.getOriginatorOrFrom().get(0) instanceof Originator)			
				originator = (Originator)label.getOriginatorOrFrom().get(0);
		}
		
		if (from != null) 
			headers.put(ShsHeaders.FROM, from.getvalue());
		if (originator != null) 
			headers.put(ShsHeaders.ORIGINATOR, originator.getvalue());
	
		headers.put(ShsHeaders.CORRID, label.getCorrId());
		headers.put(ShsHeaders.CONTENT_ID, label.getContent().getContentId());
		headers.put(ShsHeaders.CONTENT_COMMENT, label.getContent().getComment());
		headers.put(ShsHeaders.TXID, label.getTxId());
		headers.put(ShsHeaders.DATETIME, label.getDatetime());
		if (label.getEndRecipient() != null)
			headers.put(ShsHeaders.ENDRECIPIENT, label.getEndRecipient().getvalue());
		
		headers.put(ShsHeaders.MESSAGETYPE, "" + label.getMessageType());
		if (label.getProduct() != null)
			headers.put(ShsHeaders.PRODUCT_ID, label.getProduct().getvalue());
		headers.put(ShsHeaders.SEQUENCETYPE, "" + label.getSequenceType());
		headers.put(ShsHeaders.STATUS, "" + label.getStatus());
		headers.put(ShsHeaders.SUBJECT, label.getSubject());
		headers.put(ShsHeaders.TRANSFERTYPE, "" + label.getTransferType());
		if (label.getTo() != null)
			headers.put(ShsHeaders.TO, label.getTo().getvalue());
		
		Map<String, String> metaMap = createMetaMap(label);
		
		if (metaMap != null)
			headers.put(ShsHeaders.META, metaMap);
			
		exchange.removeProperty(ShsHeaders.LABEL);
		
	}

	private Map<String, String> createMetaMap(ShsLabel label) {
		Map<String, String> metaMap = null;
		
		if (!label.getMeta().isEmpty()) {
			metaMap = new HashMap<String, String>();
			
			for (Meta meta : label.getMeta()) {
				metaMap.put(meta.getName(), meta.getvalue());
			}
		}
		
		return metaMap;
	}
}
