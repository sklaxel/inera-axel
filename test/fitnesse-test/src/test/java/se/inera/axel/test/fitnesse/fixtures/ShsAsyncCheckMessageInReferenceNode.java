package se.inera.axel.test.fitnesse.fixtures;

import org.w3c.dom.Node;
import se.inera.axel.shs.cmdline.ShsCmdline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ShsAsyncCheckMessageInReferenceNode extends ShsBaseTest {
	private String txId;
	private String toAddress;
	private boolean itemExists = false;

	public String FetchList() throws Throwable {
		List<String> args = new ArrayList<String>();
		args = addIfNotNull(args, SHS_FETCH);
		args = addIfNotNull(args, "-t", this.toAddress);
		args = addIfNotNull(args, "-l");
		String[] stringArray = args.toArray(new String[args.size()]);

		// Redirect standard output to os
		PrintStream old = System.out;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		System.setOut(ps);

		ShsCmdline.main(stringArray);

		// Redirect standard output back to System.out
		System.out.flush();
		System.setOut(old);


		Node node = null;
        long startTime = System.currentTimeMillis();
        while ((node = extractNode(this.getTxId(), new ByteArrayInputStream(os.toByteArray()))) == null) {
            if (System.currentTimeMillis() - startTime > 3000) {
                break;
            }
            Thread.sleep(10);
        }
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
