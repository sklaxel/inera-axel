package se.inera.axel.test.fitnesse.fixtures;

import java.net.ServerSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fitnesse.junit.JUnitHelper;
import fitnesse.junit.JUnitXMLTestListener;

public class FitNesseIT {

	private JUnitXMLTestListener resultListener;
	private JUnitHelper jUnitHelper;

	@Before
	public void setUp() throws Exception {
		resultListener = new JUnitXMLTestListener("target/failsafe-reports");
		jUnitHelper = new JUnitHelper(".", "target/fitnesse-reports",
				resultListener);

		ServerSocket socket = new ServerSocket(0);
		jUnitHelper.setPort(socket.getLocalPort());
		socket.close();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
//		String suiteName = "FrontPage.AxelTestSuite.EkloTest";
		String suiteName = "FrontPage.AxelTestSuite.SystemTests.SystemTest1";
		jUnitHelper.assertSuitePasses(suiteName);
	}

}
