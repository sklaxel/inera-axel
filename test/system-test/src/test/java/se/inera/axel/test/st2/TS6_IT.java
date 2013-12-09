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
package se.inera.axel.test.st2;

import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.builder.xml.XPathBuilder;
import org.apache.camel.component.http.HttpOperationFailedException;
import org.hamcrest.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import se.inera.axel.test.STBase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * TS6 - Synchronous request/response from RIV to SHS using RIV/SHS Bridge
 *
 */
public class TS6_IT extends STBase {

	/**
	 * TS6a - Send a valid ping-request to local riv ping service via local shs server.
	 *
	 * <p/>
	 * Sends {@link #FILE_PING_REQUEST_OK} synchronously using SOAPAction {@link #SOAP_ACTION_RIV_PING}
	 * to RIV/SHS Bridge endpoint {@link #HTTP_ENDPOINT_RIV_SHS_BRIDGE} on server under test.
	 * <p/>
	 * A valid ping response should be returned.
	 *
	 */
	@Test
	public void testTS6a() throws Throwable {

		try {
			String response = camel.requestBodyAndHeader(HTTP_ENDPOINT_RIV_SHS_BRIDGE,
					FILE_PING_REQUEST_OK, SOAP_ACTION, SOAP_ACTION_RIV_PING, String.class);

			assertThat(response, containsString("PingForConfigurationResponse"));
		} catch (Exception e) {

			if (e.getCause() instanceof HttpOperationFailedException) {
				HttpOperationFailedException httpException = (HttpOperationFailedException)e.getCause();
				fail("Exception is not expected': " + httpException.getResponseBody());
			}

			throw e;
		}


	}


	/**
	 * TS6b - Send a valid ping-request via local shs/riv bridge to RIV receiver that does not have this service.
	 *
	 * <p/>
	 * Sends {@link #FILE_PING_REQUEST_NOK} synchronously using SOAPAction {@link #SOAP_ACTION_RIV_PING}
	 * to RIV/SHS Bridge endpoint {@link #HTTP_ENDPOINT_RIV_SHS_BRIDGE} on server under test.
	 * <p/>
	 * An Http Error 400 should be returned with a message that contains 'MissingDeliveryAddress'.
	 *
	 */
	@Test
	public void testTS6b() throws Throwable {
		try {
			camel.requestBodyAndHeader(HTTP_ENDPOINT_RIV_SHS_BRIDGE,
				FILE_PING_REQUEST_NOK, SOAP_ACTION, SOAP_ACTION_RIV_PING, String.class);

			fail("Request should raise an exception");

		} catch (Exception e) {
			if (e.getCause() instanceof HttpOperationFailedException) {
				HttpOperationFailedException httpException = (HttpOperationFailedException)e.getCause();

				assertThat(httpException.getResponseBody(), containsString("MissingDeliveryAddress"));
                assertThat(httpException.getStatusCode(), is(400));
			}
		}

	}

	/**
	 * TS6c - Send a ping-request with an unspecified RIV receiver via local shs/riv bridge.
	 *
	 * <p/>
	 * Sends {@link #FILE_PING_REQUEST_NO_RECEIVER} synchronously using SOAPAction {@link #SOAP_ACTION_RIV_PING}
	 * to RIV/SHS Bridge endpoint {@link #HTTP_ENDPOINT_RIV_SHS_BRIDGE} on server under test.
	 * <p/>
	 * An Http Error 400 should be returned with a message that contains 'UnresolvedReceiver'.
	 *
	 */
	@Test
	public void testTS6c() throws Throwable {
		try {
			camel.requestBodyAndHeader(HTTP_ENDPOINT_RIV_SHS_BRIDGE,
					FILE_PING_REQUEST_NO_RECEIVER, SOAP_ACTION, SOAP_ACTION_RIV_PING, String.class);

			fail("Request should raise an exception");

		} catch (Exception e) {
			if (e.getCause() instanceof HttpOperationFailedException) {
				HttpOperationFailedException httpException = (HttpOperationFailedException)e.getCause();

				assertThat(httpException.getResponseBody(), containsString("Validation failed for"));
                assertThat(httpException.getStatusCode(), is(400));
			}
		}

	}


	/**
	 * TS6d - Send a ping-request with a RIV receiver specified in a RIVTA 2.1 header via local shs server.
	 *
	 * <p/>
	 * Sends {@link #FILE_PING_REQUEST_RIVTA21_RECEIVER} synchronously using SOAPAction {@link #SOAP_ACTION_RIV_PING}
	 * to RIV/SHS Bridge endpoint {@link #HTTP_ENDPOINT_RIV_SHS_BRIDGE} on server under test.
	 * <p/>
	 * Request should be rotued via the RIV/SHS bridge to the local ping service that send back a pring response.
	 */
	@Test
	public void testTS6d() throws Throwable {
        Namespaces nameSpaces = new Namespaces("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        nameSpaces.add("ping", "urn:riv:itintegration:monitoring:PingForConfigurationResponder:1");

        String response = camel.requestBodyAndHeader(HTTP_ENDPOINT_RIV_SHS_BRIDGE,
                FILE_PING_REQUEST_RIVTA21_RECEIVER, SOAP_ACTION, SOAP_ACTION_RIV_PING, String.class);

        assertThat(response, containsString("pingDateTime"));

        assertTrue(XPathBuilder.xpath("/soap:Envelope/soap:Body/ping:PingForConfigurationResponse/ping:pingDateTime")
                .namespaces(nameSpaces)
                .booleanResult()
                .matches(camel.getCamelContext(), response));
	}


	/**
	 * TS6e - Send a make booking request via local shs/riv bridge with an unreachable riv endpoint mapping.
	 *
	 * <p/>
	 * Sends {@link #FILE_MAKE_BOOKING_REQUEST} synchronously using SOAPAction {@link #SOAP_ACTION_MAKE_BOOKING}
	 * to RIV/SHS Bridge endpoint {@link #HTTP_ENDPOINT_RIV_SHS_BRIDGE} on server under test.
	 * <p/>
	 * An Http Error 400 should be returned with a message that contains MissingDeliveryExecutionException.
	 *
	 */
	@Test
	public void testTS6e() throws Throwable {
		try {
			camel.requestBodyAndHeader(HTTP_ENDPOINT_RIV_SHS_BRIDGE,
					FILE_MAKE_BOOKING_REQUEST, SOAP_ACTION, SOAP_ACTION_MAKE_BOOKING, String.class);

			fail("Request should raise an exception");

		} catch (Exception e) {
			if (e.getCause() instanceof HttpOperationFailedException) {
				HttpOperationFailedException httpException = (HttpOperationFailedException)e.getCause();

				assertThat(httpException.getResponseBody(), containsString("MissingDeliveryExecutionException"));
                assertThat(httpException.getStatusCode(), is(400));
            }
		}
	}


	/**
	 * TS6f - Send a make booking request via local shs/riv bridge with an unknown SOAPAction.
	 *
	 * <p/>
	 * Sends {@link #FILE_MAKE_BOOKING_REQUEST} synchronously using SOAPAction {@link #SOAP_ACTION_UNKNOWN}
	 * to RIV/SHS Bridge endpoint {@link #HTTP_ENDPOINT_RIV_SHS_BRIDGE} on server under test.
	 * <p/>
	 * An Http Error 400 should be returned with a message that contains 'No shs product id found for riv service'.
	 *
	 */
	@Test
	public void testTS6f() throws Throwable {
		try {
			camel.requestBodyAndHeader(HTTP_ENDPOINT_RIV_SHS_BRIDGE,
					FILE_MAKE_BOOKING_REQUEST, SOAP_ACTION, SOAP_ACTION_UNKNOWN, String.class);

			fail("Request should raise an exception");

		} catch (Exception e) {
			if (e.getCause() instanceof HttpOperationFailedException) {
				HttpOperationFailedException httpException = (HttpOperationFailedException)e.getCause();

				assertThat(httpException.getResponseBody(), containsString("No SHS ProductId found for RIV Service"));
                assertThat(httpException.getStatusCode(), is(400));
			}
		}
	}

}