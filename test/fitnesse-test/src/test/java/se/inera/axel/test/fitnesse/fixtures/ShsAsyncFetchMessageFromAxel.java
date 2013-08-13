package se.inera.axel.test.fitnesse.fixtures;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import se.inera.axel.shs.cmdline.ShsCmdline;

public class ShsAsyncFetchMessageFromAxel extends ShsBaseTest {
	private String txId;

	public String FetchMessageFromAxel() throws Throwable {
		List<String> args = new ArrayList<String>();
		args = addIfNotNull(args, SHS_FETCH);
		args = addIfNotNull(args, "-i", this.txId);
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

	public String getTxId() {
		return txId;
	}
	
	public void setTxId(String txId) {
		this.txId = txId;
	}
}
