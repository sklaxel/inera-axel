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
import org.apache.camel.Processor;
import org.apache.commons.lang.StringUtils;
import se.inera.axel.shs.xml.UrnAddress;
import se.inera.axel.shs.xml.UrnProduct;

import java.util.UUID;

/**
 * Extracts "outbox" (i.e. shs org nbr of recipient) and "txId" from the HTTP PATH header.
 * Expects the path to start with {@link #PATH_PREFIX} that currently equals '{@value #PATH_PREFIX}'
 *
 */
public class HttpPathParamsExtractor implements Processor {

    public static final String PATH_PREFIX = "/shs/ds/";

    @Override
    public void process(Exchange exchange) throws Exception {

        String httpPath = exchange.getIn().getHeader(Exchange.HTTP_PATH, String.class);
        String restPath = StringUtils.removeStart(httpPath, PATH_PREFIX);

        UrnAddress outbox = UrnAddress.valueOf(StringUtils.substringBefore(restPath, "/"));
        exchange.getIn().setHeader("outbox", outbox.toSimpleForm());

        String txIdOrProductId = StringUtils.substringAfter(restPath, "/");

        if (StringUtils.isEmpty(txIdOrProductId)) {
            return;
        }

        try {
            // If its an UUID, assume it is the txId
            String txId = UUID.fromString(txIdOrProductId).toString();
            exchange.getIn().setHeader("txId", txId);
        } catch (Exception e) {
            try {
                // else, assume it's the product id.
                UrnProduct urnProduct = UrnProduct.valueOf(txIdOrProductId);
                exchange.getIn().setHeader("producttype", urnProduct.getProductId());
            } catch (Exception ee) {
                ;
            }
        }

    }
}
