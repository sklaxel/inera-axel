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
package se.inera.axel.riv.impl;

import se.inera.axel.riv.RivShsServiceMapping;
import se.inera.axel.riv.RivShsServiceMappingRepository;
import se.inera.axel.shs.broker.routing.ShsPluginRegistration;
import se.inera.axel.shs.xml.label.ShsLabel;

import javax.annotation.Resource;

/**
 * Searches {@link RivShsServiceMappingRepository#findByShsProductId(String)} for the product id
 * specified in the label and returns the endpoint uri specified with {@link #setEndpointUri}
 * if a riv/shs mapping is found for the given product id;
 *
 */
public class Shs2RivPluginRegistration implements ShsPluginRegistration {

	String endpointUri;

	@Resource
	RivShsServiceMappingRepository repository;

	public void setEndpointUri(String endpointUri) {
		this.endpointUri = endpointUri;
	}

	@Override
	public String getName() {
		return getClass().getName();
	}

	@Override
	public String getEndpointUri(ShsLabel label) {

		String productId = label.getProduct().getvalue();
		RivShsServiceMapping mapping = repository.findByShsProductId(productId);

		if (mapping != null)
			return endpointUri;

		return null;
	}
}
