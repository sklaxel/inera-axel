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
package se.inera.axel.shs.directory;

import java.io.Serializable;

public class ProductType implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    String prodDescr;
    String serialNumber;
    String description;
    String principal;
    String preferredDeliveryMethod;
    String owner;
    String providerUrl;
    String productName;
    String userPassword;
    String labeledUri;
    String keywords;
    
    
	public String getProdDescr() {
		return prodDescr;
	}
	public void setProdDescr(String prodDescr) {
		this.prodDescr = prodDescr;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPrincipal() {
		return principal;
	}
	public void setPrincipal(String principal) {
		this.principal = principal;
	}
	public String getPreferredDeliveryMethod() {
		return preferredDeliveryMethod;
	}
	public void setPreferredDeliveryMethod(String preferredDeliveryMethod) {
		this.preferredDeliveryMethod = preferredDeliveryMethod;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getProviderUrl() {
		return providerUrl;
	}
	public void setProviderUrl(String providerUrl) {
		this.providerUrl = providerUrl;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getUserPassword() {
		return userPassword;
	}
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}
	public String getLabeledUri() {
		return labeledUri;
	}
	public void setLabeledUri(String labeledUri) {
		this.labeledUri = labeledUri;
	}
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	
	
	@Override
	public String toString() {
		return "ProductType [prodDescr=" + prodDescr + ", serialNumber="
				+ serialNumber + ", description=" + description
				+ ", principal=" + principal + ", preferredDeliveryMethod="
				+ preferredDeliveryMethod + ", owner=" + owner
				+ ", providerUrl=" + providerUrl + ", productName="
				+ productName + ", userPassword=" + userPassword
				+ ", labeledUri=" + labeledUri + ", keywords=" + keywords + "]";
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((serialNumber == null) ? 0 : serialNumber.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductType other = (ProductType) obj;
		if (serialNumber == null) {
			if (other.serialNumber != null)
				return false;
		} else if (!serialNumber.equals(other.serialNumber))
			return false;
		return true;
	}


}
