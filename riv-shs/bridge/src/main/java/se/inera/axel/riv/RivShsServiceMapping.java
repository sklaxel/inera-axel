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
package se.inera.axel.riv;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class RivShsServiceMapping implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	String id;
	
	@Indexed(unique = true)
	String rivServiceNamespace;
	
	@Indexed(unique = true)
	String shsProductId;

	String rivServiceEndpoint;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRivServiceNamespace() {
		return rivServiceNamespace;
	}
	public void setRivServiceNamespace(String rivServiceNamespace) {
		this.rivServiceNamespace = rivServiceNamespace;
	}
	public String getRivServiceEndpoint() {
		return rivServiceEndpoint;
	}
	public void setRivServiceEndpoint(String rivServiceEndpoint) {
		this.rivServiceEndpoint = rivServiceEndpoint;
	}
	public String getShsProductId() {
		return shsProductId;
	}
	public void setShsProductId(String shsProductId) {
		this.shsProductId = shsProductId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((rivServiceNamespace == null) ? 0 : rivServiceNamespace
						.hashCode());
		result = prime * result
				+ ((shsProductId == null) ? 0 : shsProductId.hashCode());
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
		RivShsServiceMapping other = (RivShsServiceMapping) obj;
		if (rivServiceNamespace == null) {
			if (other.rivServiceNamespace != null)
				return false;
		} else if (!rivServiceNamespace.equals(other.rivServiceNamespace))
			return false;
		if (shsProductId == null) {
			if (other.shsProductId != null)
				return false;
		} else if (!shsProductId.equals(other.shsProductId))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "RivShsServiceMapping [rivServiceNamespace=" + rivServiceNamespace
				+ ", rivServiceEndpoint=" + rivServiceEndpoint
				+ ", shsProductId=" + shsProductId + "]";
	}
	
	
}
