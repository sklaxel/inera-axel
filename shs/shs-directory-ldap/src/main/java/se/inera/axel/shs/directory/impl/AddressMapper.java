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
package se.inera.axel.shs.directory.impl;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.simple.ParameterizedContextMapper;

import se.inera.axel.shs.directory.Address;

class AddressMapper implements ParameterizedContextMapper<Address> {

	@Override
	public Address mapFromContext(Object context) {
        if (context == null) return null;

        DirContextAdapter ctx = (DirContextAdapter)context;
        final Address address = new Address();
        
        address.setOrganizationNumber(ctx.getStringAttribute(ShsLdapAttributes.ATTR_ORGANIZATION_NUMBER));
        address.setDeliveryMethods(ctx.getStringAttribute(ShsLdapAttributes.ATTR_DELIVERY_METHODS));
        address.setSerialNumber(ctx.getStringAttribute(ShsLdapAttributes.ATTR_SERIAL_NUMBER));

        return address;
    }
	
	public static void mapToContext(Address address, DirContextAdapter context) {
		context.setAttributeValues(ShsLdapAttributes.ATTR_OBJECT_CLASS, 
				new String[] {ShsLdapAttributes.CLASS_TOP, ShsLdapAttributes.CLASS_ADDRESS});
//		context.setAttributeValue(ShsLdapAttributes.ATTR_ORGANIZATION_NUMBER, address.organizationNumber);
//		context.setAttributeValue(ShsLdapAttributes.ATTR_SERIAL_NUMBER, address.serialNumber);
		context.setAttributeValue(ShsLdapAttributes.ATTR_DELIVERY_METHODS, address.deliveryMethods);
	}
	
}