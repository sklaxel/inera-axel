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
package se.inera.axel.shs.broker.internal;

import org.apache.camel.Property;
import se.inera.axel.shs.exception.IllegalReceiverException;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.To;

import java.util.List;

/**
 * Looks for an exchange property named {@linkplain #PROPERTY_SHS_RECEIVER_LIST} that should
 * contain a list of strings populated with shs message recipients organization ids.
 * <p>
 * Expects the list to contain exactly one (1) receiver for any given message, or else an exception is raised.
 * (Since we are in the synchronous broker)
 * <p>
 * If the list is null (the property is not set on the exchange) the message is passed through unchanged.
 * <p>
 * If one recipient is found in the list that differs from the specified recipient, the label/to is changed.
 *
 */
public class RecipientLabelTransformer {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RecipientLabelTransformer.class);
	
	public static final String PROPERTY_SHS_RECEIVER_LIST = "PROPERTY_SHS_RECEIVER_LIST";

	/**
	 *
	 * @param shsLabel
	 * @param toOrganisationNumbers list of organization numbers on the format "0000000000"
	 * @return The (potentially) changed shs message.
	 * @throws IllegalReceiverException  if the list contains not exactly one recipient.
	 */
	public void transform(ShsLabel shsLabel, @Property(PROPERTY_SHS_RECEIVER_LIST) List<String> toOrganisationNumbers)
			throws IllegalReceiverException
	{
		log.debug("Starting routing...");

		if (toOrganisationNumbers == null) {
			log.warn("no list with organization numbers found, leaving message as is");
			return;
		}

		if (toOrganisationNumbers.size() != 1) {
			throw new IllegalReceiverException("Expected 1 receiver but found [" + toOrganisationNumbers.size() + "]");
		}

		String receiver = toOrganisationNumbers.get(0);
		To to = shsLabel.getTo();
		if (to == null) {
			to = new To();
			to.setvalue(receiver);
			shsLabel.setTo(to);
		} else {
			if (!to.getvalue().contains(receiver)) {
				to.setvalue(receiver);
				to.setCommonName(null);
			}
		}
	}
}
