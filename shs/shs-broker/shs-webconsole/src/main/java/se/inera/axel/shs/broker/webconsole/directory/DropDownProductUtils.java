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
package se.inera.axel.shs.broker.webconsole.directory;

import se.inera.axel.shs.broker.directory.ProductType;
import se.inera.axel.shs.xml.product.ShsProduct;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class DropDownProductUtils {
    /**
     * Utility class should not be instantiated
     */
    private  DropDownProductUtils() {

    }

    public static DropdownProduct createDropdownProduct(ProductType productType) {
        return new DropdownProduct(productType.getSerialNumber(), productType.getProductName(), productType.getLabeledUri());
    }

    public static DropdownProduct createDropdownProduct(ShsProduct shsProduct) {
        return new DropdownProduct(shsProduct.getUuid(), shsProduct
                .getCommonName(), shsProduct.getLabeledURI());
    }
}
