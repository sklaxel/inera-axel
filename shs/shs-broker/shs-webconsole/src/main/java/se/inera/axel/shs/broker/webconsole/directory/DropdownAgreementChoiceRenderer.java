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
package se.inera.axel.shs.broker.webconsole.directory;

import java.util.List;

import org.apache.wicket.markup.html.form.IChoiceRenderer;

public class DropdownAgreementChoiceRenderer<T> implements IChoiceRenderer<T> {

	private static final long serialVersionUID = 1L;

	@Override
	public Object getDisplayValue(T object) {
		String displayValue = null;
		if (object != null) {
			DropdownAgreement agreement = (DropdownAgreement) object;
			displayValue = agreement.getProductName() + ":"
					+ agreement.getProductId() + " (Agreement: "
					+ agreement.getSerialNumber() + ")";
		}
		return displayValue;
	}

	@Override
	public String getIdValue(T object, int index) {
		String idValue = null;
		if (object != null) {
			DropdownAgreement agreement = null;
			if (object instanceof List) {
				agreement = ((List<DropdownAgreement>) object).get(index);
				idValue = agreement.getSerialNumber();
			} else if (object instanceof DropdownAgreement) {
				agreement = (DropdownAgreement) object;
				idValue = agreement.getSerialNumber();
			} else {
				idValue = (String) object;
			}
		}
		return idValue;
	}

}
