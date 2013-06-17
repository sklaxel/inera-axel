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

import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.Status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public interface MessageLogService {
	ShsMessageEntry createEntry(ShsMessage message);

    ShsMessageEntry messageReceived(ShsMessageEntry entry);

    ShsMessageEntry messageQuarantined(ShsMessageEntry entry, Exception exception);

    ShsMessageEntry messageSent(ShsMessageEntry entry);

	ShsMessageEntry update(ShsMessageEntry entry);

	ShsMessageEntry findEntry(String id);
	// TODO should we return all entries with txid?
	ShsMessageEntry findEntryByTxid(String txid);

    ShsMessage fetchMessage(ShsMessageEntry entry);

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
        String sortAttribute;
        Boolean sortAsc = true;
        Boolean arrivalSortAsc = true;

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

        public Boolean getSortAsc() {
            return sortAsc;
        }

        public void setSortAsc(Boolean sortAsc) {
            this.sortAsc = sortAsc;
        }

        public Boolean getArrivalSortAsc() {
            return arrivalSortAsc;
        }

        public void setArrivalSortAsc(Boolean arrivalSortAsc) {
            this.arrivalSortAsc = arrivalSortAsc;
        }
    }
}
