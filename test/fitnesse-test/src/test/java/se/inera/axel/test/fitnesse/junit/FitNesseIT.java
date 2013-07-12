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
	
	private static final String xmlOutputDirName = "target/fitnesse/xml-output";
	private static final String htmlOutputDirName = "target/fitnesse/html-output";

	private JUnitXMLTestListener resultListener;
	private JUnitHelper jUnitHelper;

	@Test
	public void test() throws Exception {
		String suiteName = "FrontPage.AxelTestSuite.SystemTests";
		log.info("Started FitNesse tests on suite: " + suiteName);
		jUnitHelper.assertSuitePasses(suiteName);

		File xmlOutputDir = new File(xmlOutputDirName);
		log.info("FitNesse xmlOutputDir: " + xmlOutputDir.getAbsolutePath());
		
		File htmlOutputDir = new File(htmlOutputDirName);
		log.info("FitNesse htmlOutputDir: " + htmlOutputDir.getAbsolutePath());
	}

	@BeforeClass
	public void setUpCamel() throws Exception {
		resultListener = new JUnitXMLTestListener(xmlOutputDirName);
		jUnitHelper = new JUnitHelper(".", htmlOutputDirName,
				resultListener);

		ServerSocket socket = new ServerSocket(0);
		jUnitHelper.setPort(socket.getLocalPort());
		socket.close();
	}

	@AfterClass
	public void tearDownCamel() throws Exception {
	}
}
