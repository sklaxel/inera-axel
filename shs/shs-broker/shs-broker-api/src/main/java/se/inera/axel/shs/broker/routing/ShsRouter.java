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
package se.inera.axel.shs.broker.routing;

import se.inera.axel.shs.exception.MissingDeliveryAddressException;
import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.util.List;

/**
 * Applies routing logic that is used by the shs message broker.
 *
 */
public interface ShsRouter {

	/**
	 * Resolves SHS To addresses (Organization Id's) based on information in the SHS label. <p>
	 * May apply:
	 * <ul>
	 * <li>Direct addressing
	 * <li>Product addressing
	 * <li>Public agreement addressing
	 * <li>etc.
	 * </ul>
	 * 
	 * @param label
	 * @return A list of organization id's of the message recipients, or the empty list.
     * @throws ShsException if no recipient could be resolved due to an error.
	 */
	public List<String> resolveRecipients(ShsLabel label) throws ShsException;


    /**
     * Resolves a destination endpoint given an shs label.
     *
     * @param label
     * @return An endpoint uri.
     * @throws MissingDeliveryAddressException if no endpoint could be resolved.
     */
    public String resolveEndpoint(ShsLabel label) throws MissingDeliveryAddressException;


	/**
	 * Decided whether this message is local to this server or not.
	 * The broker might want to treat them differently.
	 *
	 * @param label
	 * @return
	 */
	public Boolean isLocal(ShsLabel label);


	/**
	 * Every router instance is owned by an SHS actor who's organization id is specified here.
	 *   
	 * @return The organization id of the owning SHS actor.
	 */
	public String getOrgId();
}