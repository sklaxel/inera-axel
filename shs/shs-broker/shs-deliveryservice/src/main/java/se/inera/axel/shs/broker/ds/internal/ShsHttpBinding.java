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
package se.inera.axel.shs.broker.ds.internal;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.http.DefaultHttpBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsMessageMarshaller;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A custom camel http binding for returning shs messages to client who fetch messages.
 *
 * <p/>
 *
 * If the body is of type {@link ShsMessage}, an instance of {@link ShsMessageMarshaller} is
 * used to stream the message to the client on the response stream.
 *
 * If that succeeds, and an exchange property named 'entry' of type {@link ShsMessageEntry} is found),
 * the message entry's state is changed from {@linkplain se.inera.axel.shs.broker.messagestore.MessageState#FETCHING_IN_PROGRESS}
 * to {@linkplain se.inera.axel.shs.broker.messagestore.MessageState#FETCHED}.
 *
 * If an exception occurrs during streaming, the message entry's state is reset to {@linkplain se.inera.axel.shs.broker.messagestore.MessageState#RECEIVED}.
 *
 *
 */
public class ShsHttpBinding extends DefaultHttpBinding {
    Logger log = LoggerFactory.getLogger(ShsHttpBinding.class);


    MessageLogService messageLogService;

    public void setMessageLogService(MessageLogService messageLogService) {
        this.messageLogService = messageLogService;
    }

    @Override
    protected void doWriteDirectResponse(Message message, HttpServletResponse response, Exchange exchange) throws IOException {

        Object body = message.getBody();
        if (body instanceof ShsMessage) {
            ShsMessageEntry entry = exchange.getProperty("entry", ShsMessageEntry.class);
            if (entry == null) {
                log.warn("ShsMessageEntry not found in exchange property 'entry'");
            }

            ShsMessageMarshaller marshaller = new ShsMessageMarshaller();
            try {
                marshaller.marshal((ShsMessage)body, response.getOutputStream());

                if (entry != null)
                    messageLogService.messageFetched(entry);

            } catch (Exception e) {
                if (entry != null) {
                    messageLogService.messageReceived(entry);
                }

                throw new IOException("Error writing message to client", e);
            }
        } else {
            super.doWriteDirectResponse(message, response, exchange);
        }
    }
}
