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
package se.inera.axel.shs.broker.routing;

import se.inera.axel.shs.xml.label.ShsLabel;

import java.util.Set;

/**
 * Plugin registration that is interested in specific product ids.
 */
public class ProductShsPluginRegistration implements ShsPluginRegistration {

	String name;
	Set<String> productIds;
	String endpointUri;

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<String> getProductIds() {
		return productIds;
	}

	public void setProductIds(Set<String> productIds) {
		this.productIds = productIds;
	}

	public void setEndpointUri(String endpointUri) {
		this.endpointUri = endpointUri;
	}

	@Override
	public String getEndpointUri(ShsLabel label) {
		if (productIds.contains(label.getProduct().getValue())) {
			return endpointUri;
		}

		return null;
	}
}
