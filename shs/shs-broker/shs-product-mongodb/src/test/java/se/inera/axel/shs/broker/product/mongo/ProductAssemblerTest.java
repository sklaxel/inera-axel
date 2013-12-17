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
package se.inera.axel.shs.broker.product.mongo;

import static com.natpryce.makeiteasy.MakeItEasy.an;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static org.testng.Assert.assertEquals;

import org.dozer.DozerBeanMapper;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import se.inera.axel.shs.broker.product.mongo.model.Data;
import se.inera.axel.shs.broker.product.mongo.model.Mime;
import se.inera.axel.shs.broker.product.mongo.model.MongoShsProduct;
import se.inera.axel.shs.xml.Product.ShsProductMaker;
import se.inera.axel.shs.xml.product.ShsProduct;

import com.natpryce.makeiteasy.Maker;

@SuppressWarnings("unchecked")
public class ProductAssemblerTest {
	private ProductAssembler productAssembler = new ProductAssembler();
	private DozerBeanMapper mapper;

	@BeforeMethod
	public void beforeMethod() {
	}

	@AfterMethod
	public void afterMethod() {
	}

	@BeforeClass
	public void beforeClass() {
		productAssembler.configureMapper();
		mapper = (DozerBeanMapper)ReflectionTestUtils.getField(productAssembler, "mapper");
	}

	@AfterClass
	public void afterClass() {
	}

	@Test
	public void mapDataMinOccursWithDefault() {
		se.inera.axel.shs.xml.product.Data src = new se.inera.axel.shs.xml.product.Data();
		String value = "1";
		src.setMinOccurs(value);
		Data dst = mapper.map(src, Data.class);
		
		String dstField = (String)ReflectionTestUtils.getField(dst, "minOccurs");
		assertEquals(dstField, value);
	}

	@Test
	public void mapDataMaxOccursWithDefault() {
		se.inera.axel.shs.xml.product.Data src = new se.inera.axel.shs.xml.product.Data();
		String value = "1";
		src.setMaxOccurs(value);
		Data dst = mapper.map(src, Data.class);
		
		String dstField = (String)ReflectionTestUtils.getField(dst, "maxOccurs");
		assertEquals(dstField, value);
	}

	@Test
	public void mapMimeTextCharSetWithDefault() {
		se.inera.axel.shs.xml.product.Mime src = new se.inera.axel.shs.xml.product.Mime();
		String value = "iso-8859-1";
		src.setTextCharset(value);
		Mime dst = mapper.map(src, Mime.class);
		
		String dstField = (String)ReflectionTestUtils.getField(dst, "textCharset");
		assertEquals(dstField, value);
	}

	@Test
	public void mapMimeSubTypeWithDefault() {
		se.inera.axel.shs.xml.product.Mime src = new se.inera.axel.shs.xml.product.Mime();
		String value = "xml";
		src.setSubtype(value);
		Mime dst = mapper.map(src, Mime.class);
		
		String dstField = (String)ReflectionTestUtils.getField(dst, "subtype");
		assertEquals(dstField, value);
	}

	@Test
	public void mapMimeTypeWithDefault() {
		se.inera.axel.shs.xml.product.Mime src = new se.inera.axel.shs.xml.product.Mime();
		String value = "text";
		src.setType(value);
		Mime dst = mapper.map(src, Mime.class);
		
		String dstField = (String)ReflectionTestUtils.getField(dst, "type");
		assertEquals(dstField, value);
	}

	@Test
	public void mapMimeTransferEncodingWithDefault() {
		se.inera.axel.shs.xml.product.Mime src = new se.inera.axel.shs.xml.product.Mime();
		String value = "binary";
		src.setTransferEncoding(value);
		Mime dst = mapper.map(src, Mime.class);
		
		String dstField = (String)ReflectionTestUtils.getField(dst, "transferEncoding");
		assertEquals(dstField, value);
	}

	@Test
	public void mapRespRequiredWithDefault() {
		se.inera.axel.shs.xml.product.ShsProduct src = new se.inera.axel.shs.xml.product.ShsProduct();
		String value = "yes";
		src.setRespRequired(value);
		MongoShsProduct dst = mapper.map(src, MongoShsProduct.class);
		
		String dstField = (String)ReflectionTestUtils.getField(dst, "respRequired");
		assertEquals(dstField, value);
	}

	@Test
	public void mapVersionWithDefault() {
		se.inera.axel.shs.xml.product.ShsProduct src = new se.inera.axel.shs.xml.product.ShsProduct();
		String value = "1.2";
		src.setVersion(value);
		MongoShsProduct dst = mapper.map(src, MongoShsProduct.class);
		
		String dstField = (String)ReflectionTestUtils.getField(dst, "version");
		assertEquals(dstField, value);
	}

	@Test
	public void mapShsProduct() {
		Maker<ShsProduct> productMaker = an(ShsProductMaker.ShsProduct);
		ShsProduct shsProduct = make(productMaker);
		
		MongoShsProduct mongoShsProduct = productAssembler.assembleMongoShsProduct(shsProduct);
		ShsProduct shsProduct2 = productAssembler.assembleShsProduct(mongoShsProduct);
		
		ReflectionAssert.assertReflectionEquals(shsProduct, shsProduct2);
	}
}
