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
package se.inera.axel.shs.admin.common;

import java.util.List;

public class Util {

	public static String getUUIDFromProductValue(String productValue) {
		String uuid = "";
		if (productValue != null && productValue.contains(Constant.URN_X_SHS)) {
			uuid = productValue.substring(Constant.URN_X_SHS.length());
		}
		return uuid;
	}

	public static boolean stringsContainsQuery(String query, List<String> args) {
		boolean result = false;
		for (String field : args) {
			if (field != null
					&& field.toUpperCase().contains(query.toUpperCase())) {
				result = true;
				break;
			}
		}
		return result;
	}

}
