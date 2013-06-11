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
package se.inera.axel.shs.camel.component;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.inera.axel.shs.camel.AbstractShsTestNGTests;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.SequenceType;

@ContextConfiguration(locations="classpath*:se.inera.axel.shs.camel.AbstractShsTestNGTests-context.xml")
public class DefaultShsExceptionHandlerTest extends AbstractShsTestNGTests {
	private CamelContext camelContext;
	private Exchange inExchange;
	private Exchange returnedExchange;
	
	@Test
	public void isExceptionShouldReturnTrueWhenBodyIsNotAnShsMessage() {
		DefaultShsExceptionHandler exceptionHandler = new DefaultShsExceptionHandler();
		exceptionHandler.setReturnError(false);
		
		returnedExchange.getIn().setBody("Not an ShsMessage");
		
		boolean result = exceptionHandler.isException(returnedExchange);
		
		assertTrue(result, "Should be marked as exception since the body is not an ShsMessage");
	}
	
	@Test
	public void isExceptionShouldReturnTrueWhenAnExceptionHasBeenSet() {
		DefaultShsExceptionHandler exceptionHandler = new DefaultShsExceptionHandler();
		exceptionHandler.setReturnError(false);

		returnedExchange.getIn().setBody(createTestMessage());
        returnedExchange.setException(new RuntimeException());

		boolean result = exceptionHandler.isException(returnedExchange);

		assertTrue(result, "Should be marked as exception since an exception is set on the exchange");
	}

	@Test
	public void isExceptionShouldReturnFalseWhenBodyIsAnShsMessageAndNoExceptionHasBeenSet() {
		DefaultShsExceptionHandler exceptionHandler = new DefaultShsExceptionHandler();
		exceptionHandler.setReturnError(false);

		returnedExchange.getIn().setBody(createTestMessage());

		boolean result = exceptionHandler.isException(returnedExchange);

		assertFalse(result, "Should not be an exception since no exception is set on the exchange");
	}
	
	//@Test
	public void handleExceptionShouldSetExceptionIfReturnedBodyIsNotShsMessage() {
		DefaultShsExceptionHandler exceptionHandler = new DefaultShsExceptionHandler();
		exceptionHandler.setReturnError(false);
		
		inExchange.getIn().setBody("Not an ShsMessage");
		returnedExchange.getIn().setBody("Not an ShsMessage");
		
		exceptionHandler.handleException(inExchange, returnedExchange);
		
		assertNotNull(inExchange.getException(), "Exception should have been set since body is not an ShsMessage");
	}

	@Test
	public void handleExceptionShouldSetExceptionIfAnExceptionIsSetOnTheExchange() {
		DefaultShsExceptionHandler exceptionHandler = new DefaultShsExceptionHandler();
		exceptionHandler.setReturnError(false);
		
		ShsMessage message = createTestMessage();
		inExchange.getIn().setBody(message);
		returnedExchange.getIn().setBody(message);
		returnedExchange.setException(new RuntimeException());
		
		exceptionHandler.handleException(inExchange, returnedExchange);
		
		assertNotNull(inExchange.getException(), "No exception was set on the exchange when the returned exchange had an exception");
	}
	
	@Test
	public void handleExceptionShouldSendBackAnShsError() {
		DefaultShsExceptionHandler exceptionHandler = new DefaultShsExceptionHandler();
		exceptionHandler.setReturnError(true);
		
		ShsMessage message = createTestMessage();
		inExchange.getIn().setBody(message);
		returnedExchange.getIn().setBody(message);
		returnedExchange.setException(new RuntimeException());
		
		exceptionHandler.handleException(inExchange, returnedExchange);
		
		assertEquals(inExchange.getIn().getBody(ShsMessage.class).getLabel().getProduct().getvalue(), "error", "Returned body was not an Shs error message");
		assertEquals(inExchange.getIn().getBody(ShsMessage.class).getLabel().getSequenceType(), SequenceType.ADM, "Incorrect SequenceType");
	}
	
	@BeforeMethod
	public void beforeMethod() {
		camelContext = new DefaultCamelContext();
		inExchange = new DefaultExchange(camelContext);
		returnedExchange = new DefaultExchange(camelContext);
	}
	
}
