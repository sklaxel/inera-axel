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
package se.inera.axel.shs.broker.messagestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.Status;
import se.inera.axel.shs.xml.label.TransferType;
import se.inera.axel.shs.xml.management.ShsManagement;

/**
 * The broker's interface to the message database log/queue.
 * <p/>
 *
 * This log is used to store the incoming message (with {@link #createEntry(se.inera.axel.shs.mime.ShsMessage)}
 * and get a reference to an {@link ShsMessageEntry}-object. (Claim-Check-pattern).<br/>
 * That object is then processed and dispatched inside the broker until the message is to
 * be sent to a remote destination or handed to a client. Then the original message is
 * fetched from the database with {@link #fetchMessage(ShsMessageEntry)}. <br/>
 *
 * An implementation of this interface would typically interact with {@link MessageStoreService} to handle the actual store/fetch of the real {@link ShsMessage}.
 */
public interface MessageLogService {
	ShsMessageEntry createEntry(ShsMessage message);
    ShsMessage fetchMessage(ShsMessageEntry entry);

    ShsMessageEntry messageReceived(ShsMessageEntry entry);

    ShsMessageEntry messageQuarantined(ShsMessageEntry entry, Exception exception);

    ShsMessage quarantineCorrelatedMessages(ShsMessage message);

    ShsMessage acknowledgeCorrelatedMessages(ShsMessage message);

    ShsMessageEntry messageSent(ShsMessageEntry entry);

    ShsMessageEntry messageFetched(ShsMessageEntry entry);

    ShsMessageEntry acknowledge(ShsMessageEntry entry);

	ShsMessageEntry update(ShsMessageEntry entry);

	ShsMessageEntry findEntryByShsToAndTxid(String shsTo, String txid);

    Iterable<ShsMessageEntry> listMessages(String shsTo, Filter filter);


    class Filter {
        Date since;
        Boolean noAck = false;
        Status status = Status.PRODUCTION;
        String originator;
        String endRecipient;
        String corrId;
        String contentId;
        Integer maxHits;
        List<String> productIds = new ArrayList<String>();
        String metaName;
        String metaValue;
        String sortAttribute;
        String sortOrder = "ascending";
        String arrivalOrder = "ascending";

        public Date getSince() {
            return since;
        }

        public void setSince(Date since) {
            this.since = since;
        }

        public Boolean getNoAck() {
            return noAck;
        }

        public void setNoAck(Boolean noAck) {
            this.noAck = noAck;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public String getOriginator() {
            return originator;
        }

        public void setOriginator(String originator) {
            this.originator = originator;
        }

        public String getEndRecipient() {
            return endRecipient;
        }

        public void setEndRecipient(String endRecipient) {
            this.endRecipient = endRecipient;
        }

        public String getCorrId() {
            return corrId;
        }

        public void setCorrId(String corrId) {
            this.corrId = corrId;
        }

        public String getContentId() {
            return contentId;
        }

        public void setContentId(String contentId) {
            this.contentId = contentId;
        }

        public Integer getMaxHits() {
            return maxHits;
        }

        public void setMaxHits(Integer maxHits) {
            this.maxHits = maxHits;
        }

        public List<String> getProductIds() {
            return productIds;
        }

        public void setProductIds(List<String> productIds) {
            this.productIds = productIds;
        }

        public String getSortAttribute() {
            return sortAttribute;
        }

        public void setSortAttribute(String sortAttribute) {
            this.sortAttribute = sortAttribute;
        }

        public String getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(String sortOrder) {
            this.sortOrder = sortOrder;
        }

        public String getArrivalOrder() {
            return arrivalOrder;
        }

        public void setArrivalOrder(String arrivalOrder) {
            this.arrivalOrder = arrivalOrder;
        }

        public String getMetaName() {
            return metaName;
        }

        public void setMetaName(String metaName) {
            this.metaName = metaName;
        }

        public String getMetaValue() {
            return metaValue;
        }

        public void setMetaValue(String metaValue) {
            this.metaValue = metaValue;
        }

        @Override
        public String toString() {
            return "Filter{" +
                    "since=" + since +
                    ", noAck=" + noAck +
                    ", status=" + status +
                    ", originator='" + originator + '\'' +
                    ", endRecipient='" + endRecipient + '\'' +
                    ", corrId='" + corrId + '\'' +
                    ", contentId='" + contentId + '\'' +
                    ", maxHits=" + maxHits +
                    ", productIds=" + productIds +
                    ", metaName='" + metaName + '\'' +
                    ", metaValue='" + metaValue + '\'' +
                    ", sortAttribute='" + sortAttribute + '\'' +
                    ", sortOrder='" + sortOrder + '\'' +
                    ", arrivalOrder='" + arrivalOrder + '\'' +
                    '}';
        }
    }
}
