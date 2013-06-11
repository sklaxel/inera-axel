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

public interface DirectoryAdminService extends DirectoryService {

	List<Organization> getOrganizations();
	void deleteActor(Organization organization);
	void saveActor(Organization organization);
	
	List<ProductType> getProductTypes(Organization organization);
	void saveProduct(Organization organization, ProductType product);
	void removeProduct(Organization organization, ProductType product);
	
	List<Agreement> getAgreements(Organization organization);
	void saveAgreement(Organization organization, Agreement agreement);
	void removeAgreement(Organization organization, Agreement agreement);

	List<Address> getAddresses(Organization organization);
	void saveAddress(Organization organization, Address address);
	void removeAddress(Organization organization, Address Address);
	
}
