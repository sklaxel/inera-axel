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
 * TS1 - TS42-K-001 Synchronous request/response.
 *
 */
public class TS1_IT extends STBase {


	/**
	 * TS1a - Send a ping request to a remote shs server via the local server.
	 *
	 * <p/>
	 * Sends {@link #FILE_PING_REQUEST_OK} synchronously with product @{link #PRODUCT_TEST_0}
	 * from {@link #ACTOR_AXEL} to {@link #ACTOR_REFERENCE}.
	 *
	 * <p/>
	 * The request should succeed and the response should be a mirror of the request.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testTS1a() throws Throwable {

		String[] args = { "request",
				"-f", ACTOR_AXEL,
				"-t", ACTOR_REFERENCE,
				"-p", PRODUCT_TEST_0,
				"-in", FILE_PING_REQUEST_OK.getAbsolutePath(),
				"-out", FILE_TEST_OUT.getAbsolutePath()
		} ;

		ShsCmdline.main(args);

		Assert.assertTrue(FileUtils.contentEquals(FILE_TEST_OUT, FILE_PING_REQUEST_OK),
				"Response from ping request does not match expected response");


	}


	/**
	 * Send a ping request to local server.
	 *
	 * <p/>
	 * Sends {@link #FILE_PING_REQUEST_OK} synchronously with product @{link #PRODUCT_TEST_0}
	 * from {@link #ACTOR_AXEL} to {@link #ACTOR_AXEL}.
	 *
	 * <p/>
	 * The request should succeed and the response should be the string "PONG!".
	 *
	 * @throws Throwable
	 */
	@Test
	public void testTS1b() throws Throwable {

		String[] args = { "request",
				"-f", ACTOR_AXEL,
				"-t", ACTOR_AXEL,
				"-p", PRODUCT_TEST_0,
				"-in", FILE_PING_REQUEST_OK.getAbsolutePath(),
				"-out", FILE_TEST_OUT.getAbsolutePath()
		};

		ShsCmdline.main(args);

		Assert.assertTrue("PONG!".equals(FileUtils.readFileToString(FILE_TEST_OUT)),
				"Response from ping should contain 'POINT!'");

	}

	/**
	 * Send a message to remote shs server with a product that is unknown to that server.
	 * (i.e. doesn't have an address in the SHS Directory).
	 *
	 * <p/>
	 * Sends {@link #FILE_PING_REQUEST_OK} synchronously with product @{link #PRODUCT_TEST_1}
	 * from {@link #ACTOR_AXEL} to {@link #ACTOR_REFERENCE}.
	 *
	 * <p/>
	 * A 'MissingDeliveryAddress' exception should be raised by the local server when trying to route message.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testTS1c() throws Throwable {

		String[] args = { "request",
				"-f", ACTOR_AXEL,
				"-t", ACTOR_REFERENCE,
				"-p", PRODUCT_TEST_1,
				"-in", FILE_PING_REQUEST_OK.getAbsolutePath(),
				"-out", FILE_TEST_OUT.getAbsolutePath()
		} ;

		try {
			ShsCmdline.main(args);

			Assert.fail("Exception expected from ping request");

		} catch (ShsHttpException e) {
			Assert.assertTrue(e.getResponseBody().contains("MissingDeliveryAddress"),
					"Exception should contain MissingDeliveryAddress: " + e.getResponseBody());
		}

	}


	/**
	 * Send a request to remote shs server with a product that raises an error in the mongo service implementation.
	 *
	 * <p/>
	 * Sends {@link #FILE_PING_REQUEST_OK} synchronously with product @{link #PRODUCT_TEST_2}
	 * from {@link #ACTOR_AXEL} to {@link #ACTOR_REFERENCE}.
	 *
	 * <p/>
	 * A 'SOAPFaultException' exception should be raised by the remote server that returns an shs error with that message.

	 * @throws Throwable
	 */
	@Test
	public void testTS1d() throws Throwable {

		String[] args = { "request",
				"-f", ACTOR_AXEL,
				"-t", ACTOR_REFERENCE,
				"-p", PRODUCT_TEST_2,
				"-in", FILE_PING_REQUEST_OK.getAbsolutePath(),
				"-out", FILE_TEST_OUT.getAbsolutePath()
		} ;


		ShsCmdline.main(args);

		Assert.assertTrue(FileUtils.readFileToString(FILE_TEST_OUT).contains("SOAPFaultException"),
				"Response from ping should contain an shs error with a SOAPFaultException");


	}


	/**
	 * Send a request to remote shs with a product that maps to a faulty delivery http address in the shs directory.
	 *
	 * <p/>
	 * Sends {@link #FILE_PING_REQUEST_OK} synchronously with product @{link #PRODUCT_TEST_4}
	 * from {@link #ACTOR_AXEL} to {@link #ACTOR_REFERENCE}.
	 *
	 * <p/>
	 * An exception should be raised and returned to our client with a message that the request can't be delivered
	 * delivery http address found in the shs directory.

	 * @throws Throwable
	 */
	@Test
	public void testTS1e() throws Throwable {

		String[] args = { "request",
				"-f", ACTOR_AXEL,
				"-t", ACTOR_REFERENCE,
				"-p", PRODUCT_TEST_4,
				"-in", FILE_PING_REQUEST_OK.getAbsolutePath(),
				"-out", FILE_TEST_OUT.getAbsolutePath()
		} ;



		try {
			ShsCmdline.main(args);

			Assert.fail("Exception expected from ping request");

		} catch (ShsHttpException e) {
			Assert.assertTrue(e.getResponseBody().contains("with statusCode: 404"),
					"Exception should contain 'with statusCode: 404': " + e.getResponseBody());
		}


	}


	/**
	 * Send a request to an shs actor that is not found in the shs directory.
	 *
	 * <p/>
	 * Sends {@link #FILE_PING_REQUEST_OK} synchronously with product @{link #PRODUCT_TEST_4}
	 * from {@link #ACTOR_AXEL} to {@link #ACTOR_UNKNOWN}.
	 *
	 * <p/>
	 * An exception should be raised and returned to our client with a message that the receiver is unknown.

	 * @throws Throwable
	 */
	@Test
	public void testTS1f() throws Throwable {

		String[] args = { "request",
				"-f", ACTOR_AXEL,
				"-t", ACTOR_UNKNOWN,
				"-p", PRODUCT_TEST_4,
				"-in", FILE_PING_REQUEST_OK.getAbsolutePath(),
				"-out", FILE_TEST_OUT.getAbsolutePath()
		} ;



		try {
			ShsCmdline.main(args);

			Assert.fail("Exception expected from ping request");

		} catch (ShsHttpException e) {
			Assert.assertTrue(e.getResponseBody().contains("UnknownReceiver"),
					"Exception should contain 'UnknownReceiver': " + e.getResponseBody());
		}


	}



}
