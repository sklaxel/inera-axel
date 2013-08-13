package se.inera.axel.test.fitnesse.fixtures;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import se.inera.axel.shs.cmdline.ShsCmdline;

public class ShsAsyncSendMessage extends ShsBaseTest {
	private String fromAddress;
	private String toAddress;
	private String productId;
	private String inputFile;

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

	public void setExpectedResponseFile(String expectedResponseFile) {
	}

	public String TxId() throws Throwable {
		File inFile = new File(ClassLoader.getSystemResource(this.inputFile)
				.getFile());

		List<String> args = new ArrayList<String>();
		args = addIfNotNull(args, SHS_SEND);
		args = addIfNotNull(args, "-f", this.fromAddress);
		args = addIfNotNull(args, "-t", this.toAddress);
		args = addIfNotNull(args, "-p", this.productId);
		args = addIfNotNull(args, "-in", inFile.getAbsolutePath());
		String[] stringArray = args.toArray(new String[args.size()]);

		// Redirect standard output to baos
		PrintStream old = System.out;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		System.setOut(ps);

		ShsCmdline.main(stringArray);

		// Redirect standard output back to System.out
		System.out.flush();
		System.setOut(old);
		
		// Return the transaction id received
		String s = baos.toString();
		s = s.replaceAll("\n", "");
		return s;
	}
}
