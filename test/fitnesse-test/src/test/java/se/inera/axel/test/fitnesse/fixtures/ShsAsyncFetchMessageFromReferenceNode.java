package se.inera.axel.test.fitnesse.fixtures;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import se.inera.axel.shs.cmdline.ShsCmdline;

public class ShsAsyncFetchMessageFromReferenceNode extends ShsBaseTest {
	private String txId;
	private String toAddress;
	private String inputFile;
    private String deliveryServiceUrl;

	public boolean receivedFileIsCorrect() throws Throwable {
		List<String> args = new ArrayList<String>();
		args = addIfNotNull(args, SHS_FETCH);
		args = addIfNotNull(args, "-t", this.toAddress);
		args = addIfNotNull(args, "-i", this.txId);
		String[] stringArray = args.toArray(new String[args.size()]);

        if (deliveryServiceUrl != null) {
            System.setProperty("shsServerUrlDs", deliveryServiceUrl);
        }

		ShsCmdline.main(stringArray);

		// Verify that the received file is identical to what was sent in before
		File inFile = new File(ClassLoader.getSystemResource(this.inputFile)
				.getFile());
		File outFile = new File("target/shscmdline/" + this.txId + "-0");
		return FileUtils.contentEquals(inFile, outFile);
	}

	public String getTxId() {
		return txId;
	}
	
	public void setTxId(String txId) {
		this.txId = txId;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

    public void setDeliveryServiceUrl(String deliveryServiceUrl) {
        this.deliveryServiceUrl = deliveryServiceUrl;
    }
}
