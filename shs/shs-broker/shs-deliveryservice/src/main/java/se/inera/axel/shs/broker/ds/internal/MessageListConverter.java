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

import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.message.Data;
import se.inera.axel.shs.xml.message.Message;
import se.inera.axel.shs.xml.message.ShsMessageList;

/**
 * Converts output from {@link MessageLogService#listMessages(String, se.inera.axel.shs.broker.messagestore.MessageLogService.Filter)}
 * to {@link ShsMessageList}.
 *
 */
public class MessageListConverter {

    public ShsMessageList toShsMessageList(Iterable<ShsMessageEntry> entries) {
        ShsMessageList messageList = new ShsMessageList();

        if (entries == null) {
            return messageList;
        }

        for (ShsMessageEntry entry : entries) {
            messageList.getMessage().add(toMessage(entry));
        }

        return messageList;
    }

    private Message toMessage(ShsMessageEntry entry) {
        Message message =
                new Message();

        ShsLabel label = entry.getLabel();
        if (label.getProduct() != null)
            message.setProduct(label.getProduct().getValue());

        if (label.getContent() != null)
            message.setContentId(label.getContent().getContentId());
        message.setCorrId(label.getCorrId());

        if (label.getEndRecipient() != null)
            message.setEndRecipient(label.getEndRecipient().getValue());

        if (label.getFrom() != null)
            message.setFrom(label.getFrom().getValue());

        if (label.getOriginator() != null)
            message.setOriginator(label.getOriginator().getValue());

        message.setSequenceType(label.getSequenceType());
        // message.setSize();
        message.setStatus(label.getStatus());
        message.setSubject(label.getSubject());
        message.setTimestamp(label.getDatetime());

        if (label.getTo() != null)
            message.setTo(label.getTo().getValue());

        message.setTxId(label.getTxId());

        for (Object object : label.getContent().getDataOrCompound()) {
            if (object instanceof se.inera.axel.shs.xml.label.Data) {
                se.inera.axel.shs.xml.label.Data labelData = (se.inera.axel.shs.xml.label.Data)object;
                Data data = new Data();
                data.setDatapartType(labelData.getDatapartType());
                data.setFilename(labelData.getFilename());
                data.setNoOfBytes(labelData.getNoOfBytes());
                data.setNoOfRecords(labelData.getNoOfRecords());
                message.getData().add(data);
            }
        }



        return message;
    }

}
