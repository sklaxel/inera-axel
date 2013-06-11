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
package se.inera.axel.shs.broker.webconsole.common;

import java.io.Serializable;

import org.apache.wicket.model.IModel;

@SuppressWarnings("rawtypes")
public class YesNoBooleanConverterModel implements IModel {

	private static final long serialVersionUID = 1L;

	private final IModel wrappedModel;

	public YesNoBooleanConverterModel(IModel model) {
		wrappedModel = model;
	}

	@Override
	public Serializable getObject() {
		Boolean result = false;
		String value = (String) wrappedModel.getObject();
		if (value != null && value.toUpperCase().equals("YES")) {
			result = true;
		}
		return result;
	}

	@Override
	public void setObject(Object value) {
		Boolean valueAsBoolean = (Boolean) value;
		String result = valueAsBoolean != null && valueAsBoolean ? "yes" : "no";
		wrappedModel.setObject(result);
	}
	
	@Override
	public void detach() {
		wrappedModel.detach();
	}



}
