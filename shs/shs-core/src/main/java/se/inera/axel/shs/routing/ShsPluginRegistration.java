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
package se.inera.axel.shs.routing;

import se.inera.axel.shs.xml.label.ShsLabel;

/**
 * Clients can register this interface with the shs router to tell the router that
 * they exist, what messages they want and on what endpoint they listen.
 *
 * <p>
 * This is interface so it could work easily in an OSGi whiteboard fashion.
 */
public interface ShsPluginRegistration {

	/**
	 * Name identification of this component.
	 *
	 * @return
	 */
	String getName();

	/**
	 * Endpoint to send the ShsMessage to given an ShsLabel.
	 * If the component is not interested in the given shs message when the router asks for an endpoint
	 * the component must return null.
	 *
	 * @return An endpoint uri or null.
	 */
	String getEndpointUri(ShsLabel label);
}

