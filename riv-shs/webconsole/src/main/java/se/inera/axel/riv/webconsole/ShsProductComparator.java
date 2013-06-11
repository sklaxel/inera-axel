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
package se.inera.axel.riv.webconsole;

import java.util.Comparator;

import se.inera.axel.shs.xml.product.ShsProduct;

public class ShsProductComparator implements Comparator<ShsProduct> {
	private static final ShsProductComparator INSTANCE = new ShsProductComparator();
	
	private ShsProductComparator() {
		
	}
	
	public static ShsProductComparator getComparator() {
		return INSTANCE;
	}

	@Override
	public int compare(ShsProduct o1, ShsProduct o2) {
		String commonName1 = o1 == null ? null : o1.getCommonName(); 
		String commonName2 = o2 == null ? null : o2.getCommonName();
		
		if (commonName1 == null && commonName2 == null)
			return 0;
					
		if (commonName1 == null)
			return -1;
		
		if (commonName2 == null)
			return 1;
		
		return commonName1.compareToIgnoreCase(commonName2);
	}
}