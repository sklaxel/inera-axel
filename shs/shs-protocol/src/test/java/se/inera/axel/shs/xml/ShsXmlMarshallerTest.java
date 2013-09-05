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
package se.inera.axel.shs.xml;

import org.testng.annotations.Test;
import se.inera.axel.shs.processor.ShsAgreementMarshaller;
import se.inera.axel.shs.processor.ShsProductMarshaller;
import se.inera.axel.shs.xml.agreement.ShsAgreement;
import se.inera.axel.shs.xml.product.ShsProduct;

import java.net.URL;

import static org.testng.Assert.*;

public class ShsXmlMarshallerTest {

	URL templateProductFile = getClass().getResource("/template-product.xml");
	URL brokenProductFile = getClass().getResource("/broken-product.xml");
	URL templateAgreementFile = getClass().getResource("/template-agreement.xml");

	ShsProductMarshaller productMarshaller = new ShsProductMarshaller();
	ShsAgreementMarshaller agreementMarshaller = new ShsAgreementMarshaller();
	
	@Test
	public void unmarshalTemplateProductFromStream() throws Exception {
		
		ShsProduct templateProduct = productMarshaller.unmarshal(templateProductFile.openStream());
		
		assertEquals(templateProduct.getCommonName(), "template");
		
		assertNotNull(templateProduct.getPrincipal(), "principal should not be null");
		assertEquals(templateProduct.getPrincipal().getValue(), "1234567890");
		
	}

	
	@Test(expectedExceptions=XmlException.class)
	public void unmarshalBrokenProductShouldThrow() throws Exception {
		
		ShsProduct templateProduct = productMarshaller.unmarshal(brokenProductFile.openStream());
		
		assertEquals(templateProduct.getCommonName(), "template");
		
		assertNotNull(templateProduct.getPrincipal(), "principal should not be null");
		assertEquals(templateProduct.getPrincipal().getValue(), "1234567890");
	}
	
	@Test
	public void unmarshalThenMarshalTemplateProduct() throws Exception {
		
		ShsProduct templateProduct = productMarshaller.unmarshal(templateProductFile.openStream());
		
		assertEquals(templateProduct.getCommonName(), "template");
		
		assertNotNull(templateProduct.getPrincipal(), "principal should not be null");
		assertEquals(templateProduct.getPrincipal().getValue(), "1234567890");
		
		String xml = productMarshaller.marshal(templateProduct);
		
		assertNotNull(xml, "product xml should not be null after marshal");
		assertTrue(xml.contains("shs-product-type-1.2.dtd"), "product xml should contain dtd doctype");
		
	}

	@Test
	public void unmarshalThenMarshalTemplateAgreement() throws Exception {
		
		ShsAgreement templateAgreement = agreementMarshaller.unmarshal(templateAgreementFile.openStream());
		
		assertEquals(templateAgreement.getUuid(), "7477c939-bba9-4b3a-8017-1dd656a0a6f1");
		
		assertNotNull(templateAgreement.getShs(), "<shs> should not be null");
		assertNotNull(templateAgreement.getShs().getPrincipal(), "<shs/principal> should not be null");
		assertEquals(templateAgreement.getShs().getPrincipal().getValue(), "1234567890");
		
		String xml = agreementMarshaller.marshal(templateAgreement);
		
		assertNotNull(xml, "agreement xml should not be null after marshal");
		assertTrue(xml.contains("shs-agreement-1.2.dtd"), "agreement xml should contain dtd doctype");
		
	}
}
