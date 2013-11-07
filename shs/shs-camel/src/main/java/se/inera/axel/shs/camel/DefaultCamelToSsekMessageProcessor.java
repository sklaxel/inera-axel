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
public class DefaultCamelToSsekMessageProcessor implements Processor {
	private static final Logger log = LoggerFactory.getLogger(DefaultCamelToSsekMessageProcessor.class);
	private static final String HELLO_WORLD_MSG = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://schemas.ssek.org/helloworld/2011-11-17\"><soapenv:Header></soapenv:Header><soapenv:Body><ns:HelloWorldRequest><ns:Message>?</ns:Message></ns:HelloWorldRequest></soapenv:Body></soapenv:Envelope>";
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		Message in = exchange.getIn();
		in.setBody(HELLO_WORLD_MSG);
	}
	
	
}
