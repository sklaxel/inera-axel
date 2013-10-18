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

import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.core.simple.ParameterizedContextMapper;
import org.springframework.ldap.core.simple.SimpleLdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import se.inera.axel.shs.broker.directory.*;
import se.inera.axel.shs.xml.ShsUrn;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Implements the {@link DirectoryService} with LDAP access through Spring Ldap.
 */
public class LdapDirectoryService implements DirectoryService {

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LdapDirectoryService.class);

    protected SimpleLdapTemplate ldapTemplate;
    
    public SimpleLdapTemplate getLdapTemplate() {
		return ldapTemplate;
	}
    
	public void setLdapTemplate(SimpleLdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}
	
	@Override
    public Organization getOrganization(String orgNumber) {
    	AndFilter filter = new AndFilter();
    	filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_OBJECT_CLASS, ShsLdapAttributes.CLASS_SHS_ORGEXTENSION));
    	filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_ORGANIZATION_NUMBER, orgNumber));
    	
    	return findOne(filter, new OrganizationMapper());
    }
    

	@Override
    public ProductType getProductType(String orgNumber, String productId) throws DirectoryException {
    	AndFilter filter = new AndFilter();
    	filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_OBJECT_CLASS, ShsLdapAttributes.CLASS_PRODUCT_TYPE));
    	filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_SERIAL_NUMBER, productId));
		filter.and(new LikeFilter(ShsLdapAttributes.ATTR_PRINCIPAL, "*" + orgNumber));
    	
    	return findOne(filter, new ProductTypeMapper());
    }
    

	@Override
    public Agreement getAgreement(String orgNumber, String productId, String transferType)
        throws DirectoryException
    {
		ShsUrn principal = ShsUrn.valueOf(orgNumber);
		
    	AndFilter filter = new AndFilter();
    	filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_OBJECT_CLASS, ShsLdapAttributes.CLASS_AGREEMENT));
    	filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_PRINCIPAL, principal.toUrnForm()));
    	filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_PRODUCT_ID, productId));
    	filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_TRANSFERTYPE, transferType.toLowerCase()));
    	return findOne(filter, new AgreementMapper());
    }
	
	@Override
    public List<Agreement> findAgreements(String orgNumber, String productId)
        throws DirectoryException
    {
		ShsUrn principal = orgNumber == null ? null : ShsUrn.valueOf(orgNumber);
		
    	AndFilter filter = new AndFilter();
    	filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_OBJECT_CLASS, ShsLdapAttributes.CLASS_AGREEMENT));
        if (principal != null) {
    	    filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_PRINCIPAL, principal.toUrnForm()));
        }
    	filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_PRODUCT_ID, productId));
    	return findAll(null, filter, new AgreementMapper());
    }
    

	@Override
    public Address getAddress(String orgNumber, String productSerialNumber) throws DirectoryException {

    	AndFilter filter = new AndFilter();
    	filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_OBJECT_CLASS, ShsLdapAttributes.CLASS_ADDRESS));
    	filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_ORGANIZATION_NUMBER, orgNumber));
    	filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_SERIAL_NUMBER, productSerialNumber));
    	
    	return findOne(filter, new AddressMapper());
    }

    /**
     * Finds one entry in the directory given the filter.
     *
     * @param filter
     * @param mapper
     * @param <T> Type of object to search with filter.
     * @return The entry as mapped by 'mapper', or null if no matching entry is found
     * @throws DirectoryException If more than one matching entry is found.
     */
    protected <T> T findOne(AndFilter filter, ParameterizedContextMapper<T> mapper) throws DirectoryException {
		List<T> entry = findAll(null, filter, mapper, 0, new DummyDirContextProcessor());
    	
    	if (entry.size() == 0) 
    		return null;
    	
    	if (entry.size() > 1) 
    		throw new DirectoryException("More than one entry found: " + filter.encode());
    	
    	return entry.get(0);
	}


    /**
     * Finds all entries matching filter, mapped with the mapper.
     * If organization is given, it is used as a search base.
     * For instance: list all addresses under a given organization.
     *
     * @param organization
     * @param filter
     * @param mapper
     * @param <T>
     * @return
     * @throws DirectoryException
     */
    protected <T> List<T> findAll(Organization organization, AndFilter filter, ParameterizedContextMapper<T> mapper)
            throws DirectoryException
    {
		return findAll(organization, filter, mapper, 0, new DummyDirContextProcessor());
	}
	

    /**
     * Finds all entries matching filter, mapped with the mapper.
     * If organization is given, it is used as a search base.
     * For instance: list all addresses under a given organization.
     *
     * At most 'limit' entries are returned.
     *
     * @param organization
     * @param filter
     * @param mapper
     * @param limit
     * @param dirContextProcessor
     * @param <T>
     * @return
     * @throws DirectoryException
     */
    private <T> List<T> findAll(Organization organization, AndFilter filter,
                                ParameterizedContextMapper<T> mapper, long limit,
                                DirContextProcessor dirContextProcessor)
            throws DirectoryException
    {
		List<T> entries = new ArrayList<T>();
		String base = "";
    	try {
    		SearchControls ctrl = new SearchControls();
    		ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
    		ctrl.setReturningObjFlag(true);
    		ctrl.setCountLimit(limit);
    		
    		if (organization != null) {
    			base = "o=" + organization.getOrgName();
    		}
    		
    		entries = ldapTemplate.search(base, filter.encode(), ctrl, mapper, dirContextProcessor);
    		
    		// Remove duplicates...
    		HashSet<T> set = new HashSet<T>(entries);
    		entries = new ArrayList<T>(set);
    		
       	} catch (NameNotFoundException e) {
    		log.warn("not found in ldap directory: " + base + "," + filter.encode());
    	} catch (RuntimeException e) {
    		log.error("error during looking-up", e);
    		throw new DirectoryException("error during looking-up", e);
    	}
    	
		return entries;
	}
 
    private class DummyDirContextProcessor implements DirContextProcessor {

		@Override
		public void preProcess(DirContext ctx) throws NamingException {}

		@Override
		public void postProcess(DirContext ctx) throws NamingException {}

	}
}
