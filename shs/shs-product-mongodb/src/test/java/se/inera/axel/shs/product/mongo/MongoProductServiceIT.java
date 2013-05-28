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
package se.inera.axel.shs.product.mongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.inera.axel.shs.xml.product.ObjectFactory;
import se.inera.axel.shs.xml.product.Principal;
import se.inera.axel.shs.xml.product.ShsProduct;

@ContextConfiguration
public class MongoProductServiceIT extends AbstractTestNGSpringContextTests {
	@Autowired
	private MongoShsProductRepository repository;
	
	@Autowired
	private ProductAssembler assembler;
	
	private MongoProductService productService;

	@Test
	public void testMongoProductService() {
		ShsProduct product = productService.getProduct("123456789");
		Assert.assertEquals(product.getUuid(), "123456789");
	}

	@BeforeMethod
	public void beforeMethod() {
	}

	@AfterMethod
	public void afterMethod() {
	}

	@BeforeClass
	public void beforeClass() {
		ObjectFactory productFactory = new ObjectFactory();
		ShsProduct product = productFactory.createShsProduct();
	    
	    product.setCommonName("testproductCommonName");
	    product.setDescription("desc");
	    product.setLabeledURI("labeledUri");
	    Principal principal = new Principal();
	    principal.setCommonName("principalCN");
	    principal.setLabeledURI("principalLURI");
	    principal.setvalue("principalValue");
	    product.setPrincipal(principal);
	    product.setUuid("123456789");
	    product.setVersion("1");
	    
	    se.inera.axel.shs.xml.product.Data data = productFactory.createData();
	    data.setDatapartType("xml");
	    
	    se.inera.axel.shs.xml.product.Mime mime = productFactory.createMime();

	    mime.setType("text");
	    mime.setSubtype("plain");
	    mime.setTransferEncoding("binary");
	    mime.setTextCharset("UTF-8");

	    data.setMime(mime);

	    product.getData().add(data);
	    	    
		repository.save(assembler.assembleMongoShsProduct(product));
		
		productService = new MongoProductService();
		Assert.assertNotNull(repository);
		ReflectionTestUtils.setField(productService, "mongoShsProductRepository", repository);
		ReflectionTestUtils.setField(productService, "assembler", assembler);
	}
	
	@AfterClass
	public void afterClass() {
	}

}
