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

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class TimeAdapter extends XmlAdapter<String, Date> {

	static public final String TIME_FORMAT = "HH:mm";

    @Override
    public Date unmarshal(String xml) throws Exception {
    	if (xml == null) {
    		return null;
    	}

    	SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT);
        return new Date(formatter.parse(xml).getTime());
    }

    @Override
    public String marshal(Date property) throws Exception {
    	if (property == null) {
    		return null;
    	}

    	SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT);
        return formatter.format(property);
    }

}
