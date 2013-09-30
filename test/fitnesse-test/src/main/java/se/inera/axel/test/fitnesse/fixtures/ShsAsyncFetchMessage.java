package se.inera.axel.test.fitnesse.fixtures;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import se.inera.axel.shs.cmdline.ShsCmdline;
import se.inera.axel.shs.processor.ShsLabelMarshaller;
import se.inera.axel.shs.xml.label.Meta;
import se.inera.axel.shs.xml.label.ShsLabel;

public class ShsAsyncFetchMessage extends ShsBaseTest {
	static ShsLabelMarshaller shsLabelMarshaller = new ShsLabelMarshaller();

	private String txId;
	private String toAddress;
	private String inputFile;
    private String deliveryServiceUrl;
    private String productTypeId;
    private String meta;
	private String subject;
    private String charset;
    private boolean fetched = false;

	public boolean receivedFileIsCorrect() throws Throwable {
        fetchMessage();

		// Verify that the received file is identical to what was sent in before
		File inFile = new File(ClassLoader.getSystemResource(this.inputFile)
				.getFile());
		File outFile = new File("target/shscmdline/" + this.txId + "-0");
		
		return FileUtils.contentEquals(inFile, outFile);
	}

    public String datapart() throws Throwable {
        fetchMessage();

        File outFile = new File("target/shscmdline/" + this.txId + "-0");

        return FileUtils.readFileToString(outFile);
    }

    private void fetchMessage() throws Throwable {
        if (fetched) {
            return;
        }

        List<String> args = new ArrayList<String>();
        args = addIfNotNull(args, SHS_FETCH);
        args = addIfNotNull(args, "-t", this.toAddress);
        args = addIfNotNull(args, "-i", this.txId);
        args = addIfNotNull(args, "-p", this.productTypeId);
        String[] stringArray = args.toArray(new String[args.size()]);

        if (deliveryServiceUrl != null) {
            System.setProperty("shsServerUrlDs", deliveryServiceUrl);
        }

        System.out.print(System.getProperty("line.separator") + "arguments: ");
        for (String param : stringArray) {
            System.out.print(param + " ");
        }
        System.out.print(System.getProperty("line.separator"));

        ShsCmdline.main(stringArray);
        InputStream stream = new BufferedInputStream(new FileInputStream("target/shscmdline/" + this.txId + "-label"));
        ShsLabel label = shsLabelMarshaller.unmarshal(stream);

        // Retrieve meta data
        List<Meta> metaList = label.getMeta();

        if (metaList.size() > 0) {
		    Meta item = metaList.get(0);
		    String name = item.getName();
		    String value = item.getValue();
		    this.meta = name + "=" + value;
        }

        // Retrieve subject
        this.subject = label.getSubject();

        fetched = true;
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

    public void setProductTypeId(String productTypeId) {
        this.productTypeId = productTypeId;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
    
    public String meta() {
    	return this.meta;
    }

    public String subject() {
    	return this.subject;
    }
}
