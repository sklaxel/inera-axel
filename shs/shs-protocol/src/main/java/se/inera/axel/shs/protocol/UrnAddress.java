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
package se.inera.axel.shs.protocol;

import se.inera.axel.shs.xml.ShsUrn;

public class UrnAddress extends ShsUrn {

	private static final long serialVersionUID = 1L;

	String internalId = null;
	String orgNumber = null;
	
	protected UrnAddress(String address) {
		super(address);
		
		int i = getValue().indexOf(".");
		if (i > 0) {
			orgNumber = getValue().substring(0, i);
			internalId = address.substring(i + 1);
		} else {
			orgNumber = getValue();
		}
	}
	
	public String getInternalId() {		
		return internalId;
	}
	
	public String getOrgNumber() {
		return orgNumber;
	}


	public static UrnAddress valueOf(String address) {
		return new UrnAddress(address);
	}
	
}
