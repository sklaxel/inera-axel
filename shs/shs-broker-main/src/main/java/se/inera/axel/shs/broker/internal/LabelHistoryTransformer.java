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
package se.inera.axel.shs.broker.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.messagestore.ShsMessageEntry;
import se.inera.axel.shs.protocol.ShsMessage;
import se.inera.axel.shs.xml.label.History;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class LabelHistoryTransformer {
    private static final Logger log = LoggerFactory.getLogger(LabelHistoryTransformer.class);

    String nodeId = "local";

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public ShsMessage process(ShsMessage shsMessage) throws Exception {
        ShsLabel label = shsMessage.getLabel();
        addHistoryEntry(label);

        return shsMessage;
    }

    public ShsMessageEntry process(ShsMessageEntry entry) {
        addHistoryEntry(entry.getLabel());

        return entry;
    }

    private void addHistoryEntry(ShsLabel label) {
        History history = new History();
        history.setNodeId(getNodeId());
        history.setTxId(label.getTxId());

        if (label.getTo() != null)
            history.setTo(label.getTo().getvalue());

        history.setLocalId(label.getTxId());
        history.setShsAgreement(label.getShsAgreement());
        history.setContentId(label.getContent().getContentId());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); // "2012-08-28T16:13:40"
        history.setDatetime(formatter.format(new Date()));
        history.setComment("Received");

        log.debug("Adding history to Label {}", history);
        label.getHistory().add(history);
    }

}