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
package se.inera.axel.shs.xml;

import java.util.Arrays;
import java.util.List;

public class UrnProduct extends ShsUrn {

	private static final long serialVersionUID = 1L;

	private static final List<String> specialProducts = Arrays.asList(
		"confirm", "error", "agreement"
	);
	
	protected UrnProduct(String urn) {
		super(urn);
	}

	public String getSerialNumber() {
		if (isSpecialProduct()) {
			throw new RuntimeException("no serial number in special products");
		}
		return getValue();
	}
	
	public String getSpecialProductName() {
		if (!isSpecialProduct()) {
			throw new RuntimeException("instance is not a special product");
		}
		return getValue();
	}
	
	public boolean isSpecialProduct() {
		return specialProducts.contains(getValue());
	}
	
	public String getProductId() {
		return getValue();
	}

    public static UrnProduct valueOf(String product) {
        return new UrnProduct(product);
    }
}
