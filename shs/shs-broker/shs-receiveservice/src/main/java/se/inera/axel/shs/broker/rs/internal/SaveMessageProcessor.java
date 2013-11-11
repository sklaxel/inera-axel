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

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.mime.ShsMessage;

import java.io.InputStream;

/**
 * Saves the message body to the message log. Supports both an ShsMessage
 * and an InputStream body.
 *
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class SaveMessageProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(SaveMessageProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        LOG.trace("Saving message");
        MessageLogService messageLogService = exchange.getContext().getRegistry().lookup("messageLogService", MessageLogService.class);

        Object body = exchange.getIn().getBody();

        if (body instanceof ShsMessage) {
            exchange.getIn().setBody(messageLogService.saveMessage((ShsMessage) body));
        } else {
            InputStream streamBody = exchange.getIn().getMandatoryBody(InputStream.class);
            exchange.getIn().setBody(messageLogService.saveMessageStream(streamBody));
        }
    }
}
