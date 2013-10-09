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
/**
 * 
 */
package se.inera.axel.shs.broker.rs.internal;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelTestSupport;
import org.testng.annotations.Test;
import se.inera.axel.shs.exception.OtherErrorException;

/**
 * @author Jan Hallonstén, R2M
 *
 */
public class ErrorHandlingTest extends CamelTestSupport {
	public static final class TestException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
	
	@Produce(uri = "direct:shs:synchronBroker")
	ProducerTemplate synchronBrokerTemplate;
	
	@Produce(uri = "direct:shs:broker")
	ProducerTemplate brokerTemplate;
	
	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				from("direct:shs:broker")
				.onException(Exception.class)
					.handled(true)
					.to("log:onException:direct:shs:broker")
                    .log("${exception.stacktrace}")
					.to("mock:error")
					.end()
				.setHeader("TestHeader", constant("testHeaderValue"))
				.to("log:ErrorHandlingTest")
				.to("shs:direct:shs:synchronBroker")
				.to("mock:shs:brokerEnd");
				
				from("direct:shs:synchronBroker")
				.onException(RuntimeException.class)
					.handled(true)
					.transform(constant("Handled by synchronBroker"))
					.end()
				.onException(TestException.class)
					.handled(false)
					.to("log:ErrorHandlingTest")
					.end()
				.setHeader("TestHeader", constant("synchronBroker"))
				.to("log:ErrorHandlingTest")
				.choice()
					.when(body().isEqualTo("ThrowHandledException"))
						.log(LoggingLevel.INFO, "Throwing handled exception")
						.throwException(new RuntimeException("Thrown by synchronBroker"))
					.when(body().isEqualTo("ThrowUnhandledException"))
						.log(LoggingLevel.INFO, "Throwing unhandled exception")
						.throwException(new TestException())
					.otherwise()
						.log(LoggingLevel.INFO, "No exception")
						.to("mock:noException");
				
			}
		};
	}
	
	@Test(enabled=true)
	public void brokerRouteShouldContinueWhenUnhandledExceptionIsThrownFromSynchronBroker() throws InterruptedException {
		MockEndpoint brokerEnd = getMockEndpoint("mock:shs:brokerEnd");
		MockEndpoint error = getMockEndpoint("mock:error");
		brokerEnd.setResultWaitTime(500);
		error.setResultWaitTime(500);

		brokerEnd.setExpectedMessageCount(0);
		
		error.setExpectedMessageCount(1);
		error.expectedBodiesReceived("ThrowUnhandledException");
		
		brokerTemplate.requestBody("ThrowUnhandledException");
		
		brokerEnd.assertIsSatisfied();
		error.assertIsSatisfied();
		
		Exchange errorExchange = error.getExchanges().get(0);
		Exception e = errorExchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
		
		assertIsInstanceOf(OtherErrorException.class, e);
		assertIsInstanceOf(TestException.class, e.getCause());
	}
	
	@Test(enabled=true)
	public void brokerRouteShouldContinueWhenHandledExceptionIsThrownFromSynchronBroker() throws InterruptedException {
		MockEndpoint brokerEnd = getMockEndpoint("mock:shs:brokerEnd");
		brokerEnd.setResultWaitTime(500);

		brokerEnd.setExpectedMessageCount(1);
		brokerEnd.expectedBodiesReceived("Handled by synchronBroker");
		
		brokerTemplate.requestBody("ThrowHandledException");
		
		brokerEnd.assertIsSatisfied();
		
		Exchange brokerExchange = brokerEnd.getExchanges().get(0);
		assertNull(brokerExchange.getException());
		assertNull(brokerExchange.getProperty(Exchange.EXCEPTION_CAUGHT));
	}
	
	@Test(enabled=true)
	public void brokerEndShouldNotHaveHeadersFromBrokerMainRoute() throws InterruptedException {
		MockEndpoint brokerEnd = getMockEndpoint("mock:shs:brokerEnd");
		brokerEnd.setResultWaitTime(500);
        brokerEnd.expectedMessageCount(1);

		brokerTemplate.requestBody("DefaultBody");

        MockEndpoint.assertIsSatisfied(brokerEnd);
		
		Exchange brokerExchange = brokerEnd.getExchanges().get(0);
		assertEquals(brokerExchange.getIn().getHeader("TestHeader"), "testHeaderValue");
		
		if (brokerExchange.hasOut()) {
			assertEquals(brokerExchange.getOut().getHeader("TestHeader"), "testHeaderValue");
		}
	}

}
