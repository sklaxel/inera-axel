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
import se.inera.axel.shs.broker.directory.Agreement;
import se.inera.axel.shs.xml.ShsUrn;

class AgreementMapper implements ParameterizedContextMapper<Agreement> {

	@Override
	public Agreement mapFromContext(Object context) {
        if (context == null) return null;

        DirContextAdapter ctx = (DirContextAdapter)context;
        final Agreement agreement = new Agreement();
        
        if (ctx.getStringAttribute(ShsLdapAttributes.ATTR_PRINCIPAL) != null) {
            final ShsUrn principal = ShsUrn.valueOf(ctx.getStringAttribute(ShsLdapAttributes.ATTR_PRINCIPAL));
            
            agreement.setPrincipal(principal.getValue());
        }
        agreement.setTransferType(ctx.getStringAttribute(ShsLdapAttributes.ATTR_TRANSFERTYPE));
        agreement.setDeliveryConfirmation(ctx.getStringAttribute(ShsLdapAttributes.ATTR_DELIVERY_CONFIRMATION));
        agreement.setError(ctx.getStringAttribute(ShsLdapAttributes.ATTR_ERROR));
        agreement.setProductName(ctx.getStringAttribute(ShsLdapAttributes.ATTR_PRODUCT_NAME));
        agreement.setProductId(ctx.getStringAttribute(ShsLdapAttributes.ATTR_PRODUCT_ID));
        agreement.setSerialNumber(ctx.getStringAttribute(ShsLdapAttributes.ATTR_SERIAL_NUMBER));
        agreement.setLabeledUri(ctx.getStringAttribute(ShsLdapAttributes.ATTR_LABELED_URI));

        agreement.setDescription(ctx.getStringAttribute(ShsLdapAttributes.ATTR_DESCRIPTION));

        return agreement;
    }

	public static void mapToContext(Organization organization, Agreement agreement, DirContextAdapter context) {
		context.setAttributeValues(ShsLdapAttributes.ATTR_OBJECT_CLASS, 
				new String[] {ShsLdapAttributes.CLASS_TOP, ShsLdapAttributes.CLASS_AGREEMENT});
	
		ShsUrn principal = ShsUrn.valueOf(organization.getOrgNumber());
		context.setAttributeValue(ShsLdapAttributes.ATTR_PRINCIPAL, principal.toUrnForm());
		context.setAttributeValue(ShsLdapAttributes.ATTR_PRODUCT_NAME, agreement.getProductName());
		context.setAttributeValue(ShsLdapAttributes.ATTR_PRODUCT_ID, agreement.getProductId());
	//	context.setAttributeValue(ShsLdapAttributes.ATTR_SERIAL_NUMBER, agreement.serialNumber);
		context.setAttributeValue(ShsLdapAttributes.ATTR_LABELED_URI, agreement.getLabeledUri());
		context.setAttributeValue(ShsLdapAttributes.ATTR_DELIVERY_CONFIRMATION, agreement.getDeliveryConfirmation());
		context.setAttributeValue(ShsLdapAttributes.ATTR_ERROR, agreement.getError());
		context.setAttributeValue(ShsLdapAttributes.ATTR_TRANSFERTYPE, agreement.getTransferType());
		context.setAttributeValue(ShsLdapAttributes.ATTR_DESCRIPTION, agreement.getDescription());
	}
	
}