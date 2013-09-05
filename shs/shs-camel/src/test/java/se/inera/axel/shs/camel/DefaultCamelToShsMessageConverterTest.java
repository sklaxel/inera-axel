/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.Content;
import se.inera.axel.shs.xml.label.From;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.testng.Assert.*;
import static se.inera.axel.shs.mime.ShsMessageTestObjectMother.*;

@ContextConfiguration
public class DefaultCamelToShsMessageConverterTest extends AbstractShsTestNGTests {
	
	@DirtiesContext
	@Test
	public void testCamelHeadersToLabel() throws Exception {

        resultEndpoint.expectedMessageCount(1);
        
        String message = DEFAULT_TEST_BODY;
        
        Map<String, Object> headers = new HashMap<String, Object>();
		headers.put(ShsHeaders.FROM, DEFAULT_TEST_FROM);
		headers.put(ShsHeaders.TO, DEFAULT_TEST_TO);
		headers.put(ShsHeaders.SUBJECT, DEFAULT_TEST_SUBJECT);
		headers.put(ShsHeaders.PRODUCT_ID, DEFAULT_TEST_PRODUCT_ID);
		headers.put(ShsHeaders.DATAPART_CONTENTTYPE, DEFAULT_TEST_DATAPART_CONTENTTYPE);
		headers.put(ShsHeaders.DATAPART_FILENAME, DEFAULT_TEST_DATAPART_FILENAME);
		headers.put(ShsHeaders.DATAPART_CONTENTLENGTH, message.length());
		headers.put(ShsHeaders.DATAPART_TYPE, DEFAULT_TEST_DATAPART_TYPE);
		Map<String, String> metaMap = new HashMap<String, String>();
		metaMap.put("meta1", "meta1value");
		headers.put(ShsHeaders.META, metaMap);
		
		template.sendBodyAndHeaders("direct:camelToShsConverter", message, headers);
        
        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getExchanges();
        Exchange exchange = exchanges.get(0);
        Message in = exchange.getIn();
        ShsMessage shsMessage = in.getMandatoryBody(ShsMessage.class);
        assertNotNull(shsMessage);
        
        ShsLabel label = shsMessage.getLabel();
        assertEquals(label.getSubject(), DEFAULT_TEST_SUBJECT);
        assertEquals(label.getProduct().getValue(), DEFAULT_TEST_PRODUCT_ID);
        assertEquals( label.getTo().getValue(), DEFAULT_TEST_TO);
        
        List<Object> originatorOrfrom = label.getOriginatorOrFrom();
        assertNotNull(originatorOrfrom);
        assertEquals(originatorOrfrom.size(), 1, "One, and only one, of originator or from expected");
        assertTrue(originatorOrfrom.get(0) instanceof From, "'From' expected");
        From from = (From)originatorOrfrom.get(0);
        assertEquals(from.getValue(), DEFAULT_TEST_FROM);
        
        
        Content content = label.getContent();
        assertNotNull(content);
        assertNotNull(content.getContentId(), "content id must not be null");
        
        assertNotNull(content.getDataOrCompound(), "content/data must not be null");
        assertNotEquals(0, content.getDataOrCompound().size(), "content/data must not be empty");
        
        
        assertThat(label.getMeta(), hasSize(1));
        
        assertEquals(label.getMeta().get(0).getName(), "meta1");
        assertEquals(label.getMeta().get(0).getValue(), "meta1value");
        
    }
	
	@DirtiesContext
	@Test
	public void testXmlStream() throws Exception {

		resultEndpoint.expectedMessageCount(1);

		Resource xmlResource = new ClassPathResource("se/inera/axel/shs/camel/xmlFile.xml");
		
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put(ShsHeaders.FROM, DEFAULT_TEST_FROM);
		headers.put(ShsHeaders.TO, DEFAULT_TEST_TO);
		headers.put(ShsHeaders.SUBJECT, DEFAULT_TEST_SUBJECT);
		headers.put(ShsHeaders.PRODUCT_ID, DEFAULT_TEST_PRODUCT_ID);
		headers.put(ShsHeaders.DATAPART_CONTENTTYPE, "text/xml");
		// TODO test without these headers because it should be able to autodetect via camel headers/bodyif it is a file body and fail if not.
		headers.put(ShsHeaders.DATAPART_FILENAME, xmlResource.getFilename());
		headers.put(ShsHeaders.DATAPART_CONTENTLENGTH, xmlResource.contentLength());
		// TODO make testcase where datapart type is not specified
		headers.put(ShsHeaders.DATAPART_TYPE, "xml");

		template.sendBodyAndHeaders("direct:camelToShsConverter", xmlResource.getInputStream(), headers);
        
        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getExchanges();
        Exchange exchange = exchanges.get(0);
        Message in = exchange.getIn();
        ShsMessage shsMessage = in.getMandatoryBody(ShsMessage.class);
        
        List<DataPart> dataParts = shsMessage.getDataParts();
        Assert.assertNotNull(dataParts);
        Assert.assertEquals(dataParts.size(), 1);
        
        DataPart dataPart = dataParts.get(0);
        Assert.assertEquals(dataPart.getFileName(), xmlResource.getFilename());
        Assert.assertEquals(dataPart.getDataPartType(), "xml");
        Assert.assertEquals((long)dataPart.getContentLength(), xmlResource.contentLength());
        
        Assert.assertEquals(IOUtils.toString(dataPart.getDataHandler().getInputStream()), IOUtils.toString(xmlResource.getInputStream()));
        
        
	}
	
	@DirtiesContext
	@Test
	public void testXmlFile() throws Exception {

		resultEndpoint.expectedMessageCount(1);

		Resource xmlResource = new ClassPathResource("se/inera/axel/shs/camel/xmlFile.xml");
		File xmlFile = xmlResource.getFile();
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put(ShsHeaders.FROM, DEFAULT_TEST_FROM);
		headers.put(ShsHeaders.TO, DEFAULT_TEST_TO);
		headers.put(ShsHeaders.SUBJECT, DEFAULT_TEST_SUBJECT);
		headers.put(ShsHeaders.PRODUCT_ID, DEFAULT_TEST_PRODUCT_ID);
		headers.put(Exchange.CONTENT_TYPE, "text/xml");
		headers.put(Exchange.FILE_NAME_ONLY, xmlFile.getName());
		headers.put(Exchange.CONTENT_LENGTH, xmlFile.length());
		// TODO make testcase where datapart type is not specified
		headers.put(ShsHeaders.DATAPART_TYPE, "xml");

		template.sendBodyAndHeaders("direct:camelToShsConverter", xmlFile, headers);
        
        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getExchanges();
        Exchange exchange = exchanges.get(0);
        Message in = exchange.getIn();
        ShsMessage shsMessage = in.getMandatoryBody(ShsMessage.class);
        
        List<DataPart> dataParts = shsMessage.getDataParts();
        Assert.assertNotNull(dataParts);
        Assert.assertEquals(dataParts.size(), 1);
        
        DataPart dataPart = dataParts.get(0);
        Assert.assertEquals(dataPart.getFileName(), xmlFile.getName());
        Assert.assertEquals(dataPart.getDataPartType(), "xml");
        Assert.assertEquals((long)dataPart.getContentLength(), xmlFile.length());
        
	}
	
	
	@DirtiesContext
	@Test
	public void testShsMessageToCamel() throws Exception {
		
		ShsMessage shsMessage = createTestMessage();
		
		resultEndpoint.expectedMessageCount(1);
		template.sendBody("direct:shsToCamelConverter", shsMessage);
		
		resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getExchanges();
        Exchange exchange = exchanges.get(0);
        Message in = exchange.getIn();
        
        String  responseBody = in.getBody(String.class);
        Assert.assertNotNull(responseBody, "body expected");
        Assert.assertNotNull(exchange.getProperty(ShsHeaders.LABEL), "Header " + ShsHeaders.LABEL + " should not be null");
        Assert.assertNull(exchange.getIn().getHeader(ShsHeaders.FROM), "Header value '" + ShsHeaders.FROM + "' found, should be null");
        Assert.assertEquals(responseBody, DEFAULT_TEST_BODY);
        Assert.assertNull(in.getHeader(ShsHeaders.ENDRECIPIENT));
        
        Assert.assertEquals(in.getHeader(ShsHeaders.DATAPART_TYPE), DEFAULT_TEST_DATAPART_TYPE);
		
	}

	@DirtiesContext
	@Test
	public void testShsMessageToCamelHeaders() throws Exception {
		
		ShsMessage shsMessage = createTestMessage();
		
		resultEndpoint.expectedMessageCount(1);
		template.sendBody("direct:shsToCamelHeadersConverter", shsMessage);
		
		resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getExchanges();
        Exchange exchange = exchanges.get(0);
        Message in = exchange.getIn();
        
        String  responseBody = in.getBody(String.class);
        Assert.assertNotNull(responseBody, "body expected");
        Assert.assertNull(exchange.getProperty(ShsHeaders.LABEL), "Header " + ShsHeaders.LABEL + " should not be null");
        Assert.assertNotNull(exchange.getIn().getHeader(ShsHeaders.FROM), "Header value '" + ShsHeaders.FROM + "' found, should be null");
        Assert.assertEquals(responseBody, DEFAULT_TEST_BODY);
		
        Assert.assertEquals(in.getHeader(ShsHeaders.FROM), DEFAULT_TEST_FROM);
        Assert.assertEquals(in.getHeader(ShsHeaders.TO), DEFAULT_TEST_TO);
        
        Assert.assertEquals(in.getHeader(ShsHeaders.CONTENT_ID), DEFAULT_TEST_CONTENT_ID);
        Assert.assertNull(in.getHeader(ShsHeaders.ENDRECIPIENT));
        
        
        Assert.assertEquals(in.getHeader(ShsHeaders.DATAPART_TYPE), DEFAULT_TEST_DATAPART_TYPE);
        
        @SuppressWarnings("unchecked")
		Map<String, String> metaMap = (Map<String, String>)in.getHeader(ShsHeaders.META);
        assertThat(metaMap.entrySet(), hasSize(1));
		assertEquals(metaMap.get("meta1"), "meta1value");
	}

	
}
