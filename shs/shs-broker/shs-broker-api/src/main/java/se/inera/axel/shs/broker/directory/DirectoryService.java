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
package se.inera.axel.shs.broker.directory;

import java.util.List;

/**
 * Provides access to the SHS directory service as needed by the shs broker and router.
 * 
 */
public interface DirectoryService {

    /**
     * Find the actor with the given org number.
     *
     * @param orgNumber
     * @return The actor Organization, or null if not found.
     * @throws DirectoryException if more than one match, or other error occurs.
     */
	public Organization getOrganization(String orgNumber);

    /**
     * Find the delivery address with the given org number and productId.
     *
     * @param orgNumber
     * @param productId
     * @return The Address, or null if not found.
     * @throws DirectoryException if more than one match, or other error occurs.
     */
	Address getAddress(String orgNumber, String productId);


	/**
	 * 
	 * @param orgNumber
	 * @param productId
	 * @param transferType
	 * @return
	 * 
	 * @deprecated replaced by {@link #findAgreements(String, String)}
	 */
	@Deprecated
	Agreement getAgreement(String orgNumber, String productId, String transferType);
	
	/**
	 * Finds all agreements with the given productId for the given actor.
	 * 
	 * @param orgNumber the actor of the agreement.
	 * @param productId the id of the product.
	 * @return A list of all agreements that match the given actor and productId. If no agreement is found an empty list is returned.
	 */
	List<Agreement> findAgreements(String orgNumber, String productId);


    /**
     * Find the product type with the given productId for the given actor.
     *
     * @param orgNumber
     * @param productId
     * @return The corresponding product type, or null if not found.
     * @throws DirectoryException if more than one match, or other error occurs.
     */
	ProductType getProductType(String orgNumber, String productId);

}
