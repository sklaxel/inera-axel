package se.inera.axel.test.fitnesse.fixtures;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;

public class SystemTestsSetUp {
	private static ProducerTemplate camel;
	private static CamelContext context;

	public SystemTestsSetUp() throws Exception {
		super();

		context = new DefaultCamelContext();
		context.start();

		camel = context.createProducerTemplate();
}

	public void setShsServerUrl(String shsServerUrl) {
		System.setProperty("shsServerUrl", "http:" + shsServerUrl);
	}

	public static ProducerTemplate getCamel() {
		return camel;
	}

	public static CamelContext getContext() {
		return context;
	}
}
