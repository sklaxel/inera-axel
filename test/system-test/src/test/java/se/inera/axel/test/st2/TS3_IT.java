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
 * TS3 - TS42-K-003 Synchronous request/response with public agreement.
 *
 */
public class TS3_IT extends STBase {


	/**
	 * TS3 - TS42-K-003 Synchronous request/response with public agreement.
	 *
	 * <p/>
	 * Sends {@link #FILE_PING_REQUEST_OK} synchronously with product {@link #PRODUCT_TEST_3}
	 * from {@link #ACTOR_AXEL} to {@link #ACTOR_REFERENCE}.
	 * <p/>
	 * The request should pass public agreement validation and be routed to the destination
	 * where an exception is raised, that states that 'Matilda' cannot invoke a SOAP service call.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testTS3() throws Throwable {

		String[] args = { "request",
				"-f", ACTOR_AXEL,
				"-t", ACTOR_REFERENCE,
				"-p", PRODUCT_TEST_3,
				"-in", FILE_PING_REQUEST_OK.getAbsolutePath(),
				"-out", FILE_TEST_OUT.getAbsolutePath()
		} ;



		try {
			ShsCmdline.main(args);

			String response = FileUtils.readFileToString(FILE_TEST_OUT);

			Assert.assertTrue(response.contains("SOAPFaultException"),
					"Exception should contain 'SOAPFaultException': " + response);


		} catch (ShsHttpException e) {

			Assert.assertFalse(e.getResponseBody().contains("MissingAgreementException"),
					"Exception should not contain 'MissingAgreementException': " + e.getResponseBody());

			throw e;
		}
	}


}