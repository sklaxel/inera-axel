package se.inera.axel.test.fitnesse.fixtures;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class ShsBaseTest {
	protected static final String SHS_SEND = "send";
	protected static final String SHS_FETCH = "fetch";

	protected static List<String> addIfNotNull(List<String> args2, String name,
			String value) {
		if (value != null) {
			args2.add(name);
			args2.add(value);
		}

		return args2;
	}

	protected static List<String> addIfNotNull(List<String> args2, String s) {
		if (s != null) {
			args2.add(s);
		}

		return args2;
	}

	protected static String nodeToString(Node node) throws TransformerException {
		StringWriter buf = new StringWriter();
		Transformer xform = TransformerFactory.newInstance().newTransformer();
		xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		xform.transform(new DOMSource(node), new StreamResult(buf));
		return (buf.toString());
	}

	protected Node extractNode(String txId, String fileName) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		// Ignore DTD location
		EntityResolver resolver = new EntityResolver() {
			public InputSource resolveEntity(String publicId, String systemId) {
				String empty = "";
				ByteArrayInputStream bais = new ByteArrayInputStream(
						empty.getBytes());
				return new InputSource(bais);
			}
		};
		builder.setEntityResolver(resolver);

		Document doc = builder.parse(fileName);
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath
				.compile("/shs.message-list/message[@tx.id='" + txId + "']");

		return (Node) expr.evaluate(doc, XPathConstants.NODE);
	}

}
