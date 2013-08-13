package se.inera.axel.test.fitnesse.fixtures;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import se.inera.axel.shs.cmdline.ShsCmdline;

public class ShsAsyncCheckMessageInAxel extends ShsBaseTest {
	private String txId;
	private String toAddress;
	private boolean itemExists = false;

	public String FetchListFromAxel() throws Throwable {
		List<String> args = new ArrayList<String>();
		args = addIfNotNull(args, SHS_FETCH);
		args = addIfNotNull(args, "-t", this.toAddress);
		args = addIfNotNull(args, "-l");
		String[] stringArray = args.toArray(new String[args.size()]);

		// Redirect standard output to os
		PrintStream old = System.out;
		File fetchListFile = File.createTempFile("fetchlist-", ".xml");
		FileOutputStream os = new FileOutputStream(fetchListFile);
		PrintStream ps = new PrintStream(os);
		System.setOut(ps);

		ShsCmdline.main(stringArray);

		// Redirect standard output back to System.out
		System.out.flush();
		System.setOut(old);

		Node node = extractNode(this.getTxId(), fetchListFile.getAbsolutePath());
		if (node != null) {
			this.itemExists = true;
		}
		return nodeToString(node);
	}

	public boolean ItemExists() {
		return itemExists ;
	}
	
	public String getTxId() {
		return txId;
	}
	
	public void setTxId(String txId) {
		this.txId = txId;
	}

	public String getToAddress() {
		return toAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

}
