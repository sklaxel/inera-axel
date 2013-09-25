package se.inera.axel.test.fitnesse.fixtures;

import org.apache.commons.lang.StringUtils;
import se.inera.axel.shs.cmdline.ShsCmdline;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShsAsyncSendMessage extends ShsBaseTest {
	
	private String fromAddress;
	private String toAddress;
	private String productId;
	private String inputFile;
    private String correlationId;
    private String receiveServiceUrl;
    private String meta;

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

    public String correlationId() {
        if (StringUtils.isBlank(this.correlationId)) {
            generateCorrelationId();
        }
        return this.correlationId;
    }

    private void generateCorrelationId() {
        this.correlationId = UUID.randomUUID().toString();
    }

    public void setReceiveServiceUrl(String receiveServiceUrl) {
        this.receiveServiceUrl = receiveServiceUrl;
    }

	public void setMeta(String meta) {
		this.meta = meta;
	}

	public String txId() throws Throwable {
		File inFile = new File(ClassLoader.getSystemResource(this.inputFile)
				.getFile());

		List<String> args = new ArrayList<String>();
		args = addIfNotNull(args, SHS_SEND);
		args = addIfNotNull(args, "-f", this.fromAddress);
		args = addIfNotNull(args, "-t", this.toAddress);
		args = addIfNotNull(args, "-p", this.productId);
		args = addIfNotNull(args, "-in", inFile.getAbsolutePath());
		args = addIfNotNull(args, "--corrId", this.correlationId);
		if (this.meta != null) {
			args.add("-m" + this.meta);
		}
			
		String[] stringArray = args.toArray(new String[args.size()]);

        if (this.receiveServiceUrl != null) {
            System.setProperty("shsServerUrl", this.receiveServiceUrl);
        }

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
