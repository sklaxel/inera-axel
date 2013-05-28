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

public class Organization implements Serializable {

    private static final long serialVersionUID = 1L;

    String orgName;
    String orgNumber;
    String description;
    String postalAddress;
    String postalCode;
    String streetAddress;
    String faxNumber;
    String postOfficeBox;
    String labeledUri;
    String providerUrl;
    String phoneNumber;
	String userPassword;
	
	
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public String getOrgNumber() {
		return orgNumber;
	}
	public void setOrgNumber(String orgNumber) {
		this.orgNumber = orgNumber;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPostalAddress() {
		return postalAddress;
	}
	public void setPostalAddress(String postalAddress) {
		this.postalAddress = postalAddress;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public String getStreetAddress() {
		return streetAddress;
	}
	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}
	public String getFaxNumber() {
		return faxNumber;
	}
	public void setFaxNumber(String faxNumber) {
		this.faxNumber = faxNumber;
	}
	public String getPostOfficeBox() {
		return postOfficeBox;
	}
	public void setPostOfficeBox(String postOfficeBox) {
		this.postOfficeBox = postOfficeBox;
	}
	public String getLabeledUri() {
		return labeledUri;
	}
	public void setLabeledUri(String labeledUri) {
		this.labeledUri = labeledUri;
	}
	public String getProviderUrl() {
		return providerUrl;
	}
	public void setProviderUrl(String providerUrl) {
		this.providerUrl = providerUrl;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getUserPassword() {
		return userPassword;
	}
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}
	@Override
	public String toString() {
		return "Organization [orgName=" + orgName + ", orgNumber=" + orgNumber
				+ ", description=" + description + ", postalAddress="
				+ postalAddress + ", postalCode=" + postalCode
				+ ", streetAddress=" + streetAddress + ", faxNumber="
				+ faxNumber + ", postOfficeBox=" + postOfficeBox
				+ ", labeledUri=" + labeledUri + ", providerUrl=" + providerUrl
				+ ", phoneNumber=" + phoneNumber + ", userPassword="
				+ userPassword + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((orgNumber == null) ? 0 : orgNumber.hashCode());
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
		Organization other = (Organization) obj;
		if (orgNumber == null) {
			if (other.orgNumber != null)
				return false;
		} else if (!orgNumber.equals(other.orgNumber))
			return false;
		return true;
	}
 
	
}
