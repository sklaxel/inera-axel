package se.inera.axel.test.fitnesse.fixtures;

import java.io.File;

import org.apache.camel.component.http.HttpOperationFailedException;

public class TestCase6 {
	private String endpointUri;
	private String body;
	private String header;
	private String headerValue;

	public void setEndpointUri(String endpointUri) {
		this.endpointUri = "http://" + endpointUri;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public void setHeadeRivrValue(String headerValue) {
		this.headerValue = headerValue;
	}

	public String responseString() {
		String response = sendMessage();

		return response;
	}

	public String responseException() {
		try {
			sendMessage();
		} catch (Exception e) {
			if (e.getCause() instanceof HttpOperationFailedException) {
				HttpOperationFailedException httpException = (HttpOperationFailedException) e
						.getCause();

				return httpException.getResponseBody();
			} else {
				return e.getMessage();
			}

		}

		return "ERROR";
	}

	private String sendMessage() {
		File inFile = new File(ClassLoader.getSystemResource(this.body)
				.getFile());

		String response = SystemTestsSetUp.getCamel().requestBodyAndHeader(
				this.endpointUri, inFile, this.header, this.headerValue,
				String.class);

		return response;
	}
}
