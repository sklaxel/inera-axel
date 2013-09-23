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
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import se.inera.axel.shs.broker.directory.*;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LdapDirectoryAdminService extends LdapDirectoryService implements DirectoryAdminService {

	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LdapDirectoryAdminService.class);


	@Override
	public List<Organization> getOrganizations() {
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_OBJECT_CLASS,
                ShsLdapAttributes.CLASS_SHS_ORGEXTENSION));

        List<Organization> organizations = findAll(null, filter, new OrganizationMapper());

        Collections.sort(organizations, new Comparator<Organization>() {
            @Override
            public int compare(Organization o1, Organization o2) {

                return o1.getOrgName().compareToIgnoreCase(o2.getOrgName());
            }
        });

        return organizations;
    }


	@Override
	public void deleteActor(Organization organization) {
		Name dn = buildDn(organization);

		try {
			for (ProductType o : getProductTypes(organization)) {
				removeProduct(organization, o);
			}
			
			DistinguishedName productTypesDn = (DistinguishedName) dn.clone();
			productTypesDn.add("ou", ShsLdapAttributes.OU_PRODUCT_TYPES);
			
			ldapTemplate.unbind(productTypesDn);
		} catch (Exception e) {
			log.warn("Error removing product types for organization: " + organization);
		}
		
		
		try {
			
			for (Agreement o : getAgreements(organization)) {
				removeAgreement(organization, o);
			}
			
			DistinguishedName agreementsDn = (DistinguishedName) dn.clone();
			agreementsDn.add("ou", ShsLdapAttributes.OU_AGREEMENTS);
			ldapTemplate.unbind(agreementsDn);
			
		} catch (Exception e) {
			log.warn("Error removing product types for organization: " + organization);
		}
		


		try {
			for (Address o : getAddresses(organization)) {
				removeAddress(organization, o);
			}

			DistinguishedName addressesDn = (DistinguishedName) dn.clone();
			addressesDn.add("ou", ShsLdapAttributes.OU_ADRESSES);
			
			ldapTemplate.unbind(addressesDn);
			
		} catch (Exception e) {
			log.warn("Error removing addresses for organization: " + organization);
		}
		

		try {
			ldapTemplate.unbind(dn);
			log.debug("organization {} deleted from directory", dn.toString());
		} catch (Exception e) {
			log.warn("Error removing organization: " + organization);
		}
	}

	@Override
	public void saveActor(Organization organization) {
		Name dn = buildDn(organization);
		boolean isNew = false;
		DirContextAdapter context = null;
		try {
			context = (DirContextAdapter)ldapTemplate.lookupContext(dn);
		} catch (NameNotFoundException e) {
			isNew = true;
			context = new DirContextAdapter(dn);
		}

		OrganizationMapper.mapToContext(organization, context);

		if (isNew) {
			ldapTemplate.bind(context);
			DistinguishedName dn2 = (DistinguishedName) dn.clone();
			dn2.add("ou", ShsLdapAttributes.OU_PRODUCT_TYPES);
			context = new DirContextAdapter(dn2);
			context.setAttributeValues("objectClass", new String[] {"top", "organizationalUnit"});
			ldapTemplate.bind(context);

			dn2 = (DistinguishedName) dn.clone();
			dn2.add("ou", ShsLdapAttributes.OU_AGREEMENTS);
			context = new DirContextAdapter(dn2);
			context.setAttributeValues("objectClass", new String[] {"top", "organizationalUnit"});
			ldapTemplate.bind(context);

			dn2 = (DistinguishedName) dn.clone();
			dn2.add("ou", ShsLdapAttributes.OU_ADRESSES);
			context = new DirContextAdapter(dn2);
			context.setAttributeValues("objectClass", new String[] {"top", "organizationalUnit"});
			ldapTemplate.bind(context);

			log.debug("organization {} created in directory", dn.toString());
		}
		else {
			ldapTemplate.modifyAttributes(context);
			log.debug("organization {} updated in directory", dn.toString());
		}
	}
	
	
	@Override
	public void saveProduct(Organization organization, ProductType product) {
		saveProduct12(organization, product);
		saveProduct11(organization, product);
	}
	
	private void saveProduct12(Organization organization, ProductType product) {
		Name dn = buildDn(organization);
		DirContextAdapter context = null;
		try {
			context = (DirContextAdapter)ldapTemplate.lookupContext(dn);
		} catch (NameNotFoundException e) {
			throw new IllegalArgumentException("organization for product non-exist");
		}

		dn = buildDn(organization, product);
		boolean isNew = false;
		try {
			context = (DirContextAdapter)ldapTemplate.lookupContext(dn);
		} catch (NameNotFoundException e) {
			isNew = true;
			context = new DirContextAdapter(dn);
		}

		ProductTypeMapper.mapToContext(organization, product, context);

		if (isNew) {
			ldapTemplate.bind(context);
			log.debug("product {} created in directory", dn.toString());
		} else {
			ldapTemplate.modifyAttributes(context);
			log.debug("product {} updated in directory", dn.toString());
		}
	}

	
	/**
	 * Save a product entry in v1.1 branch o=Myndighetsprodukter
	 * 
	 * @param organization
	 * @param product
	 */
	private void saveProduct11(Organization organization, ProductType product) {
		DirContextAdapter context = null;
		Name dn = build11Dn(organization, product);
		boolean isNew = false;
		try {
			context = (DirContextAdapter)ldapTemplate.lookupContext(dn);
		} catch (NameNotFoundException e) {
			isNew = true;
			context = new DirContextAdapter(dn);
		}

		ProductTypeMapper.mapToContext(organization, product, context);

		if (isNew) {
			ldapTemplate.bind(context);
			log.debug("product {} created in directory o=Myndighetsprodukter", dn.toString());
		} else {
			ldapTemplate.modifyAttributes(context);
			log.debug("product {} updated in directory o=Myndighetsprodukter", dn.toString());
		}
	}

	
	@Override
	public void removeProduct(Organization organization, ProductType product) {
		
		Name dn = buildDn(organization, product);
		ldapTemplate.unbind(dn);
		log.debug("product {} deleted in directory", dn.toString());

		try {
			dn = build11Dn(organization, product);
			ldapTemplate.unbind(dn);
		} catch (Exception e) {
			log.warn("Error removing product from o=Myndighetsprodukter: {}", product.toString());
		}	
	}
	

	@Override
	public List<ProductType> getProductTypes(Organization organization) throws DirectoryException {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_OBJECT_CLASS, ShsLdapAttributes.CLASS_PRODUCT_TYPE));

		return findAll(organization, filter, new ProductTypeMapper());
	}

	
	@Override
	public List<Agreement> getAgreements(Organization organization) throws DirectoryException {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_OBJECT_CLASS, ShsLdapAttributes.CLASS_AGREEMENT));

		return findAll(organization, filter, new AgreementMapper());
	}


	@Override
	public void saveAgreement(Organization organization, Agreement agreement) {
		Name dn = buildDn(organization);
		DirContextAdapter context = null;
		try {
			context = (DirContextAdapter)ldapTemplate.lookupContext(dn);
		} catch (NameNotFoundException e) {
			throw new IllegalArgumentException("organization for agreement non-exist");
		}

		dn = buildDn(organization, agreement);
		boolean isNew = false;
		try {
			context = (DirContextAdapter)ldapTemplate.lookupContext(dn);
		} catch (NameNotFoundException e) {
			isNew = true;
			context = new DirContextAdapter(dn);
		}

		
		AgreementMapper.mapToContext(organization, agreement, context);
		
		if (isNew) {
			ldapTemplate.bind(context);
			log.debug("agreement {} created in directory", dn.toString());
		}
		else {
			ldapTemplate.modifyAttributes(context);
			log.debug("agreement {} updated in directory", dn.toString());
		}
	}


	@Override
	public void removeAgreement(Organization organization, Agreement agreement) {
		Name dn = buildDn(organization, agreement);
		ldapTemplate.unbind(dn);
		log.debug("agreement {} deleted in directory", dn.toString());
	}
	
	
	@Override
	public void saveAddress(Organization organization, Address address) {
		save12Address(organization, address);
		save11Address(organization, address);
	}
	
	
	private void save12Address(Organization organization, Address address) {
		Name dn = buildDn(organization);
		DirContextAdapter context = null;
		try {
			context = (DirContextAdapter)ldapTemplate.lookupContext(dn);
		} catch (NameNotFoundException e) {
			throw new IllegalArgumentException("organization for address non-exist");
		}

		dn = buildDn(organization, address);
		boolean isNew = false;
		try {
			context = (DirContextAdapter)ldapTemplate.lookupContext(dn);
		} catch (NameNotFoundException e) {
			isNew = true;
			context = new DirContextAdapter(dn);
		}

		AddressMapper.mapToContext(address, context);

		if (isNew) {
			ldapTemplate.bind(context);
			log.debug("address {} created in directory", dn.toString());
		} else {
			ldapTemplate.modifyAttributes(context);
			log.debug("address {} updated in directory", dn.toString());
		}
	}
	
	private void save11Address(Organization organization, Address address) {
		Name dn =  build11Dn(organization, address);
		DirContextAdapter context = null;
		boolean isNew = false;
		try {
			context = (DirContextAdapter)ldapTemplate.lookupContext(dn);
		} catch (NameNotFoundException e) {
			isNew = true;
			context = new DirContextAdapter(dn);
		}

		AddressMapper.mapToContext(address, context);

		if (isNew) {
			ldapTemplate.bind(context);
			log.debug("address {} created in directory", dn.toString());
		} else {
			ldapTemplate.modifyAttributes(context);
			log.debug("address {} updated in directory", dn.toString());
		}
	}


	@Override
	public void removeAddress(Organization organization, Address address) {
		Name dn = buildDn(organization, address);
		ldapTemplate.unbind(dn);
		log.debug("address {} deleted in directory", dn.toString());
		
		try {
			dn = build11Dn(organization, address);
			ldapTemplate.unbind(dn);
		} catch (Exception e) {
			log.warn("Error removing address from o=Myndighetsaddresser: {}", address.toString());
		}	 
	}

	@Override
	public List<Address> getAddresses(final Organization organization) throws DirectoryException {

		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter(ShsLdapAttributes.ATTR_OBJECT_CLASS, ShsLdapAttributes.CLASS_ADDRESS));

		return findAll(organization, filter, new AddressMapper());
	}
	
	
	protected Name buildDn(Organization organization, Address address) {
		if (organization == null) {
			throw new IllegalArgumentException("Organization expected to be not null");
		}
		
		if (address == null) {
			throw new IllegalArgumentException("Address expected to be not null");
		}
		
		if (address.getOrganizationNumber() == null) {
			throw new IllegalArgumentException("OrgId must be specified in address");
		}
		
		if (address.getSerialNumber() == null) {
			throw new IllegalArgumentException("product id must be specified in address");
		}
		
		DistinguishedName dn = (DistinguishedName) buildDn(organization);
		dn.add("ou", ShsLdapAttributes.OU_ADRESSES);
		try {
			dn.add(ShsLdapAttributes.ATTR_ORGANIZATION_NUMBER + "=" + address.getOrganizationNumber() +
					"+" + ShsLdapAttributes.ATTR_SERIAL_NUMBER + "=" + address.getSerialNumber());
		} catch (InvalidNameException e) {
			throw new IllegalArgumentException("Wrong dn format: " + ShsLdapAttributes.ATTR_ORGANIZATION_NUMBER + "=" + address.getOrganizationNumber() +
					"+" + ShsLdapAttributes.ATTR_SERIAL_NUMBER + "=" + address.getSerialNumber());
		}
		return dn;
	}
	
	protected Name build11Dn(Organization organization, Address address) {
		if (organization == null) {
			throw new IllegalArgumentException("Organization expected to be not null");
		}
		
		if (address == null) {
			throw new IllegalArgumentException("Address expected to be not null");
		}
		
		if (address.getOrganizationNumber() == null) {
			throw new IllegalArgumentException("OrgId must be specified in address");
		}
		
		if (address.getSerialNumber() == null) {
			throw new IllegalArgumentException("product id must be specified in address");
		}
		
		DistinguishedName dn = new DistinguishedName();
		dn.add("o", ShsLdapAttributes.O_MYNDIGHETSADRESSER);
		try {
			dn.add(ShsLdapAttributes.ATTR_ORGANIZATION_NUMBER + "=" + address.getOrganizationNumber() +
					"+" + ShsLdapAttributes.ATTR_SERIAL_NUMBER + "=" + address.getSerialNumber());
		} catch (InvalidNameException e) {
			throw new IllegalArgumentException("Wrong dn format: " + ShsLdapAttributes.ATTR_ORGANIZATION_NUMBER + "=" + address.getOrganizationNumber() +
					"+" + ShsLdapAttributes.ATTR_SERIAL_NUMBER + "=" + address.getSerialNumber());
		}
		return dn;
	}

	protected Name buildDn(Organization organization, Agreement agreement) {
		if (organization == null) {
			throw new IllegalArgumentException("Organization expected to be not null");
		}
		
		if (agreement == null) {
			throw new IllegalArgumentException("Agreement expected to be not null");
		}
		
		if (agreement.getSerialNumber() == null) {
			throw new IllegalArgumentException("Serial number must be specified in agreement");
		}
		
		DistinguishedName dn = (DistinguishedName) buildDn(organization);
		dn.add("ou", ShsLdapAttributes.OU_AGREEMENTS);
		dn.add(ShsLdapAttributes.ATTR_SERIAL_NUMBER, agreement.getSerialNumber());
		return dn;
	}

	protected Name build11Dn(Organization organization, ProductType product) {
		if (organization == null) {
			throw new IllegalArgumentException("Organization expected to be not null");
		}
		
		if (product == null) {
			throw new IllegalArgumentException("ProductType expected to be not null");
		}
		
		if (product.getProductName() == null) {
			throw new IllegalArgumentException("Product name must be specified in product type");
		}
		
		DistinguishedName dn = new DistinguishedName();
		dn.add("o", ShsLdapAttributes.O_MYNDIGHETSPRODUKTER);
		dn.add(ShsLdapAttributes.ATTR_PRODUCT_NAME, product.getProductName());
		return dn;
	}


	protected Name buildDn(Organization organization, ProductType product) {
		if (organization == null) {
			throw new IllegalArgumentException("Organization expected to be not null");
		}
		
		if (product == null) {
			throw new IllegalArgumentException("ProductType expected to be not null");
		}
		
		if (product.getProductName() == null) {
			throw new IllegalArgumentException("Product name must be specified in product type");
		}
		
		DistinguishedName dn = (DistinguishedName) buildDn(organization);
		dn.add("ou", ShsLdapAttributes.OU_PRODUCT_TYPES);
		dn.add(ShsLdapAttributes.ATTR_PRODUCT_NAME, product.getProductName());
		return dn;
	}
	
	
	protected Name buildDn(Organization organization) {
		if (organization == null) {
			throw new IllegalArgumentException("Organization expected to be not null");
		}
		
		if (organization.getOrgName() == null) {
			throw new IllegalArgumentException("Organization name must be specified on organization");
		}
		
		DistinguishedName dn = new DistinguishedName();
		dn.add("o", organization.getOrgName());
		return dn;
	}



	
}
