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
package se.inera.axel.shs.broker.directory;

import java.io.Serializable;


public class Address implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	
	public String serialNumber;
	public String organizationNumber;
	public String deliveryMethods;
	
	
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	public String getOrganizationNumber() {
		return organizationNumber;
	}
	public void setOrganizationNumber(String organizationNumber) {
		this.organizationNumber = organizationNumber;
	}
	public String getDeliveryMethods() {
		return deliveryMethods;
	}
	public void setDeliveryMethods(String deliveryMethods) {
		this.deliveryMethods = deliveryMethods;
	}
	
	
	@Override
	public String toString() {
		return "Address [serialNumber=" + serialNumber
				+ ", organizationNumber=" + organizationNumber
				+ ", deliveryMethods=" + deliveryMethods + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((organizationNumber == null) ? 0 : organizationNumber
						.hashCode());
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
		Address other = (Address) obj;
		if (organizationNumber == null) {
			if (other.organizationNumber != null)
				return false;
		} else if (!organizationNumber.equals(other.organizationNumber))
			return false;
		if (serialNumber == null) {
			if (other.serialNumber != null)
				return false;
		} else if (!serialNumber.equals(other.serialNumber))
			return false;
		return true;
	}

	
}
