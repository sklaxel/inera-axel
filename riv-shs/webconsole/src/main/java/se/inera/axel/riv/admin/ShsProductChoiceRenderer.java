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
package se.inera.axel.riv.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.axel.shs.xml.product.ShsProduct;

public class ShsProductChoiceRenderer implements IChoiceRenderer<String> {
	private static final Logger log = LoggerFactory.getLogger(ShsProductChoiceRenderer.class);

	private static final long serialVersionUID = 1L;
	
	private Map<String, ShsProduct> products = new HashMap<String, ShsProduct>();
	
	ShsProductChoiceRenderer(List<ShsProduct> products) {
		for (ShsProduct shsProduct : products) {
			this.products.put(shsProduct.getUuid(), shsProduct);
		}
	}

	@Override
	public Object getDisplayValue(String productId) {
		String displayValue = null;
		
		if (productId != null) {
			ShsProduct product = products.get(productId);
			
			displayValue = StringUtils.stripToEmpty(product.getCommonName()) + " (" + product.getUuid() + ")";
		} else {
			displayValue = "";
		}
		log.trace("displayValue={}", displayValue);
		return displayValue;
	}

	@Override
	public String getIdValue(String productId, int index) {
		log.trace("getIdValue({}, {})", productId, index);
		
		return productId;
	}

}
