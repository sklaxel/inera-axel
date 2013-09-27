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

import org.apache.wicket.markup.html.form.IChoiceRenderer;

import java.util.Map;

public class DropdownProductChoiceRenderer implements IChoiceRenderer<String> {
    private Map<String, DropdownProduct> productMap;

	private static final long serialVersionUID = 1L;

    public DropdownProductChoiceRenderer(Map<String, DropdownProduct> productMap) {
        this.productMap = productMap;
    }

	@Override
	public Object getDisplayValue(String productId) {
		String displayValue = null;
        DropdownProduct product = productMap.get(productId);

		if (product != null) {
            if (product.getProductName() != null) {
			    displayValue = product.getProductName() + "  (" + product.getSerialNumber() + ")";
            } else {
                displayValue = product.getSerialNumber();
            }
		}

		return displayValue;
	}

	@Override
	public String getIdValue(String productId, int index) {
		return productId;
	}

}
