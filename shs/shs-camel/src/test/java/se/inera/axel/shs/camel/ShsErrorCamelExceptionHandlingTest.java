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
package se.inera.axel.shs.camel;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelTestSupport;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;
import se.inera.axel.shs.xml.management.Confirmation;
import se.inera.axel.shs.xml.management.Error;
import se.inera.axel.shs.xml.management.ShsManagement;

import java.util.Date;

@ContextConfiguration
public class ShsErrorCamelExceptionHandlingTest extends CamelTestSupport {
	@Produce(uri = "direct:start")
	ProducerTemplate producer;
	
	@EndpointInject(uri = "mock:result")
	MockEndpoint resultEndpoint;
	
	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:start")
				.to("direct:shs")
				.to("mock:result");
			
				
				from("direct:startOnException")
				.onException(Exception.class)
					.setBody(simple("Error happened on ${exception.corrId}"))
					.handled(true)
				.end()
				.bean(new ThrowExceptionOnShsErrorProcessor())
				.to("mock:result");
				
				
				from("direct:shs")
				.bean(new ThrowExceptionOnShsErrorProcessor());
			
			}
		};
	}
	
	@DirtiesContext
	@Test
	public void testShouldThrowException() throws Exception {
		ShsManagement management = new ShsManagement();
		management.setContentId("1");
		management.setCorrId("2");
		management.setDatetime(new Date());
		Error error = new Error();
		error.setErrorcode("Hello");
		error.setErrorinfo("World");
		management.getConfirmationOrError().add(error);
		
//		resultEndpoint.expectedMessageCount(1);
		try {
			producer.sendBody("direct:start", ExchangePattern.InOnly, management);
			fail();
		} catch (CamelExecutionException e) {
			ShsManagement management2 = e.getExchange().getIn().getBody(ShsManagement.class);
			assertNotNull(management2);
		}
		
		try {
			producer.sendBody("direct:start", ExchangePattern.InOut, management);
			fail();
		} catch (CamelExecutionException e) {
			ShsManagement management2 = e.getExchange().getIn().getBody(ShsManagement.class);
			assertNotNull(management2);
		}        
        
    }
	
	@DirtiesContext
	@Test
	public void testShouldNotThrowException() throws Exception {
		ShsManagement management = new ShsManagement();
		management.setContentId("1");
		management.setCorrId("2");
		management.setDatetime(new Date());
		Confirmation confirm = new Confirmation();
		
		management.getConfirmationOrError().add(confirm);
	
		MockEndpoint.expectsMessageCount(1, resultEndpoint);
		
		producer.sendBody("direct:start", ExchangePattern.InOnly, management);
		
		resultEndpoint.assertIsSatisfied();
		
		Object result = producer.sendBody("direct:start", ExchangePattern.InOut, management);
		assertNotNull(result, "Response expected");
		
		
		result = producer.sendBody("direct:start", ExchangePattern.InOut, "Body was here");
		assertNotNull(result, "Response expected");
		assertEquals(result, "Body was here");
		
    }

	@DirtiesContext
	@Test
	public void testExceptionHandler() throws Exception {
		ShsManagement management = new ShsManagement();
		management.setContentId("1");
		management.setCorrId("2");
		management.setDatetime(new Date());
		Error error = new Error();
		error.setErrorcode("Hello");
		error.setErrorinfo("World");
		management.getConfirmationOrError().add(error);
	
		Object result = producer.requestBody("direct:startOnException", management);
		assertEquals(result, "Error happened on " + management.getCorrId());

    }
}
