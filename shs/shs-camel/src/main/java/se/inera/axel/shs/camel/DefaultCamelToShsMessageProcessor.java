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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.Content;
import se.inera.axel.shs.xml.label.Data;
import se.inera.axel.shs.xml.label.ShsLabel;

/**
 * Converts a camel message to an ShsMessage.
 * 
 * The created ShsMessage is set as the new body. The Shs headers are removed from the exchange.
 */
public class DefaultCamelToShsMessageProcessor implements Processor {
	private static final Logger log = LoggerFactory.getLogger(DefaultCamelToShsMessageProcessor.class);

	CamelHeadersToShsLabelProcessor headerProcessor = new CamelHeadersToShsLabelProcessor();
	CamelMessageToDataPartProcessor dataPartProcessor = new CamelMessageToDataPartProcessor();
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		Message in = exchange.getIn();
		
		ShsMessage shsMessage = new ShsMessage();
		
		ShsLabel shsLabel = exchange.getProperty(ShsHeaders.LABEL, ShsLabel.class);
		if (shsLabel == null) {
			log.debug("No label found in exchange creating a new label from Camel headers");
			headerProcessor.process(exchange);
			shsLabel = exchange.getProperty(ShsHeaders.LABEL, ShsLabel.class);
		}
		
		if (shsLabel == null) {
			throw new RuntimeException("Can't assemble shs message, no label found");
		}
		shsMessage.setLabel(shsLabel);
		
		
		DataPart dataPart = in.getBody(DataPart.class);
		if (dataPart == null) {
			dataPartProcessor.process(exchange);
			dataPart = in.getMandatoryBody(DataPart.class);
		}
		
		if (dataPart == null) {
			throw new RuntimeException("Can't assemble shs message, no label found");
		}
		shsMessage.addDataPart(dataPart);
		
		Content content = shsLabel.getContent();
		content.getDataOrCompound().clear();
		for (DataPart dp : shsMessage.getDataParts()) {
			Data data = new Data();
			data.setDatapartType(dp.getDataPartType());
			data.setFilename(dp.getFileName());
			if (dp.getContentLength() > 0) 
				data.setNoOfBytes("" + dp.getContentLength());
			content.getDataOrCompound().add(data);
		}
		
		in.setBody(shsMessage);
		
		// All header values have been moved to the ShsMessage so remove them from the Exchange
		// to avoid inconsistency
//		in.removeHeaders("Shs*");
//		exchange.removeProperty(ShsHeaders.LABEL);
	}
	
	
}
