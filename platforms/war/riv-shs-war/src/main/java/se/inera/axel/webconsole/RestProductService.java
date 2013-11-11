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
package se.inera.axel.webconsole;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.broker.product.ProductService;
import se.inera.axel.shs.xml.product.ShsProduct;

import javax.ws.rs.PathParam;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RestProductService implements ProductService {
    Logger log = LoggerFactory.getLogger(RestProductService.class);
    URL address;

    public URL getAddress() {
        return address;
    }

    public void setAddress(URL address) {
        this.address = address;
    }

    private ProductService createProxy() {
        return JAXRSClientFactory.create(getAddress().toString(), ProductService.class);
    }

    @Override
    public ShsProduct getProduct(@PathParam("productId") String productTypeId) {
        try {
            return createProxy().getProduct(productTypeId);
        } catch (Exception e) {
            log.error("Cannot load product: " + productTypeId, e);
            // TODO do not return null, but throw exception. Seee JIRA issue.
            return null;
        }
    }

    @Override
    public List<ShsProduct> findAll() {
        try {
            return createProxy().findAll();
        } catch (Exception e) {
            log.error("Cannot load product list", e);
            // TODO do not return empty list, but throw exception. Seee JIRA issue.
            return new ArrayList<ShsProduct>();
        }

    }
}
