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
package se.inera.axel.shs.processor;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import se.inera.axel.shs.exception.IllegalMessageStructureException;
import se.inera.axel.shs.exception.IllegalProductTypeException;
import se.inera.axel.shs.exception.IllegalReceiverException;
import se.inera.axel.shs.exception.IllegalSenderException;
import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.protocol.ShsMessage;
import se.inera.axel.shs.protocol.UrnAddress;
import se.inera.axel.shs.protocol.UrnProduct;
import se.inera.axel.shs.xml.label.From;
import se.inera.axel.shs.xml.label.Originator;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.To;

public class SimpleLabelValidator {
	public SimpleLabelValidator() {
        super();
    }
	
	public void validate(ShsMessage message)  throws ShsException {
		
		validate(message.getLabel());
	}
	
	public void validate(ShsLabel label) throws ShsException {
		validateOriginatorOrFrom(label);
		validateTo(label);
		validateProduct(label);
		validateSequenceType(label);
	}
	
	private void validateOriginatorOrFrom(ShsLabel label) {
		List<Object> orignatorOrFrom = label.getOriginatorOrFrom();
		
		// If no From is given Originator is mandatory
		if (orignatorOrFrom == null || orignatorOrFrom.isEmpty()) {
			throw new IllegalSenderException("Originator is mandatory when from is not given");
		}
		
		From from = null;
		Originator originator = null;
		
		for (Object object : orignatorOrFrom) {
			if (object instanceof From) {
				if (from != null) {
					throw new IllegalSenderException("Multiple from is not allowed");
				}
				from = (From)object;
			} else if (object instanceof Originator) {
				if (originator != null) {
					throw new IllegalSenderException("Multiple originator is not allowed");
				}
				originator = (Originator)object;
			}
		}
		
		// If from is empty Originator is mandatory // TODO is this the correct check?
		if (from == null || StringUtils.isBlank(from.getvalue())) {
			if (originator == null || StringUtils.isBlank(originator.getvalue())) {
				throw new IllegalSenderException("Originator is mandatory when from is not given");
			}
		}
	}

	private void validateTo(ShsLabel label) {
		To to = label.getTo();
		if (to == null) {
			// To is optional
			return;
		}
		
		// Check that the to address is valid
		try {
			UrnAddress.valueOf(to.getvalue());
		} catch (IllegalArgumentException e) {
			throw new IllegalReceiverException(e);
		}
	}
	
	private void validateProduct(ShsLabel label) {
		if (label.getProduct() == null) {
			throw new IllegalProductTypeException("Product is mandatory");
		}
		
		UrnProduct product = null;
		try {
			product = new UrnProduct(label.getProduct().getvalue());
		} catch(IllegalArgumentException e) {
			throw new IllegalProductTypeException("The given product type id is not valid", e);
		}
		
		String productURN = product.getValue();
		
		if (productURN == null) {
			throw new IllegalProductTypeException("Product type id must not be blank");
		}
		
		if (!product.isSpecialProduct()) {
			try {
				UUID.fromString(productURN);
			} catch (IllegalArgumentException e) {
				throw new IllegalProductTypeException("Product type id [" + productURN + "] is not valid", e);
			}
		}
		
	}
	
	private void validateSequenceType(ShsLabel label) {
		if (label.getSequenceType() == null) {
			throw new IllegalMessageStructureException("Sequence type is mandatory");
		}
	}

}
