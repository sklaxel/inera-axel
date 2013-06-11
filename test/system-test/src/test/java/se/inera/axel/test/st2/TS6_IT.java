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

import org.apache.camel.component.http.HttpOperationFailedException;
import org.testng.Assert;
import org.testng.annotations.Test;
import se.inera.axel.test.STBase;

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

			Assert.assertTrue(response.contains("PingForConfigurationResponse"),
					"Response should contain a valid ping response 'PingForConfigurationResponse': " + response);
		} catch (Exception e) {

			if (e.getCause() instanceof HttpOperationFailedException) {
				HttpOperationFailedException httpException = (HttpOperationFailedException)e.getCause();
				Assert.fail("Exception is not expected': " + httpException.getResponseBody());
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


		String response;

		try {
			response = camel.requestBodyAndHeader(HTTP_ENDPOINT_RIV_SHS_BRIDGE,
				FILE_PING_REQUEST_NOK, SOAP_ACTION, SOAP_ACTION_RIV_PING, String.class);

			Assert.fail("Request should raise an exception");

		} catch (Exception e) {
			if (e.getCause() instanceof HttpOperationFailedException) {
				HttpOperationFailedException httpException = (HttpOperationFailedException)e.getCause();

				Assert.assertTrue(httpException.getResponseBody().contains("MissingDeliveryAddress"),
						"Exception should contain 'MissingDeliveryAddress': " + httpException.getResponseBody());
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


		String response;

		try {
			response = camel.requestBodyAndHeader(HTTP_ENDPOINT_RIV_SHS_BRIDGE,
					FILE_PING_REQUEST_NO_RECEIVER, SOAP_ACTION, SOAP_ACTION_RIV_PING, String.class);

			Assert.fail("Request should raise an exception");

		} catch (Exception e) {
			if (e.getCause() instanceof HttpOperationFailedException) {
				HttpOperationFailedException httpException = (HttpOperationFailedException)e.getCause();

				Assert.assertTrue(httpException.getResponseBody().contains("Validation failed for"),
						"Exception should contain 'Validation failed for': " + httpException.getResponseBody());
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
	 * Request should be rotued via the RIV/SHS bridge to the local ping service that only supports RIVTA2.0
	 * headers and thus generates a soap fault that should be returned to this client.
	 */
	@Test
	public void testTS6d() throws Throwable {

		try {
			String response = camel.requestBodyAndHeader(HTTP_ENDPOINT_RIV_SHS_BRIDGE,
					FILE_PING_REQUEST_RIVTA21_RECEIVER, SOAP_ACTION, SOAP_ACTION_RIV_PING, String.class);

			Assert.assertTrue(response.contains("No ws-addressing 'To'-address found in message"),
					"Response should should be a soap fault with message" +
							" 'No ws-addressing 'To'-address found in message': " + response);
		} catch (Exception e) {

			if (e.getCause() instanceof HttpOperationFailedException) {
				HttpOperationFailedException httpException = (HttpOperationFailedException)e.getCause();
				Assert.fail("Exception is not expected': " + httpException.getResponseBody());
			}

			throw e;
		}
	}


	/**
	 * TS6e - Send a make booking request via local shs/riv bridge with an unreachable riv endpoint mapping.
	 *
	 * <p/>
	 * Sends {@link #FILE_MAKE_BOOKING_REQUEST} synchronously using SOAPAction {@link #SOAP_ACTION_MAKE_BOOKING}
	 * to RIV/SHS Bridge endpoint {@link #HTTP_ENDPOINT_RIV_SHS_BRIDGE} on server under test.
	 * <p/>
	 * An Http Error 400 should be returned with a message that contains 'OtherError' and 'HTTP operation failed'.
	 *
	 */
	@Test
	public void testTS6e() throws Throwable {


		String response;

		try {
			response = camel.requestBodyAndHeader(HTTP_ENDPOINT_RIV_SHS_BRIDGE,
					FILE_MAKE_BOOKING_REQUEST, SOAP_ACTION, SOAP_ACTION_MAKE_BOOKING, String.class);

			Assert.fail("Request should raise an exception");

		} catch (Exception e) {
			if (e.getCause() instanceof HttpOperationFailedException) {
				HttpOperationFailedException httpException = (HttpOperationFailedException)e.getCause();

				Assert.assertTrue(httpException.getResponseBody().contains("OtherError")
						&& httpException.getResponseBody().contains("HTTP operation failed"),
						"Exception should contain 'UnresolvedReceiver' and 'HTTP operation failed': " + httpException.getResponseBody());
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


		String response;

		try {
			response = camel.requestBodyAndHeader(HTTP_ENDPOINT_RIV_SHS_BRIDGE,
					FILE_MAKE_BOOKING_REQUEST, SOAP_ACTION, SOAP_ACTION_UNKNOWN, String.class);

			Assert.fail("Request should raise an exception");

		} catch (Exception e) {
			if (e.getCause() instanceof HttpOperationFailedException) {
				HttpOperationFailedException httpException = (HttpOperationFailedException)e.getCause();

				Assert.assertTrue(httpException.getResponseBody().contains("No SHS ProductId found for RIV Service"),
						"Exception should contain 'No SHS ProductId found for RIV Service': "
								+ httpException.getResponseBody());
			}
		}
	}

}