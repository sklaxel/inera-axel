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


public class ShsLdapAttributes {

	public static final String O_MYNDIGHETSPRODUKTER = "Myndighetsprodukter";
	public static final String O_MYNDIGHETSADRESSER = "Myndighetsadresser";
	
	public static final String CLASS_TOP = "top";
    public static final String CLASS_ORGANISATION = "organization";
    public static final String CLASS_SHS_ORGEXTENSION = "shsOrgExtension";
	public static final String CLASS_ADDRESS = "shsAddresses";
	public static final String CLASS_AGREEMENT = "shsAgreement";
    public static final String CLASS_PRODUCT_TYPE = "shsProduct";

    public static final String OU_AGREEMENTS = "Agreements";
    public static final String OU_PRODUCT_TYPES = "Product types";
    public static final String OU_ADRESSES = "Addresses";
    
	public static final String ATTR_OBJECT_CLASS = "objectclass";
    public static final String ATTR_PRODUCT_DESCRIPTION = "prodDescr";
    public static final String ATTR_PRODUCT_ID = "shsProductID";
    public static final String ATTR_DESCRIPTION = "description";
    public static final String ATTR_SERIAL_NUMBER = "serialNumber";
    public static final String ATTR_OWNER = "owner";
    public static final String ATTR_FAX_NUMBER = "facsimileTelephoneNumber";
    public static final String ATTR_KEYWORDS = "keywords";
    public static final String ATTR_POSTAL_CODE = "postalCode";
    public static final String ATTR_POSTAL_ADDRESS = "postalAddress";
    public static final String ATTR_ORGANIZATION_NAME = "o";
    public static final String ATTR_STREET_ADDRESS = "street";
    public static final String ATTR_LABELED_URI = "labeledURI";
    public static final String ATTR_STREET_ADDRESS_ALIAS = "streetAddress";
    public static final String ATTR_POST_OFFICE_BOX = "postOfficeBox";
    public static final String ATTR_PHONE_NUMBER = "telephoneNumber";
    public static final String ATTR_USER_PASSWORD = "userPassword";
    public static final String ATTR_PRODUCT_NAME = "productName";
    public static final String ATTR_PRINCIPAL = "principal";
    public static final String ATTR_DELIVERY_METHODS = "shsDeliveryMethods";
    public static final String ATTR_PREFERRED_DELIVERY_METHOD = "shsPreferredDeliveryMethod";
    public static final String ATTR_ORGANIZATION_NUMBER = "organizationNumber";
    public static final String ATTR_DELIVERY_CONFIRMATION = "shsDeliveryConfirmation";
    public static final String ATTR_TRANSFERTYPE = "shsTransferType";
    public static final String ATTR_ERROR = "shsError";
   
}
