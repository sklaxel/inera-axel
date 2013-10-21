package se.inera.axel.test.fitnesse.junit;

import java.io.File;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import fitnesse.junit.JUnitHelper;
import fitnesse.junit.JUnitXMLTestListener;

public class FitNesseIT {
	private static final Logger log = LoggerFactory.getLogger(FitNesseIT.class);
	
	// NOTE!!! xmlOutputDirName has to be "target/failsafe-reports" in order to signal
	// to Jenkins if the tests pass or fail. Otherwise, a failed FitNesse test would result
	// in Jenkins SUCCESS.
	private static final String jenkinsDirName = "target/failsafe-reports";
	private static final String fitNesseHtmlOutputDirName = "target/fitnesse-reports";

	private JUnitXMLTestListener resultListener;
	private JUnitHelper jUnitHelper;

	@Test
	public void test() throws Exception {
		String suiteName = "FrontPage.AxelTestSuite";
		log.info("Started FitNesse tests on suite: " + suiteName);
		jUnitHelper.assertSuitePasses(suiteName);
	}

	@BeforeClass
	public void setUpCamel() throws Exception {
        // Add slash to java.io.tmpdir to work around Camel bug in DefaultStreamCachingStrategy
        System.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir") + "/");
		File jenkinsOutputDir = new File(jenkinsDirName);
		log.info("FitNesse jenkinsOutputDir: " + jenkinsOutputDir.getAbsolutePath());
		
		File fitNesseHtmlOutputDir = new File(fitNesseHtmlOutputDirName);
		log.info("FitNesse fitNesseHtmlOutputDir: " + fitNesseHtmlOutputDir.getAbsolutePath());

		resultListener = new JUnitXMLTestListener(jenkinsDirName);
		jUnitHelper = new JUnitHelper(".", fitNesseHtmlOutputDirName,
				resultListener);

		ServerSocket socket = new ServerSocket(0);
		jUnitHelper.setPort(socket.getLocalPort());
		socket.close();
	}

	@AfterClass
	public void tearDownCamel() throws Exception {
	}
}
