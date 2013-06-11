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
package se.inera.axel.shs.camel.component;

import org.apache.camel.Exchange;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.exception.MissingDeliveryExecutionException;
import se.inera.axel.shs.exception.OtherErrorException;
import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.processor.ResponseMessageBuilder;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.io.IOException;

public class DefaultShsExceptionHandler implements ShsExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(DefaultShsExceptionHandler.class);
	
	private boolean isReturnError = false;
	
	ResponseMessageBuilder responseMessageBuilder = new ResponseMessageBuilder();
	
	public boolean isReturnError() {
		return isReturnError;
	}

	public void setReturnError(boolean isReturnError) {
		this.isReturnError = isReturnError;
	}

	public void handleException(final Exchange inExchange, final Exchange returnedExchange) {

		ShsMessage shsMessage = null;
		ShsLabel label = null;

		shsMessage = inExchange.getIn().getBody(ShsMessage.class);
		if (shsMessage != null)
			label = shsMessage.getLabel();

		if (label == null)
			label = inExchange.getProperty(ShsHeaders.LABEL, ShsLabel.class);

		if (hasException(returnedExchange)) {
			createResponse(inExchange, createOrEnrichShsException(returnedExchange, label));
		}
    }

	private ShsException createOrEnrichShsException(Exchange returnedExchange, ShsLabel label) {

		ShsException shsException = returnedExchange.getException(ShsException.class);

		if (shsException == null) {
			IOException ioException = returnedExchange.getException(IOException.class);

			if (ioException != null) {
				shsException = new MissingDeliveryExecutionException(ioException);
			}
		}

		if (shsException == null) {
			Exception exception = returnedExchange.getException(Exception.class);
			shsException = new OtherErrorException(exception);
		}

		if (label != null) {
			if (StringUtils.isBlank(shsException.getContentId()) && label.getContent() != null) {
				shsException.setContentId(label.getContent().getContentId());
			}
			
			if (StringUtils.isBlank(shsException.getCorrId())) {
				shsException.setCorrId(label.getCorrId());
			}
		}
		
		return shsException;
	}

	private void createResponse(final Exchange inExchange,
			ShsException shsException) {
		if (isReturnError()) {
			inExchange.getIn().setBody(responseMessageBuilder.buildErrorMessage(inExchange.getIn().getBody(ShsMessage.class), shsException));
		} else {
			inExchange.setException(shsException);
		}
	}

	@Override
	public boolean isException(Exchange returnedExchange) {
		if (hasException(returnedExchange))
			return true;
		
		if (!isShsMessage(returnedExchange))
			return true;
		
		return false;
	}

	private boolean isShsMessage(Exchange returnedExchange) {
		return getBody(returnedExchange) instanceof ShsMessage;
	}

	private boolean hasException(Exchange returnedExchange) {
		return returnedExchange.getException() != null;
	}
	
	private Object getBody(Exchange exchange) {
		if (exchange.hasOut()) {
			return exchange.getOut().getBody();
		} else {
			return exchange.getIn().getBody();
		}
	}
}
