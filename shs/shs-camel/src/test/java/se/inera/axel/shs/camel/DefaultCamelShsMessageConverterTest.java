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
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.processor.ShsMessageMarshaller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static se.inera.axel.shs.mime.ShsMessageTestObjectMother.*;

@ContextConfiguration
public class DefaultCamelShsMessageConverterTest extends AbstractShsTestNGTests {


    @DirtiesContext
    @Test
    public void convertXmlStreamToShsMessage() throws Exception {

        resultEndpoint.expectedMessageCount(1);

        Resource xmlResource = new ClassPathResource("se/inera/axel/shs/camel/xmlFile.xml");

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(ShsHeaders.FROM, DEFAULT_TEST_FROM);
        headers.put(ShsHeaders.TO, DEFAULT_TEST_TO);
        headers.put(ShsHeaders.SUBJECT, DEFAULT_TEST_SUBJECT);
        headers.put(ShsHeaders.PRODUCT_ID, DEFAULT_TEST_PRODUCT_ID);
        headers.put(ShsHeaders.DATAPART_CONTENTTYPE, "text/xml");
        headers.put(ShsHeaders.DATAPART_FILENAME, "MyXmlFile.xml");
        headers.put(ShsHeaders.DATAPART_CONTENTLENGTH, xmlResource.contentLength());
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
        Assert.assertEquals(dataPart.getFileName(), "MyXmlFile.xml");
        Assert.assertEquals(dataPart.getDataPartType(), "xml");
        Assert.assertEquals((long)dataPart.getContentLength(), xmlResource.contentLength());

        Assert.assertEquals(IOUtils.toString(dataPart.getDataHandler().getInputStream()),
                IOUtils.toString(xmlResource.getInputStream()));


    }

    @DirtiesContext
    @Test
    public void convertXmlFileToShsMessage() throws Exception {

        resultEndpoint.expectedMessageCount(1);

        Resource xmlResource = new ClassPathResource("se/inera/axel/shs/camel/xmlFile.xml");
        File xmlFile = xmlResource.getFile();
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(ShsHeaders.FROM, DEFAULT_TEST_FROM);
        headers.put(ShsHeaders.TO, DEFAULT_TEST_TO);
        headers.put(ShsHeaders.SUBJECT, DEFAULT_TEST_SUBJECT);
        headers.put(ShsHeaders.PRODUCT_ID, DEFAULT_TEST_PRODUCT_ID);
        headers.put(Exchange.CONTENT_TYPE, "text/xml");
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
    public void convertShsMessageToCamelMessage() throws Exception {

        Resource mimeMessage = new ClassPathResource("se/inera/axel/shs/camel/mimeMessage.txt");
        ShsMessageMarshaller marshaller = new ShsMessageMarshaller();
        ShsMessage shsMessage = marshaller.unmarshal(mimeMessage.getInputStream());

        resultEndpoint.expectedMessageCount(1);
        template.sendBody("direct:shsToCamelConverter", shsMessage);

        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getExchanges();
        Exchange exchange = exchanges.get(0);
        Message in = exchange.getIn();

        String  responseBody = in.getBody(String.class);

        Assert.assertNotNull(responseBody, "body expected");
        Assert.assertNotNull(exchange.getProperty(ShsHeaders.LABEL),
                "Header " + ShsHeaders.LABEL + " should not be null");
        Assert.assertNull(exchange.getIn().getHeader(ShsHeaders.FROM),
                "Header value '" + ShsHeaders.FROM + "' found, should be null");

        Assert.assertEquals(responseBody, DEFAULT_TEST_BODY);
        Assert.assertEquals(in.getHeader(ShsHeaders.DATAPART_CONTENTTYPE),
                "text/plain; name=testfile.txt; charset=iso-8859-1");

        Assert.assertEquals(in.getHeader(ShsHeaders.DATAPART_FILENAME), "testfile.txt");
        Assert.assertEquals(in.getHeader(Exchange.CHARSET_NAME), "iso-8859-1");
        // Assert.assertEquals(in.getHeader(ShsHeaders.DATAPART_CONTENTLENGTH), 13);
        Assert.assertEquals(in.getHeader(ShsHeaders.DATAPART_TYPE), "txt");


        // TODO should this be set on the headers instead of leaving the label n the property?
//        Assert.assertEquals(in.getHeader(ShsHeaders.FROM), DEFAULT_TEST_FROM);
//        Assert.assertEquals(in.getHeader(ShsHeaders.TO), DEFAULT_TEST_TO);
//
//        Assert.assertEquals(in.getHeader(ShsHeaders.CONTENT_ID), DEFAULT_TEST_CONTENT_ID);
//        Assert.assertNull(in.getHeader(ShsHeaders.ENDRECIPIENT));
//
//        @SuppressWarnings("unchecked")
//        Map<String, String> metaMap = (Map<String, String>)in.getHeader(ShsHeaders.META);
//        assertThat(metaMap.entrySet(), hasSize(1));
//        assertEquals(metaMap.get("meta1"), "meta1value");

    }

    @DirtiesContext
    @Test
    public void convertShsMessageToCamelMessageWithoutFilename() throws Exception {

        Resource mimeMessage = new ClassPathResource("se/inera/axel/shs/camel/mimeMessageWithoutFilename.txt");
        ShsMessageMarshaller marshaller = new ShsMessageMarshaller();
        ShsMessage shsMessage = marshaller.unmarshal(mimeMessage.getInputStream());

        resultEndpoint.expectedMessageCount(1);
        template.sendBody("direct:shsToCamelConverter", shsMessage);

        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getExchanges();
        Exchange exchange = exchanges.get(0);
        Message in = exchange.getIn();

        String  responseBody = in.getBody(String.class);
        Assert.assertNotNull(responseBody, "body expected");
        Assert.assertNotNull(exchange.getProperty(ShsHeaders.LABEL),
                "Header " + ShsHeaders.LABEL + " should not be null");
        Assert.assertNull(exchange.getIn().getHeader(ShsHeaders.FROM),
                "Header value '" + ShsHeaders.FROM + "' found, should be null");

        Assert.assertEquals(responseBody, DEFAULT_TEST_BODY);
        Assert.assertEquals(in.getHeader(ShsHeaders.DATAPART_CONTENTTYPE),
                "text/plain; charset=UTF-8");

        Assert.assertNull(in.getHeader(ShsHeaders.DATAPART_FILENAME));
        Assert.assertEquals(in.getHeader(Exchange.CHARSET_NAME), "UTF-8");
        // TODO Assert.assertEquals(in.getHeader(ShsHeaders.DATAPART_CONTENTLENGTH), 13);
        Assert.assertEquals(in.getHeader(ShsHeaders.DATAPART_TYPE), "txt");

    }

}
