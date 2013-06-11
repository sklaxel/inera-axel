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
package se.inera.axel.shs.broker.directory.internal;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.simple.ParameterizedContextMapper;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.directory.ProductType;
import se.inera.axel.shs.xml.UrnActor;
import se.inera.axel.shs.xml.ShsUrn;

class ProductTypeMapper implements ParameterizedContextMapper<ProductType> {

	@Override
	public ProductType mapFromContext(Object context) {
        if (context == null) return null;

        DirContextAdapter ctx = (DirContextAdapter)context;
        final ProductType product = new ProductType();
        
        if (ctx.getStringAttribute(ShsLdapAttributes.ATTR_PRINCIPAL) != null) {
            final ShsUrn principal = UrnActor.valueOf(ctx.getStringAttribute(ShsLdapAttributes.ATTR_PRINCIPAL));
            product.setPrincipal(principal.getValue());
        }
    	product.setProdDescr(ctx.getStringAttribute(ShsLdapAttributes.ATTR_PRODUCT_DESCRIPTION));
    	product.setOwner(ctx.getStringAttribute(ShsLdapAttributes.ATTR_OWNER));
    	product.setProductName(ctx.getStringAttribute(ShsLdapAttributes.ATTR_PRODUCT_NAME));
    	product.setSerialNumber(ctx.getStringAttribute(ShsLdapAttributes.ATTR_SERIAL_NUMBER));
    	product.setDescription(ctx.getStringAttribute(ShsLdapAttributes.ATTR_DESCRIPTION));
    	product.setLabeledUri(ctx.getStringAttribute(ShsLdapAttributes.ATTR_LABELED_URI));
    	product.setKeywords(ctx.getStringAttribute(ShsLdapAttributes.ATTR_KEYWORDS));
    	product.setPreferredDeliveryMethod(ctx.getStringAttribute(ShsLdapAttributes.ATTR_PREFERRED_DELIVERY_METHOD));
    	product.setUserPassword(ctx.getStringAttribute(ShsLdapAttributes.ATTR_USER_PASSWORD));
        return product;
    }
	
	
	public static void mapToContext(Organization organization, ProductType product, DirContextAdapter context) {
		context.setAttributeValues(ShsLdapAttributes.ATTR_OBJECT_CLASS, 
				new String[] {ShsLdapAttributes.CLASS_TOP, ShsLdapAttributes.CLASS_PRODUCT_TYPE});
//		context.setAttributeValue(ShsLdapAttributes.ATTR_PRODUCT_NAME, product.getProductName());
		context.setAttributeValue(ShsLdapAttributes.ATTR_SERIAL_NUMBER, product.getSerialNumber());
		ShsUrn principal = ShsUrn.valueOf(organization.getOrgNumber());
		context.setAttributeValue(ShsLdapAttributes.ATTR_PRINCIPAL, principal.toUrnForm());
		context.setAttributeValue(ShsLdapAttributes.ATTR_DESCRIPTION, product.getDescription());
		context.setAttributeValue(ShsLdapAttributes.ATTR_LABELED_URI, product.getLabeledUri());
		context.setAttributeValue(ShsLdapAttributes.ATTR_KEYWORDS, product.getKeywords());
		context.setAttributeValue(ShsLdapAttributes.ATTR_PRODUCT_DESCRIPTION, product.getProdDescr());
		context.setAttributeValue(ShsLdapAttributes.ATTR_PREFERRED_DELIVERY_METHOD, product.getPreferredDeliveryMethod());
		// TODO set owner to DN of owning organization.
		context.setAttributeValue(ShsLdapAttributes.ATTR_OWNER, product.getOwner());
	}


}