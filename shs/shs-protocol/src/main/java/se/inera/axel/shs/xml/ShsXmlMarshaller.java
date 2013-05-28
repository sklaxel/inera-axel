/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public abstract class ShsXmlMarshaller<T> {
	protected org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(getClass());
	
	protected JAXBContext jaxbContext;
	
	protected ShsXmlMarshaller() {
		createJaxbContext();
	}

	protected ShsXmlMarshaller(ClassLoader classloader) {
		createJaxbContext(classloader);
	}
	
	protected void createJaxbContext() {
		try {
			this.jaxbContext = JAXBContext.newInstance(getContextPaths());
		} catch (JAXBException e) {
			throw new RuntimeException("Failed to create JAXB context", e);
		}
	}
	
	protected void createJaxbContext(ClassLoader classloader) {
		try {
			this.jaxbContext = JAXBContext.newInstance(getContextPaths(), classloader);
		} catch (JAXBException e) {
			throw new RuntimeException("Failed to create JAXB context", e);
		}
	}
	
	protected abstract String getContextPaths();
	
	protected abstract String getDoctypeHeader();
	
	protected Marshaller createMarshaller() throws JAXBException,
			PropertyException {
		return createMarshaller("ISO-8859-1");
	}

	protected Marshaller createMarshaller(String encoding) throws JAXBException,
			PropertyException {
		if (encoding == null || encoding.isEmpty()) {
			throw new IllegalArgumentException("Encoding must not be empty");
		}
		
		Marshaller marshaller;
		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
		// TODO retrieve formatted output flag
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);
		
		try {
			marshaller.setProperty("com.sun.xml.internal.bind.xmlHeaders",
					getDoctypeHeader());
		} catch (PropertyException pex) {
			marshaller.setProperty("com.sun.xml.bind.xmlHeaders", getDoctypeHeader());
		}
		
		return marshaller;
	}
	
	protected Unmarshaller createUnmarshaller() throws JAXBException {
		Unmarshaller unmarshaller;
		unmarshaller = jaxbContext.createUnmarshaller();
		
		return unmarshaller;
	}

	public T unmarshal(String xml) {
		T result = null;
		InputSource source = new InputSource(
				new StringReader(xml));

		result = unmarshal(result, source);

		return result;
	}

	@SuppressWarnings("unchecked")
	protected T unmarshal(T result, InputSource source) {
		try {
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setEntityResolver(new DtdEntityResolver());
			SAXSource saxSource = new SAXSource(xmlReader, source);
		
			Object object = createUnmarshaller().unmarshal(saxSource);
			
			result = (T)object;
		} catch (SAXException e) {
			throw new XmlException("Failed to unmarshal", e);
		} catch (JAXBException e) {
			throw new XmlException("Failed to unmarshal", e);
		}
		
		return result;
	}
	
	public T unmarshal(InputStream in) throws XmlException {
		T result = null;
		
		InputSource source = new InputSource(in);
		
		result = unmarshal(result, source);
		
		return result;
	}
	
	public String marshal(T object) {
		StringWriter writer = new StringWriter();
		Marshaller marshaller;
		try {
			marshaller = createMarshaller();

			marshaller.marshal(object, new StreamResult(writer));
		} catch (JAXBException e) {
			// TODO handle exception
			throw new RuntimeException(e);
		}
			
		return writer.toString();
	}
	
	public void marshal(T object, OutputStream out) throws XmlException {
		try {
			Marshaller marshaller = createMarshaller();
			marshaller.marshal(object, out);
		} catch (JAXBException e) {
			throw new XmlException("Failed to marshal", e);
		}
	}
	
	public void marshal(T object, OutputStream out, String encoding) throws XmlException {
		try {
			Marshaller marshaller = createMarshaller(encoding);
			marshaller.marshal(object, out);
		} catch (JAXBException e) {
			throw new XmlException("Failed to marshal", e);
		}
	}
}
