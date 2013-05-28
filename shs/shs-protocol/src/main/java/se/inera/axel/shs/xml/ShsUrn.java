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
package se.inera.axel.shs.xml;

import java.io.Serializable;


public class ShsUrn implements Serializable {	
	private static final long serialVersionUID = 1L;

	public static final String SHS_URN = "urn:X-shs:";
	String value = null;


	protected ShsUrn(String value) {
		// TODO should null be allowed?
		if (value != null && value.toUpperCase().startsWith(SHS_URN.toUpperCase())) {
			this.value = value.substring(SHS_URN.length());
		} else {
			this.value = value;
		}

	}

	public String getValue() {
		return value;
	}
	
	
	public static ShsUrn valueOf(String value) {
		return new ShsUrn(value);
	}

	public String toUrnForm() {
		if (getValue() == null || getValue().equals("")) {
			return null;
		}
		return SHS_URN + getValue();
	}

	@Override
	public String toString() {
		return toUrnForm();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		ShsUrn other = (ShsUrn) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	
   
}
