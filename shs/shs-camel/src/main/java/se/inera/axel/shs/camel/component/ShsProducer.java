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
package se.inera.axel.shs.camel.component;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultProducer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.axel.shs.processor.AxelHeaders;
import se.inera.axel.shs.processor.ShsHeaders;

import java.util.Map;

/**
 * The HelloWorld producer.
 */
public class ShsProducer extends DefaultProducer {
    private static final transient Logger log = LoggerFactory.getLogger(ShsProducer.class);
    private ShsEndpoint endpoint;
    private ProducerTemplate producerTemplate;
    
    public ShsProducer(ShsEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
        this.producerTemplate = endpoint.getCamelContext().createProducerTemplate();
    }

    public void process(final Exchange inExchange) throws Exception {
    	Exchange returnedExchange = producerTemplate.send(getDestinationUri(inExchange), ExchangePattern.InOut, new Processor() {

			@Override
			public void process(Exchange exchange) throws Exception {
				Object body = inExchange.getIn().getBody();

                exchange.getIn().setHeader(AxelHeaders.ROBUST_ASYNCH_SHS, inExchange.getIn().getHeader(AxelHeaders.ROBUST_ASYNCH_SHS));
                exchange.getIn().setHeader(AxelHeaders.SENDER_CERTIFICATE, inExchange.getIn().getHeader(AxelHeaders.SENDER_CERTIFICATE));
                exchange.getIn().setHeader(AxelHeaders.CALLER_IP, inExchange.getIn().getHeader(AxelHeaders.CALLER_IP));
				exchange.getIn().setBody(body);
			}	
		});

		Object body = getBody(returnedExchange);
		log.debug("Returned body {}", body);
		inExchange.getIn().setBody(body);

        Map<String, Object> headers = returnedExchange.getOut().getHeaders();
        for (String key: headers.keySet()) {
            if (key.toLowerCase().startsWith("x-shs")) {
                inExchange.getIn().setHeader(key, headers.get(key));
            }
        }

        if (returnedExchange.getProperty(ShsHeaders.LABEL) != null) {
            inExchange.setProperty(ShsHeaders.LABEL, returnedExchange.getProperty(ShsHeaders.LABEL));
        }

		if (isException(returnedExchange)) {
			handleException(inExchange, returnedExchange);
		}
	}

	private void handleException(final Exchange inExchange,
			Exchange returnedExchange) {
		endpoint.getExceptionHandler().handleException(inExchange, returnedExchange);
	}

	private boolean isException(Exchange returnedExchange) {
		return endpoint.getExceptionHandler().isException(returnedExchange);
	}

	private String getDestinationUri(Exchange exchange) {
		String destinationUri = exchange.getIn().getHeader(ShsHeaders.DESTINATION_URI, String.class);
		
		if (StringUtils.isBlank(destinationUri)) {
			destinationUri = endpoint.getDestinationUri(); 
		}
		
		return destinationUri;
	}

	private Object getBody(Exchange returnedExchange) {
		if (returnedExchange.hasOut()) {
			return returnedExchange.getOut().getBody();
		} else {
			return returnedExchange.getIn().getBody();
		}
	}
}
