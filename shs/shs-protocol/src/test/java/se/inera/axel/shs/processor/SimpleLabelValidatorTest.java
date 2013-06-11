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
package se.inera.axel.shs.processor;

import com.natpryce.makeiteasy.Maker;
import org.testng.annotations.*;
import se.inera.axel.shs.exception.IllegalMessageStructureException;
import se.inera.axel.shs.exception.IllegalProductTypeException;
import se.inera.axel.shs.exception.IllegalReceiverException;
import se.inera.axel.shs.exception.IllegalSenderException;
import se.inera.axel.shs.xml.label.ObjectFactory;
import se.inera.axel.shs.xml.label.*;

import java.util.Collections;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.*;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.*;
@SuppressWarnings({"unchecked", "static-access"})
public class SimpleLabelValidatorTest {
	ObjectFactory shsLabelFactory = new ObjectFactory();
	
	SimpleLabelValidator validator;
	Maker<ShsLabel> labelMaker;
	
	@Test
	public void validateWithValidLabelShouldBeSuccessful() {
		ShsLabel label = make(labelMaker);
		validator.validate(label);
	}
	
	@Test(expectedExceptions = IllegalSenderException.class)
	public void originatorIsMandatoryIfNoFromAddressIsSpecified() {
		ShsLabel label = labelMaker.but(
				with(originatorOrFrom,
						Collections.<Object>emptyList())).make();
		
		validator.validate(label);
	}
	
	@Test(expectedExceptions = IllegalSenderException.class)
	public void originatorIsMandatoryIfFromAddressIsEmpty() {
		ShsLabel label = labelMaker.but(
				with(originatorOrFrom,
						listOf(a(From,
								with(From.value,"")
								)))).make();
		
		validator.validate(label);
	}
	
	@Test(expectedExceptions = IllegalReceiverException.class, enabled=false) // TODO what should happen if the value is null?
	public void shouldThrowWhenThereIsNoToAddress() {
		ShsLabel shsLabel = labelMaker.but(
				with(to, a(To,
						with(To.value, (String)null))))
				.make();
		
		validator.validate(shsLabel);
	}
	
	@Test(expectedExceptions = IllegalSenderException.class)
	public void shouldThrowIfNeitherFromNorOriginatorIsGiven() {
		ShsLabel shsLabel = labelMaker.but(
				with(to, (To)null)
				, with(originatorOrFrom, Collections.<Object>emptyList())
				).make();
		
		validator.validate(shsLabel);
	}
	
	@Test(expectedExceptions = IllegalProductTypeException.class)
	public void shouldThrowIfNoProductIsGiven() {
		ShsLabel label = labelMaker.but(
				with(product, (Product)null)
				).make();
		
		validator.validate(label);
	}
	
	@Test(expectedExceptions = IllegalProductTypeException.class)
	public void shouldThrowIfTheProductTypeIdContainsWhitespace() {
		ShsLabel label = labelMaker.but(
				with(product, a(Product, 
						with(Product.value, "12345678-1234- 1234-1234-123456789012")))
				).make();
		
		validator.validate(label);
	}
	
	@Test(expectedExceptions = IllegalProductTypeException.class)
	public void shouldThrowIfTheProductTypeIdIsInvalid() {
		ShsLabel label = labelMaker.but(
				with(product, a(Product, 
						with(Product.value, "12345678-12341234-1234-123456789012")))
				).make();
		
		validator.validate(label);
	}
	
	@Test(expectedExceptions = IllegalProductTypeException.class)
	public void shouldThrowIfProductTypeIdIsNull() {
		ShsLabel label = labelMaker.but(
				with(product, a(Product, 
						with(Product.value, (String)null)))
				).make();
		
		validator.validate(label);
	}
	
	@Test(expectedExceptions = IllegalMessageStructureException.class)
	public void shouldThrowIfSequenceTypeIsNull() {
		ShsLabel label = labelMaker.but(
				with(sequenceType, (SequenceType)null)).make();
		
		validator.validate(label);
	}

	@BeforeMethod
	public void beforeMethod() {
	}

	@AfterMethod
	public void afterMethod() {
	}

	@BeforeClass
	public void beforeClass() {
		validator = new SimpleLabelValidator();
		labelMaker = a(ShsLabel);
	}

	@AfterClass
	public void afterClass() {
	}

}
