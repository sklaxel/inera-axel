package se.inera.axel.test.fitnesse.fixtures;

import org.w3c.dom.Node;
import se.inera.axel.shs.cmdline.ShsCmdline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class CheckThatMessageIsAvailableIn extends ShsBaseTest {
	private String txId;
	private String toAddress;
    private String productTypeId;
	private boolean itemExists = false;
    private String deliveryServiceUrl;
    private String correlationId;

    public CheckThatMessageIsAvailableIn(String deliveryServiceUrl) {
        this.deliveryServiceUrl = deliveryServiceUrl;
    }

	public String fetchList() throws Throwable {
		List<String> args = new ArrayList<String>();
		args = addIfNotNull(args, SHS_FETCH);
		args = addIfNotNull(args, "-t", this.toAddress);
		args = addIfNotNull(args, "-p", this.productTypeId);
		args = addIfNotNull(args, "-l");
        args = addIfNotNull(args, "--corrId", this.correlationId);
		String[] stringArray = args.toArray(new String[args.size()]);

        if (deliveryServiceUrl != null) {
            System.setProperty("shsServerUrlDs", deliveryServiceUrl);
        }

		// Redirect standard output to os
		PrintStream old = System.out;
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		System.setOut(ps);

		ShsCmdline.main(stringArray);

		// Redirect standard output back to System.out
		System.out.flush();
		System.setOut(old);

		Node node = AsynchFetcher.fetch(new AsynchFetcher.Fetcher<Node>() {
            @Override
            public Node fetch() throws Exception {
                return extractNode(getTxId(), new ByteArrayInputStream(os.toByteArray()));
            }
        });

		if (node != null) {
			this.itemExists = true;
		}

		return nodeToString(node);
	}

	public boolean itemExists() {
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

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public void setProductTypeId(String productTypeId) {
        this.productTypeId = productTypeId;

    }
}
