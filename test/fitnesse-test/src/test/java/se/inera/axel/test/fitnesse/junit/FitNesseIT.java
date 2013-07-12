package se.inera.axel.test.fitnesse.junit;

import java.net.ServerSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fitnesse.junit.JUnitHelper;
import fitnesse.junit.JUnitXMLTestListener;

public class FitNesseIT {

	private static final Logger log = LoggerFactory.getLogger(FitNesseIT.class);

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
		String suiteName = "FrontPage.AxelTestSuite.SystemTests";
		log.info("Started FitNesse tests on suite: " + suiteName);
		jUnitHelper.assertSuitePasses(suiteName);
		log.info("Finished FitNesse tests on suite: " + suiteName);
	}
}
