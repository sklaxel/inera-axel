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
package se.inera.axel.shs.broker.rs.internal;

import org.apache.camel.ExchangeException;
import org.apache.camel.Header;
import org.apache.camel.component.http.HttpOperationFailedException;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.exception.MissingDeliveryExecutionException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsHeaders;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class RemoteMessageHandlingErrorHandler {

    public RemoteMessageHandlingErrorHandler() {
    }

    public void handleError(
            ShsMessage shsMessage,
            @Header(ShsHeaders.X_SHS_ERRORCODE) String errorCode,
            @ExchangeException HttpOperationFailedException exception) {
        MissingDeliveryExecutionException e = new MissingDeliveryExecutionException(
                String.format("Delivery of message failed. Remote message handling error: errorCode %s errorInfo %s",
                        errorCode,
                        exception.getResponseBody()),
                exception);

        throw e;
    }
}
