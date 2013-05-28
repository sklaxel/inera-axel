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

import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.processor.ShsMessageMarshaller;
import se.inera.axel.shs.protocol.ShsMessage;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Data format that transforms between {@link ShsMessage} and mime format in String or Input/OutputStream. 
 */
public class ShsMessageDataFormat implements DataFormat {
	Logger log = LoggerFactory.getLogger(ShsMessageDataFormat.class);

	ShsMessageMarshaller marshaller = new ShsMessageMarshaller();


	@Override
	public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
		ShsMessage shsMessage = exchange.getContext().getTypeConverter().mandatoryConvertTo(ShsMessage.class, graph);

		marshaller.marshal(shsMessage, stream);
	}

	@Override
	public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
		return marshaller.unmarshal(stream);
	}
	
}
