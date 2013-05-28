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
package se.inera.axel.test;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.File;


public class STBase {

	protected static final File FILE_PING_REQUEST_OK =
			new File(ClassLoader.getSystemResource("ping-request-ok.xml").getFile());

	protected static final File FILE_PING_REQUEST_NOK =
			new File(ClassLoader.getSystemResource("ping-request-nok.xml").getFile());

	protected static final File FILE_PING_REQUEST_NO_RECEIVER =
			new File(ClassLoader.getSystemResource("ping-request-no-receiver.xml").getFile());

	protected static final File FILE_PING_REQUEST_RIVTA21_RECEIVER =
			new File(ClassLoader.getSystemResource("ping-request-rivta21-receiver.xml").getFile());

	protected static final File FILE_MAKE_BOOKING_REQUEST =
			new File(ClassLoader.getSystemResource("make-booking-request.xml").getFile());

	protected static final String ACTOR_AXEL = "0000000000";
	protected static final String ACTOR_REFERENCE = "1111111111";
	protected static final String ACTOR_UNKNOWN = "0000000001";
	protected static final String PRODUCT_TEST_0 = "00000000-0000-0000-0000-000000000000";
	protected static final String PRODUCT_TEST_1 = "00000000-0000-0000-0000-000000000001";
	protected static final String PRODUCT_TEST_2 = "00000000-0000-0000-0000-000000000002";
	protected static final String PRODUCT_TEST_3 = "00000000-0000-0000-0000-000000000003";
	protected static final String PRODUCT_TEST_4 = "00000000-0000-0000-0000-000000000004";
	protected static final String PRODUCT_TEST_5 = "00000000-0000-0000-0000-000000000005";
	protected File FILE_TEST_OUT;

	protected static final String SOAP_ACTION = "SOAPAction";
	protected static final String SOAP_ACTION_RIV_PING =
			"urn:riv:itintegration:monitoring:PingForConfigurationResponder:1:PingForConfiguration";

	protected static final String SOAP_ACTION_MAKE_BOOKING=
			"urn:riv:crm:scheduling:MakeBookingResponder:1:MakeBooking";
	protected static final String SOAP_ACTION_UNKNOWN =
			"urn:riv:crm:scheduling:MakeBookingResponder:1";

	protected static String HTTP_ENDPOINT_RIV_SHS_BRIDGE = "http://localhost:8089/riv";
	protected static String HTTP_ENDPOINT_SHS_RS ="http://localhost:8585/shs/rs";


	protected ProducerTemplate camel;
	protected CamelContext context;


	@BeforeMethod
	public void setUp() throws Exception {
		System.setProperty("shsServerUrl",HTTP_ENDPOINT_SHS_RS);
		FILE_TEST_OUT = File.createTempFile("axeltest-", ".out");
	}

	@AfterMethod
	public void tearDown() throws Exception {
		FILE_TEST_OUT.delete();
	}

	@BeforeClass
	public void setUpCamel() throws Exception {
		context = new DefaultCamelContext();
		context.start();

		camel = context.createProducerTemplate();
	}

	@AfterClass
	public void tearDownCamel() throws Exception {
		context.stop();
	}

}
