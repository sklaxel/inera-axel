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

import org.apache.camel.Header;
import org.apache.commons.lang.StringUtils;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.xml.TimestampAdapter;
import se.inera.axel.shs.xml.UrnProduct;
import se.inera.axel.shs.xml.label.Status;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * Converts camel header values (http headers and/or request parameters)
 * to a {@link MessageLogService.Filter} filter object suitable for listing messages.
 *
 */
public class HeaderToFilterConverter {

    public MessageLogService.Filter toFilter(
            @Header("producttype") String producttype,
            @Header("filter") String noAckfilter,
            @Header("maxhits") Integer maxHits,
            @Header("status") String status,
            @Header("corrid") String corrId,
            @Header("contentid") String contentId,
            @Header("originator") String originator,
            @Header("since") String since,
            @Header("sortattribute") String sortattribute,
            @Header("sortorder") String sortorder,
            @Header("arrivalorder") String arrivalorder,
            @Header("endrecipient") String endrecipient)
    {

        MessageLogService.Filter filter = new MessageLogService.Filter();


        if ("noack".equals(noAckfilter))
            filter.setNoAck(true);

        if (producttype != null) {
            List<String> productIds = Arrays.asList(StringUtils.split(producttype, ','));
            ListIterator<String> productIdsIterator = productIds.listIterator();

            while (productIdsIterator.hasNext()) {
                productIdsIterator.set(UrnProduct.valueOf(productIdsIterator.next()).getProductId());
            }

            filter.setProductIds(productIds);
        }

        filter.setOriginator(originator);

        filter.setEndRecipient(endrecipient);

        filter.setMaxHits(maxHits);

        filter.setCorrId(corrId);

        filter.setContentId(contentId);


        if (since != null) {
            SimpleDateFormat formatter = new SimpleDateFormat(TimestampAdapter.DATETIME_FORMAT);
            try {
                filter.setSince(formatter.parse(since));
            } catch (Exception e) {
                throw new IllegalArgumentException("timestamp format error on 'since': " + since);
            }
        }

        filter.setSortAttribute(sortattribute);

        filter.setArrivalOrder(arrivalorder);

        filter.setSortOrder(sortorder);
        if (status != null)
            filter.setStatus(Status.valueOf(status.toUpperCase()));

        return filter;
    }
}
