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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;
import se.inera.axel.shs.processor.InputStreamDataSource;
import se.inera.axel.shs.processor.ShsMessageMarshaller;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.mime.TransferEncoding;
import se.inera.axel.shs.xml.label.ShsLabel;

import javax.activation.DataHandler;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

@ContextConfiguration
public class LargeShsMessageTypeConverterTest extends AbstractShsTestNGTests {

	ShsMessage testShsMessage = createTestMessage();
	

    @DirtiesContext
    @Test
    public void testLargeFileToStream() throws Exception {
        Assert.assertNotNull(testShsMessage);

        InputStream largeInput = new NullInputStream(100000000);

        testShsMessage.getDataParts().remove(0);
        DataPart dataPart = new DataPart(new DataHandler(new InputStreamDataSource(largeInput, "application/x-iso9660-image")));
        dataPart.setContentType("application/x-iso9660-image");
        dataPart.setFileName("largefile");
        dataPart.setTransferEncoding(TransferEncoding.BASE64);
        dataPart.setDataPartType("iso");

        testShsMessage.getDataParts().add(dataPart);

        resultEndpoint.expectedMessageCount(1);
        template.sendBody("direct:convertToStream", testShsMessage);

        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getReceivedExchanges();
        Exchange exchange = exchanges.get(0);

        InputStream result = exchange.getIn().getMandatoryBody(InputStream.class);

        IOUtils.skipFully(result, 100000000);

    }

    @DirtiesContext
    @Test
    public void testLargeFileRoundtrip() throws Exception {
        Assert.assertNotNull(testShsMessage);

        InputStream largeInput = new NullInputStream(100000000);

        testShsMessage.getDataParts().remove(0);
        DataPart dataPart = new DataPart(new DataHandler(new InputStreamDataSource(largeInput, "application/x-iso9660-image")));
        dataPart.setContentType("application/x-iso9660-image");
        dataPart.setFileName("largefile");
        dataPart.setTransferEncoding(TransferEncoding.BASE64);
        dataPart.setDataPartType("iso");

        testShsMessage.getDataParts().add(dataPart);

        resultEndpoint.expectedMessageCount(1);
        template.sendBody("direct:convertRoundtrip", testShsMessage);

        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getReceivedExchanges();
        Exchange exchange = exchanges.get(0);

        ShsMessage shsMessage = exchange.getIn().getMandatoryBody(ShsMessage.class);

        Assert.assertNotSame(shsMessage, testShsMessage);

        ShsLabel label = shsMessage.getLabel();

        Assert.assertNotNull(label, "label should not be null");

        Assert.assertEquals(label.getSubject(), testShsMessage.getLabel().getSubject());
        Assert.assertEquals(label.getDatetime().toString(), testShsMessage.getLabel().getDatetime().toString());

        Assert.assertNotNull(testShsMessage.getDataParts());
        DataPart dataPartResponse = testShsMessage.getDataParts().get(0);
    }

    @DirtiesContext
    @Test
    public void testLargeStreamToShsMessage() throws Exception {
        Assert.assertNotNull(testShsMessage);
        ShsMessageMarshaller marshaller = new ShsMessageMarshaller();
        InputStream largeInput = new NullInputStream(100000000);

        testShsMessage.getDataParts().remove(0);
        DataPart dataPart = new DataPart(new DataHandler(new InputStreamDataSource(largeInput, "application/x-iso9660-image")));
        dataPart.setContentType("application/x-iso9660-image");
        dataPart.setFileName("largefile");
        dataPart.setTransferEncoding(TransferEncoding.BASE64);
        dataPart.setDataPartType("iso");

        testShsMessage.getDataParts().add(dataPart);

        File shsFile = File.createTempFile("axel", "test");
        FileUtils.copyInputStreamToFile(marshaller.marshal(testShsMessage), shsFile);
        resultEndpoint.expectedMessageCount(1);
        template.sendBody("direct:convertFromStream", new BufferedInputStream(new FileInputStream(shsFile)));

        resultEndpoint.assertIsSatisfied();
        List<Exchange> exchanges = resultEndpoint.getReceivedExchanges();
        Exchange exchange = exchanges.get(0);

        ShsMessage shsMessage = exchange.getIn().getMandatoryBody(ShsMessage.class);

        Assert.assertNotSame(shsMessage, testShsMessage);

        ShsLabel label = shsMessage.getLabel();

        Assert.assertNotNull(label, "label should not be null");

        Assert.assertEquals(label.getSubject(), testShsMessage.getLabel().getSubject());
        Assert.assertEquals(label.getDatetime().toString(), testShsMessage.getLabel().getDatetime().toString());

        Assert.assertNotNull(testShsMessage.getDataParts());
        DataPart dataPartResponse = testShsMessage.getDataParts().get(0);
    }

}
