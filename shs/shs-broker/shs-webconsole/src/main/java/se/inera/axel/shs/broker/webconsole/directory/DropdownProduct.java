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

import java.io.Serializable;

public class DropdownProduct implements Serializable {
	
	private String serialNumber;
	private String productName;
	private String labeledUri;

	public DropdownProduct() {
		// Do nothing
	}

	public DropdownProduct(String serialNumber, String productName, String labeledUri) {
		this.serialNumber = serialNumber;
		this.productName = productName;
		this.labeledUri = labeledUri;
	}

    @Override
	public String toString() {
		// Quick fix - not so very pretty
		return this.serialNumber;
	}

	public String getLabeledUri() {
		return labeledUri;
	}

	public void setLabeledUri(String labeledUri) {
		this.labeledUri = labeledUri;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
}
