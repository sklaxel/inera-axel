package se.inera.axel.test.fitnesse.fixtures;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang.StringUtils;

import se.inera.axel.shs.cmdline.ShsCmdline;

public class ShsAsyncSendMessage extends ShsBaseTest {
	
	private String fromAddress;
	private String toAddress;
	private String productId;
    private String correlationId;
    private String receiveServiceUrl;
    private String meta;
    private String subject;
	private File inFile;

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
		this.inFile = new File(ClassLoader.getSystemResource(inputFile)
				.getFile());
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
		args = addIfNotNull(args, "-s", this.subject);
			
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
		String txId = baos.toString();
		txId = txId.replaceAll("\n", "");
		return txId;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setGenerateInputFileWithSize(int generateInputFileSize) throws IOException {
        InputStream inputStream = new NullInputStream(generateInputFileSize);
        
		this.inFile = File.createTempFile("FitNesse_", ".bin");
		FileOutputStream fileOutputStream = new FileOutputStream(inFile.getAbsoluteFile());
		IOUtils.copyLarge(inputStream, fileOutputStream);
	}
	
	public String fileName() {
		return this.inFile.getAbsolutePath();
	}
}
