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

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.ShsLabel;

public class DefaultShsMessageToCamelProcessor implements Processor {

	ShsLabelToCamelHeadersProcessor labelProcessor = new ShsLabelToCamelHeadersProcessor();
	DataPartToCamelMessageProcessor datapartProcessor = new DataPartToCamelMessageProcessor();
	
	public void process(Exchange exchange) throws Exception {
		
		ShsMessage shsMessage = exchange.getIn().getMandatoryBody(ShsMessage.class);
		
		ShsLabel label = shsMessage.getLabel();
		
		
		Message in = exchange.getIn();
		
		if (shsMessage.getDataParts() == null || shsMessage.getDataParts().isEmpty())
			throw new RuntimeException("Shs Message contains no data parts");
		
		DataPart dataPart = shsMessage.getDataParts().get(0);
		exchange.setProperty(ShsHeaders.LABEL, label);
		
		in.setBody(dataPart);
		datapartProcessor.process(exchange);
		
		
	}
	
}
