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
package se.inera.axel.shs.broker.webconsole.agreement;

import org.apache.wicket.markup.html.form.IChoiceRenderer;

import se.inera.axel.shs.xml.agreement.Product;

public class ProductChoiceRenderer implements IChoiceRenderer<Product> {

	private static final long serialVersionUID = 1L;

	@Override
	public Object getDisplayValue(Product product) {
		String displayValue = null;
		if (product != null) {
			displayValue = product.getCommonName() + "(" + product.getvalue() + ")";
		}
		return displayValue;
	}

	@Override
	public String getIdValue(Product product, int index) {
		String idValue = null;
		if (product != null) {
			idValue = product.getvalue();
		}
		return idValue;
	}

}
