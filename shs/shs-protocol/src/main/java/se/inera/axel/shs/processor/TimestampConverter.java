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
package se.inera.axel.shs.processor;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampConverter {

	static public final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static Date stringToDate(String s) throws Exception {
    	if (s == null) {
    		return null;
    	}
    	
        SimpleDateFormat formatter = new SimpleDateFormat(DATETIME_FORMAT);
        return formatter.parse(s);
    }

    public static String dateToString(Date d) throws Exception {
    	if (d == null) {
    		return null;
    	}

    	SimpleDateFormat formatter = new SimpleDateFormat(DATETIME_FORMAT);
        return formatter.format(d);
    }

}
