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
package se.inera.axel.test.st2;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import se.inera.axel.shs.cmdline.ShsCmdline;
import se.inera.axel.shs.cmdline.ShsHttpException;
import se.inera.axel.test.STBase;

/**
 * TS7 - Synchronous request/response from SHS to RIV.
 *
 */
public class TS7_IT extends STBase {


	/**
	 * TS7a - Send a ping request on local server via SHS/RIV bridge.
	 *
	 * <p/>
	 * Sends {@link #FILE_PING_REQUEST_OK} synchronously with product {@link #PRODUCT_TEST_1}
	 * from {@link #ACTOR_AXEL} to {@link #ACTOR_AXEL}
	 * <p/>
	 * The request should be routed to the local ping service that return a valid ping response.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testTS7a() throws Throwable {

		String[] args = { "request",
				"-f", ACTOR_AXEL,
				"-t", ACTOR_AXEL,
				"-p", PRODUCT_TEST_1,
				"-in", FILE_PING_REQUEST_OK.getAbsolutePath(),
				"-out", FILE_TEST_OUT.getAbsolutePath()
		} ;


		ShsCmdline.main(args);

		String out = FileUtils.readFileToString(FILE_TEST_OUT);

		Assert.assertTrue(out.contains("PingForConfigurationResponse"),
				"Response from ping request should contain 'PingForConfigurationResponse': " + out);

	}


	/**
	 * TS7b - Send a ping request with an illegal RIV recipient  on local server via SHS/RIV bridge.
	 *
	 * <p/>
	 * Sends {@link #FILE_PING_REQUEST_NOK} synchronously with product {@link #PRODUCT_TEST_1}
	 * from {@link #ACTOR_AXEL} to {@link #ACTOR_AXEL}
	 * <p/>
	 * The request should be routed to the local ping service that return a soap fault stating that the
	 * recipient specified in the ping request is invalid.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testTS7b() throws Throwable {

		String[] args = { "request",
				"-f", ACTOR_AXEL,
				"-t", ACTOR_AXEL,
				"-p", PRODUCT_TEST_1,
				"-in", FILE_PING_REQUEST_NOK.getAbsolutePath(),
				"-out", FILE_TEST_OUT.getAbsolutePath()
		} ;


		ShsCmdline.main(args);

		String out = FileUtils.readFileToString(FILE_TEST_OUT);

		Assert.assertTrue(out.contains("illegal 'To'-address: 1111111111"),
				"Response from ping request should contain 'illegal 'To'-address: 1111111111': " + out);

	}


	/**
	 * TS7c - Send a ping request without specified RIV receiver on local server via SHS/RIV bridge.
	 *
	 * <p/>
	 * Sends {@link #FILE_PING_REQUEST_NO_RECEIVER} synchronously with product {@link #PRODUCT_TEST_1}
	 * from {@link #ACTOR_AXEL} to {@link #ACTOR_AXEL}
	 * <p/>
	 * The request should be routed to the local ping service that return a soap fault stating that the
	 * recipient is not specified in the ping request.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testTS7c() throws Throwable {

		String[] args = { "request",
				"-f", ACTOR_AXEL,
				"-t", ACTOR_AXEL,
				"-p", PRODUCT_TEST_1,
				"-in", FILE_PING_REQUEST_NO_RECEIVER.getAbsolutePath(),
				"-out", FILE_TEST_OUT.getAbsolutePath()
		} ;


		ShsCmdline.main(args);

		String out = FileUtils.readFileToString(FILE_TEST_OUT);

		Assert.assertTrue(out.contains("No ws-addressing 'To'-address found in message"),
				"Response from ping request should contain 'No ws-addressing 'To'-address found in message': " + out);

	}


	/**
	 * TS7d - Send a ping request with a RIV receiver specified RIV 2.1
	 * header instead of an RIV 2.0 (ws-addressing) header on local server via SHS/RIV bridge.
	 *
	 * <p/>
	 * Sends {@link #FILE_PING_REQUEST_RIVTA21_RECEIVER} synchronously with product {@link #PRODUCT_TEST_1}
	 * from {@link #ACTOR_AXEL} to {@link #ACTOR_AXEL}
	 * <p/>
	 * The request should be routed to the local ping service that return a soap fault stating that the
	 * recipient is not specified in the ping request, since this service only understand RIVTA 2.0 headers.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testTS7d() throws Throwable {

		String[] args = { "request",
				"-f", ACTOR_AXEL,
				"-t", ACTOR_AXEL,
				"-p", PRODUCT_TEST_1,
				"-in", FILE_PING_REQUEST_RIVTA21_RECEIVER.getAbsolutePath(),
				"-out", FILE_TEST_OUT.getAbsolutePath()
		} ;


		ShsCmdline.main(args);

		String out = FileUtils.readFileToString(FILE_TEST_OUT);

		Assert.assertTrue(out.contains("No ws-addressing 'To'-address found in message"),
				"Response from ping request should contain 'No ws-addressing 'To'-address found in message': " + out);

	}


	/**
	 * TS7e - Send a request for a RIV service that is mapped on the local server, but routed to a non existent HTTP.
	 *
	 * <p/>
	 * Sends {@link #FILE_MAKE_BOOKING_REQUEST} synchronously with product {@link #PRODUCT_TEST_5}
	 * from {@link #ACTOR_AXEL} to {@link #ACTOR_AXEL}
	 * <p/>
	 * The request should be routed to the local SHS/RIV bridge, but the service should not be reached and an HTTP
	 * error 400 should be returned with a message that service could not be reached (statuscode 404).
	 *
	 * @throws Throwable
	 */
	@Test
	public void testTS7e() throws Throwable {

		String[] args = { "request",
				"-f", ACTOR_AXEL,
				"-t", ACTOR_AXEL,
				"-p", PRODUCT_TEST_5,
				"-in", FILE_MAKE_BOOKING_REQUEST.getAbsolutePath(),
				"-out", FILE_TEST_OUT.getAbsolutePath()
		} ;


		try {
			ShsCmdline.main(args);

			Assert.fail("Exception should be thrown when trying to call make booking service");
		} catch (ShsHttpException e) {

			Assert.assertTrue(e.getResponseBody().contains("with statusCode: 404"),
					"Exception from make booking request should contain 'with statusCode: 404': " + e);
		}


	}



}