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
package se.inera.axel.shs.camel;

import java.io.InputStream;
import java.util.List;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.camel.Exchange;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.mime.ShsMessageTestObjectMother;
import se.inera.axel.shs.mime.TransferEncoding;
import se.inera.axel.shs.xml.label.ShsLabel;

@ContextConfiguration
public class ShsMessageDataFormatTest extends AbstractShsTestNGTests {
	

	Resource testMimeMessage = new ClassPathResource("se/inera/axel/shs/camel/mimeMessage.txt");
	Resource testPdfFile = new ClassPathResource("se/inera/axel/shs/camel/pdfFile.pdf");
	Resource testJpgFile = new ClassPathResource("se/inera/axel/shs/camel/consultant1.jpg");
	
	ShsMessage testShsMessage = createTestMessage();

	@DirtiesContext
	@Test
	public void testMarshal() throws Exception {
		Assert.assertNotNull(testShsMessage);
		
		resultEndpoint.expectedMessageCount(1);
		template.sendBody("direct:marshal", testShsMessage);
		
		resultEndpoint.assertIsSatisfied();
		List<Exchange> exchanges = resultEndpoint.getReceivedExchanges();
		Exchange exchange = exchanges.get(0);
		
		InputStream mimeStream = exchange.getIn().getBody(InputStream.class);
		
		MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(System.getProperties()), mimeStream);
		String[] mimeSubject = mimeMessage.getHeader("Subject");
		Assert.assertTrue("SHS-message".equalsIgnoreCase(mimeSubject[0]), 
				"Subject is expected to be 'SHS-message' but was " + mimeSubject[0]);
		
		Assert.assertNull(mimeMessage.getMessageID());
		
		MimeMultipart multipart = (MimeMultipart)mimeMessage.getContent();
		Assert.assertEquals(multipart.getCount(), 2);
		
		BodyPart bodyPart = multipart.getBodyPart(1);
		String content = (String)bodyPart.getContent();
		Assert.assertEquals(content, ShsMessageTestObjectMother.DEFAULT_TEST_BODY);
		
		String contentType = bodyPart.getContentType();
		Assert.assertTrue(StringUtils.contains(contentType, ShsMessageTestObjectMother.DEFAULT_TEST_DATAPART_CONTENTTYPE), "Content type error");

		String encodings[] = bodyPart.getHeader("Content-Transfer-Encoding");
		Assert.assertNotNull(encodings);
		Assert.assertEquals(encodings.length, 1);
		Assert.assertEquals(encodings[0].toUpperCase(), ShsMessageTestObjectMother.DEFAULT_TEST_DATAPART_TRANSFERENCODING.toString().toUpperCase());
						
		mimeMessage.writeTo(System.out);
	}
	
	@DirtiesContext
	@Test
	public void testUnmarshal() throws Exception {
		Assert.assertNotNull(testMimeMessage);
		
		resultEndpoint.expectedMessageCount(1);
		template.sendBody("direct:unmarshal", testMimeMessage.getInputStream());
		
		resultEndpoint.assertIsSatisfied();
		List<Exchange> exchanges = resultEndpoint.getReceivedExchanges();
		Exchange exchange = exchanges.get(0);
		
		ShsMessage shsMessage = exchange.getIn().getBody(ShsMessage.class);
		ShsLabel label = shsMessage.getLabel();
		
		Assert.assertNotNull(label, "label should not be null");		
		Assert.assertEquals(label.getSubject(), "Subject");
	}



	@DirtiesContext
	@Test
	public void testUnmarshalRoundtrip() throws Exception {
		Assert.assertNotNull(testMimeMessage);

		resultEndpoint.expectedMessageCount(1);
		template.sendBody("direct:unmarshalRoundtrip", testMimeMessage.getInputStream());

		resultEndpoint.assertIsSatisfied();
		List<Exchange> exchanges = resultEndpoint.getReceivedExchanges();
		Exchange exchange = exchanges.get(0);

		String mime = exchange.getIn().getBody(String.class);

		Assert.assertNotNull(mime, "mime should not be null");
		Assert.assertTrue(mime.contains("Content-Type: multipart/mixed"), "The result does not seem to be a mime message");
	}


	@DirtiesContext
	@Test
	public void testMarshalRoundtrip() throws Exception {
		Assert.assertNotNull(testShsMessage);
		
		resultEndpoint.expectedMessageCount(1);
		template.sendBody("direct:marshalRoundtrip", testShsMessage);
		
		resultEndpoint.assertIsSatisfied();
		List<Exchange> exchanges = resultEndpoint.getReceivedExchanges();
		Exchange exchange = exchanges.get(0);
				
		ShsMessage shsMessage = exchange.getIn().getMandatoryBody(ShsMessage.class);
		
		Assert.assertNotSame(shsMessage, testShsMessage);
		
		ShsLabel label = shsMessage.getLabel();
		
		Assert.assertNotNull(label, "label should not be null");
		
		// TODO add more comparisons
		Assert.assertEquals(label.getSubject(), testShsMessage.getLabel().getSubject());
		Assert.assertEquals(label.getDatetime().toString(), testShsMessage.getLabel().getDatetime().toString());
	}
	
	@DirtiesContext
	@Test
	public void testMarshalPdf() throws Exception {
		Assert.assertNotNull(testShsMessage);
		Assert.assertNotNull(testPdfFile);		
		
		testShsMessage.getDataParts().remove(0);
		DataPart dataPart = new DataPart(new DataHandler(new ByteArrayDataSource(testPdfFile.getInputStream(), "application/xml")));
		dataPart.setContentType("application/xml");
		dataPart.setFileName(testPdfFile.getFilename());					
		dataPart.setTransferEncoding(TransferEncoding.BASE64);
		dataPart.setDataPartType("pdf");
		
		testShsMessage.getDataParts().add(dataPart);
		
		resultEndpoint.expectedMessageCount(1);
		template.sendBody("direct:marshalRoundtrip", testShsMessage);
		
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
		
		Assert.assertTrue(isSame(testPdfFile.getInputStream(), dataPartResponse.getDataHandler().getInputStream()), "Response data stream is not same as source data stream");
	}
	
	@DirtiesContext
	@Test
	public void testMarshalJpg() throws Exception {
		Assert.assertNotNull(testShsMessage);
		Assert.assertNotNull(testJpgFile);		
		
		testShsMessage.getDataParts().remove(0);
		DataPart dataPart = new DataPart(new DataHandler(new ByteArrayDataSource(testJpgFile.getInputStream(), "image/jpeg")));
		dataPart.setContentType("image/jpeg");
		dataPart.setFileName(testJpgFile.getFilename());					
		dataPart.setTransferEncoding(TransferEncoding.BASE64);
		dataPart.setDataPartType("jpg");
		
		testShsMessage.getDataParts().add(dataPart);
		
		resultEndpoint.expectedMessageCount(1);
		template.sendBody("direct:marshalRoundtrip", testShsMessage);
		
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
		
		Assert.assertTrue(isSame(testJpgFile.getInputStream(), dataPartResponse.getDataHandler().getInputStream()), "Response data stream is not same as source data stream");
	}
	

}
