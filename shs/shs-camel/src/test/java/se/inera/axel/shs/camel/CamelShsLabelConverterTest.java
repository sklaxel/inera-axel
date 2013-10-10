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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.Content;
import se.inera.axel.shs.xml.label.From;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.Status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.testng.Assert.*;
import static se.inera.axel.shs.mime.ShsMessageTestObjectMother.*;

@ContextConfiguration
public class CamelShsLabelConverterTest extends AbstractShsTestNGTests {

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

        Map<String, String> metaMap = new HashMap<String, String>();
        metaMap.put("meta1", "meta1value");
        headers.put(ShsHeaders.META, metaMap);

        template.sendBodyAndHeaders("direct:camelToShsConverter", message, headers);

        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getExchanges();
        Exchange exchange = exchanges.get(0);

        assertNull(exchange.getIn().getHeader(ShsHeaders.FROM));
        assertNull(exchange.getIn().getHeader(ShsHeaders.TO));
        assertNull(exchange.getIn().getHeader(ShsHeaders.SUBJECT));
        assertNull(exchange.getIn().getHeader(ShsHeaders.PRODUCT_ID));
        assertNull(exchange.getIn().getHeader(ShsHeaders.CONTENT_COMMENT));


        ShsLabel label = exchange.getProperty(ShsHeaders.LABEL, ShsLabel.class);
        assertNotNull(label);

        assertEquals(label.getSubject(), DEFAULT_TEST_SUBJECT);
        assertEquals(label.getProduct().getValue(), DEFAULT_TEST_PRODUCT_ID);
        assertEquals(label.getTo().getValue(), DEFAULT_TEST_TO);
        assertNotNull(label.getTxId());
        assertEquals(label.getCorrId(), label.getTxId());
        assertNotNull(label.getDatetime());
        assertNotNull(label.getTransferType());
        assertEquals(label.getStatus(), Status.PRODUCTION);

        List<Object> originatorOrfrom = label.getOriginatorOrFrom();
        assertNotNull(originatorOrfrom);
        assertEquals(originatorOrfrom.size(), 1, "One, and only one, of originator or from expected");
        assertTrue(originatorOrfrom.get(0) instanceof From, "'From' expected");
        From from = (From)originatorOrfrom.get(0);
        assertEquals(from.getValue(), DEFAULT_TEST_FROM);

        assertThat(label.getMeta(), hasSize(1));

        assertEquals(label.getMeta().get(0).getName(), "meta1");
        assertEquals(label.getMeta().get(0).getValue(), "meta1value");

        Content content = label.getContent();
        assertNotNull(content);
        assertNotNull(content.getContentId(), "content id must not be null");
        assertNull(content.getComment(), "content comment should be null");

        assertNotNull(content.getDataOrCompound(), "content/data should not be null");
        assertEquals(content.getDataOrCompound().size(), 0, "content/data should be empty");

    }

    @DirtiesContext
    @Test
    public void testCamelHeadersToLabelWithContentComment() throws Exception {

        resultEndpoint.expectedMessageCount(1);

        String message = DEFAULT_TEST_BODY;

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(ShsHeaders.FROM, DEFAULT_TEST_FROM);
        headers.put(ShsHeaders.TO, DEFAULT_TEST_TO);
        headers.put(ShsHeaders.SUBJECT, DEFAULT_TEST_SUBJECT);
        headers.put(ShsHeaders.PRODUCT_ID, DEFAULT_TEST_PRODUCT_ID);
        headers.put(ShsHeaders.CONTENT_COMMENT, "Comment");

        Map<String, String> metaMap = new HashMap<String, String>();
        metaMap.put("meta1", "meta1value");
        headers.put(ShsHeaders.META, metaMap);

        template.sendBodyAndHeaders("direct:camelToShsConverter", message, headers);

        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getExchanges();
        Exchange exchange = exchanges.get(0);

        assertNull(exchange.getIn().getHeader(ShsHeaders.FROM));
        assertNull(exchange.getIn().getHeader(ShsHeaders.TO));
        assertNull(exchange.getIn().getHeader(ShsHeaders.SUBJECT));
        assertNull(exchange.getIn().getHeader(ShsHeaders.PRODUCT_ID));
        assertNull(exchange.getIn().getHeader(ShsHeaders.CONTENT_COMMENT));


        ShsLabel label = exchange.getProperty(ShsHeaders.LABEL, ShsLabel.class);
        assertNotNull(label);

        Content content = label.getContent();
        assertNotNull(content);
        assertNotNull(content.getContentId(), "content id must not be null");
        assertNotNull(content.getComment(), "content comment should not be null");

        assertNotNull(content.getDataOrCompound(), "content/data should not be null");
        assertEquals(content.getDataOrCompound().size(), 0, "content/data should be empty");

    }

}
