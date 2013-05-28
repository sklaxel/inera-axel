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

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import se.inera.axel.shs.protocol.DataPart;
import se.inera.axel.shs.protocol.ShsHeaders;

public class DataPartToCamelMessageProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Message in = exchange.getIn();
		Map<String, Object> headers = in.getHeaders();
		DataPart dataPart = in.getBody(DataPart.class);		
			
		headers.put(ShsHeaders.DATAPART_CONTENTLENGTH, dataPart.getContentLength());
		headers.put(ShsHeaders.DATAPART_CONTENTTYPE, dataPart.getContentType());
		headers.put(ShsHeaders.DATAPART_TRANSFERENCODING, dataPart.getTransferEncoding());
		headers.put(ShsHeaders.DATAPART_TYPE, dataPart.getDataPartType());
		headers.put(ShsHeaders.DATAPART_FILENAME, dataPart.getFileName());
		
		in.setBody(dataPart.getDataHandler().getInputStream());
		
	}

}
