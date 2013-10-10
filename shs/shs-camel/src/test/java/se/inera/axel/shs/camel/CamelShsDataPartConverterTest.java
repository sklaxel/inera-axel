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

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;
import se.inera.axel.shs.exception.IllegalDatapartContentException;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.processor.ShsHeaders;

import javax.activation.DataHandler;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;
import static se.inera.axel.shs.mime.ShsMessageTestObjectMother.*;

@ContextConfiguration
public class CamelShsDataPartConverterTest extends AbstractShsTestNGTests {

    @DirtiesContext
    @Test
    public void convertCamelMessageToDataPart() throws Exception {

        resultEndpoint.expectedMessageCount(1);

        String message = DEFAULT_TEST_BODY;

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(ShsHeaders.DATAPART_TRANSFERENCODING, DEFAULT_TEST_DATAPART_TRANSFERENCODING);
        headers.put(ShsHeaders.DATAPART_CONTENTTYPE, DEFAULT_TEST_DATAPART_CONTENTTYPE);
        headers.put(ShsHeaders.DATAPART_FILENAME, DEFAULT_TEST_DATAPART_FILENAME);
        headers.put(ShsHeaders.DATAPART_CONTENTLENGTH, message.length());
        headers.put(ShsHeaders.DATAPART_TYPE, DEFAULT_TEST_DATAPART_TYPE);

        template.sendBodyAndHeaders("direct:camelToShsConverter", message, headers);

        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getExchanges();
        Exchange exchange = exchanges.get(0);
        Message in = exchange.getIn();
        DataPart datapart = in.getMandatoryBody(DataPart.class);
        assertNotNull(datapart);

        assertEquals((long)datapart.getContentLength(), message.length());
        assertEquals(datapart.getContentType(), DEFAULT_TEST_DATAPART_CONTENTTYPE);
        assertEquals(datapart.getDataPartType(), DEFAULT_TEST_DATAPART_TYPE);
        assertEquals(datapart.getFileName(), DEFAULT_TEST_DATAPART_FILENAME);
        assertEquals(datapart.getTransferEncoding(), DEFAULT_TEST_DATAPART_TRANSFERENCODING);
        assertNotNull(datapart.getDataHandler());

        DataHandler dataHandler = datapart.getDataHandler();
        assertEquals(dataHandler.getContentType(), DEFAULT_TEST_DATAPART_CONTENTTYPE);
        assertEquals(dataHandler.getName(), DEFAULT_TEST_DATAPART_FILENAME);
        assertEquals(dataHandler.getContent(), DEFAULT_TEST_BODY);
    }

    @DirtiesContext
    @Test
    public void convertCamelMessageToDataPartWithoutFilename() throws Exception {

        resultEndpoint.expectedMessageCount(1);

        String message = DEFAULT_TEST_BODY;

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(ShsHeaders.DATAPART_TRANSFERENCODING, DEFAULT_TEST_DATAPART_TRANSFERENCODING);
        headers.put(ShsHeaders.DATAPART_CONTENTTYPE, DEFAULT_TEST_DATAPART_CONTENTTYPE);
        headers.put(ShsHeaders.DATAPART_CONTENTLENGTH, message.length());
        headers.put(ShsHeaders.DATAPART_TYPE, DEFAULT_TEST_DATAPART_TYPE);

        template.sendBodyAndHeaders("direct:camelToShsConverter", message, headers);

        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getExchanges();
        Exchange exchange = exchanges.get(0);
        Message in = exchange.getIn();
        DataPart datapart = in.getMandatoryBody(DataPart.class);
        assertNotNull(datapart);

        assertEquals((long)datapart.getContentLength(), message.length());
        assertEquals(datapart.getContentType(), DEFAULT_TEST_DATAPART_CONTENTTYPE);
        assertEquals(datapart.getDataPartType(), DEFAULT_TEST_DATAPART_TYPE);
        assertNull(datapart.getFileName());
        assertEquals(datapart.getTransferEncoding(), DEFAULT_TEST_DATAPART_TRANSFERENCODING);
        assertNotNull(datapart.getDataHandler());

        DataHandler dataHandler = datapart.getDataHandler();
        assertEquals(dataHandler.getContentType(), DEFAULT_TEST_DATAPART_CONTENTTYPE);
        assertNull(dataHandler.getName());
        assertEquals(dataHandler.getContent(), DEFAULT_TEST_BODY);
    }


    @DirtiesContext
    @Test
    public void convertCamelMessageToDataPartWithFilenameInContentType() throws Exception {

        resultEndpoint.expectedMessageCount(1);

        String message = DEFAULT_TEST_BODY;

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(ShsHeaders.DATAPART_TRANSFERENCODING, DEFAULT_TEST_DATAPART_TRANSFERENCODING);
        headers.put(ShsHeaders.DATAPART_CONTENTTYPE, DEFAULT_TEST_DATAPART_CONTENTTYPE
                + ";name=" + DEFAULT_TEST_DATAPART_FILENAME);
        headers.put(ShsHeaders.DATAPART_CONTENTLENGTH, message.length());
        headers.put(ShsHeaders.DATAPART_TYPE, DEFAULT_TEST_DATAPART_TYPE);

        template.sendBodyAndHeaders("direct:camelToShsConverter", message, headers);

        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getExchanges();
        Exchange exchange = exchanges.get(0);
        Message in = exchange.getIn();
        DataPart datapart = in.getMandatoryBody(DataPart.class);
        assertNotNull(datapart);

        assertEquals((long)datapart.getContentLength(), message.length());
        assertEquals(datapart.getContentType(), DEFAULT_TEST_DATAPART_CONTENTTYPE
                + ";name=" + DEFAULT_TEST_DATAPART_FILENAME);
        assertEquals(datapart.getDataPartType(), DEFAULT_TEST_DATAPART_TYPE);
        assertEquals(datapart.getFileName(), DEFAULT_TEST_DATAPART_FILENAME);
        assertEquals(datapart.getTransferEncoding(), DEFAULT_TEST_DATAPART_TRANSFERENCODING);

        assertNotNull(datapart.getDataHandler());
        DataHandler dataHandler = datapart.getDataHandler();
        assertEquals(dataHandler.getContentType(), DEFAULT_TEST_DATAPART_CONTENTTYPE
                + ";name=" + DEFAULT_TEST_DATAPART_FILENAME);
        assertEquals(dataHandler.getName(), DEFAULT_TEST_DATAPART_FILENAME);
        assertEquals(dataHandler.getContent(), DEFAULT_TEST_BODY);

    }


    @DirtiesContext
    @Test
    public void convertCamelMessageToDataPartWithCharsetHeaderAndFilenameInContentType() throws Exception {

        resultEndpoint.expectedMessageCount(1);

        String message = DEFAULT_TEST_BODY;

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(ShsHeaders.DATAPART_TRANSFERENCODING, DEFAULT_TEST_DATAPART_TRANSFERENCODING);
        headers.put(ShsHeaders.DATAPART_CONTENTTYPE, DEFAULT_TEST_DATAPART_CONTENTTYPE
                + ";name=" + DEFAULT_TEST_DATAPART_FILENAME);
        headers.put(ShsHeaders.DATAPART_CONTENTLENGTH, message.length());
        headers.put(ShsHeaders.DATAPART_TYPE, DEFAULT_TEST_DATAPART_TYPE);
        headers.put(Exchange.CHARSET_NAME, "iso-8859-1");

        template.sendBodyAndHeaders("direct:camelToShsConverter", message, headers);

        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getExchanges();
        Exchange exchange = exchanges.get(0);
        Message in = exchange.getIn();
        DataPart datapart = in.getMandatoryBody(DataPart.class);
        assertNotNull(datapart);

        assertEquals((long)datapart.getContentLength(), message.length());
        assertTrue(datapart.getContentType().contains("charset=iso-8859-1"));
        assertTrue(datapart.getContentType().contains("name=" + DEFAULT_TEST_DATAPART_FILENAME));
        assertEquals(datapart.getDataPartType(), DEFAULT_TEST_DATAPART_TYPE);
        assertEquals(datapart.getFileName(), DEFAULT_TEST_DATAPART_FILENAME);
        assertEquals(datapart.getTransferEncoding(), DEFAULT_TEST_DATAPART_TRANSFERENCODING);

        assertNotNull(datapart.getDataHandler());
        DataHandler dataHandler = datapart.getDataHandler();
        assertEquals(dataHandler.getName(), DEFAULT_TEST_DATAPART_FILENAME);
        assertEquals(dataHandler.getContent(), DEFAULT_TEST_BODY);

    }


    @DirtiesContext
    @Test
    public void convertCamelMessageToDataPartWithBase64() throws Exception {

        resultEndpoint.expectedMessageCount(1);

        String message = DEFAULT_TEST_BODY;

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(ShsHeaders.DATAPART_TRANSFERENCODING, "base64");
        headers.put(ShsHeaders.DATAPART_CONTENTTYPE, DEFAULT_TEST_DATAPART_CONTENTTYPE);
        headers.put(ShsHeaders.DATAPART_FILENAME, DEFAULT_TEST_DATAPART_FILENAME);
        headers.put(ShsHeaders.DATAPART_CONTENTLENGTH, message.length());
        headers.put(ShsHeaders.DATAPART_TYPE, DEFAULT_TEST_DATAPART_TYPE);

        template.sendBodyAndHeaders("direct:camelToShsConverter", message, headers);

        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getExchanges();
        Exchange exchange = exchanges.get(0);
        Message in = exchange.getIn();
        DataPart datapart = in.getMandatoryBody(DataPart.class);
        assertNotNull(datapart);

        assertEquals((long)datapart.getContentLength(), message.length());
        assertEquals(datapart.getContentType(), DEFAULT_TEST_DATAPART_CONTENTTYPE);
        assertEquals(datapart.getDataPartType(), DEFAULT_TEST_DATAPART_TYPE);
        assertEquals(datapart.getFileName(), DEFAULT_TEST_DATAPART_FILENAME);
        assertEquals(datapart.getTransferEncoding(), "base64");

        assertNotNull(datapart.getDataHandler());
        DataHandler dataHandler = datapart.getDataHandler();
        assertEquals(dataHandler.getContentType(), DEFAULT_TEST_DATAPART_CONTENTTYPE);
        assertEquals(dataHandler.getName(), DEFAULT_TEST_DATAPART_FILENAME);
        assertEquals(dataHandler.getContent(), DEFAULT_TEST_BODY);
    }

    @DirtiesContext
    @Test(expectedExceptions = IllegalDatapartContentException.class)
    public void convertCamelMessageToDataPartWithNoDataPartTypeShouldThrow() throws Throwable {

        resultEndpoint.expectedMessageCount(1);

        String message = DEFAULT_TEST_BODY;

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(ShsHeaders.DATAPART_TRANSFERENCODING, "base64");
        headers.put(ShsHeaders.DATAPART_CONTENTTYPE, DEFAULT_TEST_DATAPART_CONTENTTYPE);
        headers.put(ShsHeaders.DATAPART_FILENAME, DEFAULT_TEST_DATAPART_FILENAME);
        headers.put(ShsHeaders.DATAPART_CONTENTLENGTH, message.length());

        try {
            template.sendBodyAndHeaders("direct:camelToShsConverter", message, headers);
        } catch (CamelExecutionException e) {
            throw e.getCause();
        }

        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getExchanges();
        Exchange exchange = exchanges.get(0);
        Message in = exchange.getIn();
        DataPart datapart = in.getMandatoryBody(DataPart.class);
        assertNotNull(datapart);

        assertEquals((long)datapart.getContentLength(), message.length());
        assertEquals(datapart.getContentType(), DEFAULT_TEST_DATAPART_CONTENTTYPE);
        assertEquals(datapart.getDataPartType(), DEFAULT_TEST_DATAPART_TYPE);
        assertEquals(datapart.getFileName(), DEFAULT_TEST_DATAPART_FILENAME);
        assertEquals(datapart.getTransferEncoding(), "base64");

        assertNotNull(datapart.getDataHandler());
        DataHandler dataHandler = datapart.getDataHandler();
        assertEquals(dataHandler.getContentType(), DEFAULT_TEST_DATAPART_CONTENTTYPE);
        assertEquals(dataHandler.getName(), DEFAULT_TEST_DATAPART_FILENAME);
        assertEquals(dataHandler.getContent(), DEFAULT_TEST_BODY);
    }


    @DirtiesContext
    @Test
    public void testXmlStream() throws Exception {

        resultEndpoint.expectedMessageCount(1);

        Resource xmlResource = new ClassPathResource("se/inera/axel/shs/camel/xmlFile.xml");

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(ShsHeaders.DATAPART_TRANSFERENCODING, "base64");
        headers.put(ShsHeaders.DATAPART_CONTENTTYPE, "text/xml");
        headers.put(ShsHeaders.DATAPART_FILENAME, xmlResource.getFilename());
        headers.put(ShsHeaders.DATAPART_CONTENTLENGTH, xmlResource.contentLength());
        headers.put(ShsHeaders.DATAPART_TYPE, "xml");

        template.sendBodyAndHeaders("direct:camelToShsConverter", xmlResource.getInputStream(), headers);

        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getExchanges();
        Exchange exchange = exchanges.get(0);
        Message in = exchange.getIn();
        DataPart datapart = in.getMandatoryBody(DataPart.class);
        assertNotNull(datapart);

        Assert.assertEquals(datapart.getFileName(), xmlResource.getFilename());
        Assert.assertEquals(datapart.getDataPartType(), "xml");
        Assert.assertEquals((long)datapart.getContentLength(), xmlResource.contentLength());

        Assert.assertEquals(IOUtils.toString(datapart.getDataHandler().getInputStream()),
                IOUtils.toString(xmlResource.getInputStream()));

    }

    @DirtiesContext
    @Test
    public void testPdfFile() throws Exception {

        resultEndpoint.expectedMessageCount(1);

        Resource pdfResource = new ClassPathResource("se/inera/axel/shs/camel/pdfFile.pdf");
        File pdfFile = pdfResource.getFile();

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(ShsHeaders.DATAPART_TRANSFERENCODING, "base64");
        headers.put(ShsHeaders.DATAPART_CONTENTTYPE, "application/pdf");
        headers.put(ShsHeaders.DATAPART_TYPE, "pdf");

        template.sendBodyAndHeaders("direct:camelToShsConverter", pdfFile, headers);

        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getExchanges();
        Exchange exchange = exchanges.get(0);
        Message in = exchange.getIn();
        DataPart datapart = in.getMandatoryBody(DataPart.class);
        assertNotNull(datapart);

        Assert.assertEquals(datapart.getFileName(), pdfResource.getFilename());
        Assert.assertEquals(datapart.getDataPartType(), "pdf");
        Assert.assertEquals((long)datapart.getContentLength(), pdfResource.contentLength());

        Assert.assertEquals(IOUtils.toString(datapart.getDataHandler().getInputStream()),
                IOUtils.toString(pdfResource.getInputStream()));

    }

    @DirtiesContext
    @Test(expectedExceptions = IllegalDatapartContentException.class)
    public void testPdfWithIncompatibleTransferEncoding() throws Throwable {

        resultEndpoint.expectedMessageCount(1);

        Resource pdfResource = new ClassPathResource("se/inera/axel/shs/camel/pdfFile.pdf");
        File pdfFile = pdfResource.getFile();

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(ShsHeaders.DATAPART_TRANSFERENCODING, "7BIT");
        headers.put(ShsHeaders.DATAPART_CONTENTTYPE, "application/pdf");
        headers.put(ShsHeaders.DATAPART_TYPE, "pdf");

        try {
            template.sendBodyAndHeaders("direct:camelToShsConverter", pdfFile, headers);
        } catch (CamelExecutionException e) {
            throw e.getCause();
        }


    }


}
