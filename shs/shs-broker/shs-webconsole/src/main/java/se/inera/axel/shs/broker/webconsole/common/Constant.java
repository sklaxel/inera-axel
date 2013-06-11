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
package se.inera.axel.shs.broker.webconsole.common;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class Constant {
	public final static String URN_X_SHS = "urn:X-shs:";

	public static final List<String> DIRECTION_LIST = new ArrayList<String>(
			Arrays.asList("any", "to-customer", "from-customer"));

	public static final List<String> TRANSFER_TYPE_LIST = new ArrayList<String>(
			Arrays.asList("any", "asynch", "synch"));

	public static final List<String> YESNO_LIST = new ArrayList<String>(
			Arrays.asList("yes", "no"));

	public static final String YES = "yes";
	public static final String NO = "no";

}
