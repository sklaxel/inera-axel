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
package se.inera.axel.shs.broker.directory.internal;

import org.springframework.cache.annotation.Cacheable;
import se.inera.axel.shs.broker.directory.*;

import java.util.List;

/**
 * Extends the {@link LdapDirectoryService} implementation with caching of method invocations.
 */
public class CachingLdapDirectoryService extends LdapDirectoryService {

 
	@Cacheable("ldap")
	@Override
    public Organization getOrganization(String orgNumber) {
		return super.getOrganization(orgNumber);
    }
    
	
	@Cacheable("ldap")
	@Override
    public ProductType getProductType(String orgNumber, String productId) throws DirectoryException {
    	return super.getProductType(orgNumber, productId);
    }
    

	@Cacheable("ldap")
	@Override
    public Agreement getAgreement(String orgNumber, String productId, String transferType)
        throws DirectoryException
    {
		return super.getAgreement(orgNumber, productId, transferType);
    }
	
	@Cacheable("ldap")
	@Override
    public List<Agreement> findAgreements(String orgNumber, String productId)
        throws DirectoryException
    {
		return super.findAgreements(orgNumber, productId);
    }
    

    @Cacheable("ldap")
	@Override
    public Address getAddress(String orgNumber, String productSerialNumber) throws DirectoryException {
    	return super.getAddress(orgNumber, productSerialNumber);
    }
    
}
