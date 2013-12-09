package se.inera.axel.test.fitnesse.fixtures;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import se.inera.axel.shs.cmdline.ShsCmdline;
import se.inera.axel.shs.cmdline.ShsHttpException;

public class ShsTester {
	private String messageType;
	private String fromAddress;
	private String toAddress;
	private String productId;
	private String inputFile;
    private String endRecipient;
	private String expectedResponseFile;

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

    public void setEndRecipient(String endRecipient) {
        this.endRecipient = endRecipient;
    }

	public void setExpectedResponseFile(String expectedResponseFile) {
		this.expectedResponseFile = expectedResponseFile;
	}

	private File sendMessage() throws Throwable {
		File outFile = File.createTempFile("fitness-", ".out");

		File inFile = new File(ClassLoader.getSystemResource(
				this.inputFile).getFile());
		
		List<String> args = new ArrayList<String>();
		args = addIfNotNull(args, this.messageType);
		args = addIfNotNull(args, "-f", this.fromAddress);
		args = addIfNotNull(args, "-t", this.toAddress);
		args = addIfNotNull(args, "-p", this.productId);
		args = addIfNotNull(args, "-E", this.endRecipient);
		args = addIfNotNull(args, "-in", inFile.getAbsolutePath());
		args = addIfNotNull(args, "-out", outFile.getAbsolutePath());

		String[] stringArray = args.toArray(new String[args.size()]);
		ShsCmdline.main(stringArray);

		return outFile;
	}

	private List<String> addIfNotNull(List<String> args2, String name,
			String value) {
		if (value != null) {
			args2.add(name);
			args2.add(value);
		}

		return args2;
	}

	private List<String> addIfNotNull(List<String> args2, String s) {
		if (s != null) {
			args2.add(s);
		}

		return args2;
	}

	public boolean responseMatchesFile() throws Throwable {
		File outFile = sendMessage();

		File expectedFile = new File(ClassLoader.getSystemResource(
				this.expectedResponseFile).getFile());

		return FileUtils.contentEquals(outFile, expectedFile);
	}

	public String responseString() throws Throwable {
		File outFile = sendMessage();

		return FileUtils.readFileToString(outFile);
	}

	public String responseException() throws Throwable {
		try {
			sendMessage();
		} catch (ShsHttpException e) {
			return e.getResponseBody();
		} catch (Exception e) {
			return e.getMessage();
		}

		return "ERROR";
	}
}
