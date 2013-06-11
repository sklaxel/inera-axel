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

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import se.inera.axel.shs.cmdline.ShsCmdline;
import se.inera.axel.shs.cmdline.ShsHttpException;
import se.inera.axel.test.STBase;

/**
 * TS2- TS42-K-002 Synchronous request/response with product (content based) routing.
 *
 */
public class TS2_IT extends STBase {

	/**
	 * TS2- TS42-K-002 Synchronous request/response with product (content based) routing.
	 * <p/>
	 * <p/>
	 * Sends {@link #FILE_PING_REQUEST_OK} synchronously with product {@link #PRODUCT_TEST_2}
	 * from {@link #ACTOR_AXEL} with no specified receiver.
	 * <p/>
	 * The request should be routed to the destination {@link #ACTOR_REFERENCE} (by existence of an agreement)
	 * where an exception is raised, that states that 'Matilda' cannot invoke a SOAP service call.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testTS2() throws Throwable {

		String[] args = {"request",
				"-f", ACTOR_AXEL,
				"-p", PRODUCT_TEST_2,
				"-in", FILE_PING_REQUEST_OK.getAbsolutePath(),
				"-out", FILE_TEST_OUT.getAbsolutePath()
		};


		try {
			ShsCmdline.main(args);

			String response = FileUtils.readFileToString(FILE_TEST_OUT);

			Assert.assertTrue(response.contains("SOAPFaultException"),
					"Exception should contain 'SOAPFaultException': " + response);

		} catch (ShsHttpException e) {

			Assert.assertFalse(e.getResponseBody().contains("UnresolvedReceiver"),
					"Exception should not contain 'UnresolvedReceiver': " + e.getResponseBody());

			throw e;
		}
	}
}