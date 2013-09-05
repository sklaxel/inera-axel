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
package se.inera.axel.shs.broker.webconsole;

import se.inera.axel.shs.xml.product.ObjectFactory;
import se.inera.axel.shs.xml.product.Principal;
import se.inera.axel.shs.xml.product.ShsProduct;

public class ObjectMother {
	public static final String DEFAULT_PRODUCT_ID = "00000000-0000-0000-0000-000000000001";

	public static ShsProduct createShsProduct() {
		ObjectFactory productFactory = new ObjectFactory();
		ShsProduct product = productFactory.createShsProduct();
	    
	    product.setCommonName("testproductCommonName");
	    product.setDescription("desc");
	    product.setLabeledURI("labeledUri");
	    Principal principal = new Principal();
	    principal.setCommonName("principalCN");
	    principal.setLabeledURI("principalURI");
	    principal.setValue("principalValue");
	    product.setPrincipal(principal);
	    product.setUuid(DEFAULT_PRODUCT_ID);
	    product.setVersion("1");
	    
	    se.inera.axel.shs.xml.product.Data data = productFactory.createData();
	    data.setDatapartType("xml");
	    
	    se.inera.axel.shs.xml.product.Mime mime = productFactory.createMime();
	
	    mime.setType("text");
	    mime.setSubtype("plain");
	    mime.setTransferEncoding("binary");
	    mime.setTextCharset("UTF-8");
	
	    data.setMime(mime);
	    data.setMinOccurs("1");
	
	    product.getData().add(data);
	    
	    return product;
	}

}
