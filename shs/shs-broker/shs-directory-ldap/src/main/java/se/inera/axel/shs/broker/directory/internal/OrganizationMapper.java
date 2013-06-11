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

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.simple.ParameterizedContextMapper;
import se.inera.axel.shs.broker.directory.Organization;

class OrganizationMapper implements ParameterizedContextMapper<Organization> {

	@Override
	public Organization mapFromContext(Object context) {
        if (context == null) return null;

        DirContextAdapter ctx = (DirContextAdapter)context;
        final Organization organization = new Organization();
        
        organization.setOrgName(ctx.getStringAttribute(ShsLdapAttributes.ATTR_ORGANIZATION_NAME));
        organization.setDescription(ctx.getStringAttribute(ShsLdapAttributes.ATTR_DESCRIPTION));
        organization.setPostalAddress(ctx.getStringAttribute(ShsLdapAttributes.ATTR_POSTAL_ADDRESS));
        organization.setPostalCode(ctx.getStringAttribute(ShsLdapAttributes.ATTR_POSTAL_CODE));
        organization.setPostOfficeBox(ctx.getStringAttribute(ShsLdapAttributes.ATTR_POST_OFFICE_BOX));
        
        if (ctx.getStringAttribute(ShsLdapAttributes.ATTR_STREET_ADDRESS) != null) {
            organization.setStreetAddress(ctx.getStringAttribute(ShsLdapAttributes.ATTR_STREET_ADDRESS));
        } else {
            organization.setStreetAddress(ctx.getStringAttribute(ShsLdapAttributes.ATTR_STREET_ADDRESS_ALIAS));
        }
        
        organization.setFaxNumber(ctx.getStringAttribute(ShsLdapAttributes.ATTR_FAX_NUMBER));
        organization.setPhoneNumber(ctx.getStringAttribute(ShsLdapAttributes.ATTR_PHONE_NUMBER));
        organization.setLabeledUri(ctx.getStringAttribute(ShsLdapAttributes.ATTR_LABELED_URI));
        organization.setOrgNumber(ctx.getStringAttribute(ShsLdapAttributes.ATTR_ORGANIZATION_NUMBER));
        return organization;
    }
	
	
	public static void mapToContext(Organization organization, DirContextAdapter context) {
		
		context.setAttributeValues(ShsLdapAttributes.ATTR_OBJECT_CLASS, 
				new String[] {ShsLdapAttributes.CLASS_TOP, ShsLdapAttributes.CLASS_ORGANISATION, ShsLdapAttributes.CLASS_SHS_ORGEXTENSION});
//		context.setAttributeValue(ShsLdapAttributes.ATTR_ORGANIZATION_NAME, organization.orgName);
		context.setAttributeValue(ShsLdapAttributes.ATTR_DESCRIPTION, organization.getDescription());
		context.setAttributeValue(ShsLdapAttributes.ATTR_POSTAL_ADDRESS, organization.getPostalAddress());
		context.setAttributeValue(ShsLdapAttributes.ATTR_POSTAL_CODE, organization.getPostalCode());
		context.setAttributeValue(ShsLdapAttributes.ATTR_POST_OFFICE_BOX, organization.getPostOfficeBox());
		context.setAttributeValue(ShsLdapAttributes.ATTR_STREET_ADDRESS, organization.getStreetAddress());
		context.setAttributeValue(ShsLdapAttributes.ATTR_FAX_NUMBER, organization.getFaxNumber());
		context.setAttributeValue(ShsLdapAttributes.ATTR_PHONE_NUMBER, organization.getPhoneNumber());
		context.setAttributeValue(ShsLdapAttributes.ATTR_USER_PASSWORD, organization.getUserPassword());

		context.setAttributeValue(ShsLdapAttributes.ATTR_ORGANIZATION_NUMBER, organization.getOrgNumber());
		context.setAttributeValue(ShsLdapAttributes.ATTR_LABELED_URI, organization.getLabeledUri());

	}
}