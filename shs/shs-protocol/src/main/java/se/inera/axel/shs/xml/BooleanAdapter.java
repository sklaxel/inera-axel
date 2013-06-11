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
package se.inera.axel.shs.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class BooleanAdapter extends XmlAdapter<String, Boolean> {

	@Override
    public Boolean unmarshal(String xml) throws Exception {
    	if (xml != null) {
    		
    		if (xml.equalsIgnoreCase("on")
        			|| xml.equalsIgnoreCase("yes")
        			|| xml.equalsIgnoreCase("true"))
    		{
    			return Boolean.TRUE;
    		}
    		
    	}
    	
    	return Boolean.FALSE;
    }

    @Override
    public String marshal(Boolean property) throws Exception {
    	
    	if (property == null || property == Boolean.FALSE) {
    		return "no";
    	}
    	
    	return "yes";
    }

}
