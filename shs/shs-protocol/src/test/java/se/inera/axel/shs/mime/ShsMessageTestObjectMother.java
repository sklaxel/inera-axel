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
package se.inera.axel.shs.mime;


import se.inera.axel.shs.xml.label.*;

import javax.activation.DataHandler;
import java.util.Date;

public class ShsMessageTestObjectMother {

	static public final String DEFAULT_TEST_BODY = "Message body";
	static public final String DEFAULT_TEST_TXID = "cee13995-88b8-4f3f-af2a-e5347b05c4c2";
	static public final String DEFAULT_TEST_PRODUCT_ID = "cee13995-88b8-4f3f-af2a-e5347b05c4c3";
	static public final String DEFAULT_TEST_CONTENT_ID = "cee13995-88b8-4f3f-af2a-e5347b05c4c1";
	static public final String DEFAULT_TEST_FROM = "02020202";
	static public final String DEFAULT_TEST_TO = "02020202";
	static public final String DEFAULT_TEST_SUBJECT = "Subject";
	static public final String DEFAULT_TEST_DATAPART_TYPE = "txt";
	static public final String DEFAULT_TEST_DATAPART_CONTENTTYPE = "text/plain";
	static public final String DEFAULT_TEST_DATAPART_FILENAME = "testfile.txt";
	static public final String DEFAULT_TEST_DATAPART_TRANSFERENCODING = "binary";


	public static ShsMessage createTestMessage() {
		ShsMessage testShsMessage = new ShsMessage();

		ShsLabel shsLabel = new ShsLabel();
		shsLabel.setSubject(DEFAULT_TEST_SUBJECT);
		To to = new To(); to.setValue(DEFAULT_TEST_TO);
		shsLabel.setTo(to);
		From from = new From(); from.setValue(DEFAULT_TEST_FROM);
		shsLabel.getOriginatorOrFrom().add(from);
		Product product = new Product(); product.setValue(DEFAULT_TEST_PRODUCT_ID);
		shsLabel.setProduct(product);

		Date now = new Date();
		shsLabel.setTxId(DEFAULT_TEST_TXID);
		shsLabel.setCorrId(shsLabel.getTxId());
		shsLabel.setDatetime(now);
		shsLabel.setSequenceType(SequenceType.EVENT);
		shsLabel.setStatus(Status.PRODUCTION);
		shsLabel.setTransferType(TransferType.ASYNCH);

		shsLabel.setMessageType(MessageType.SIMPLE);

		Meta meta = new Meta();
		meta.setName("meta1");
		meta.setValue("meta1value");
		shsLabel.getMeta().add(meta);
		testShsMessage.setLabel(shsLabel);

		DataPart dataPart = new DataPart();
		dataPart.setContentType(DEFAULT_TEST_DATAPART_CONTENTTYPE);
		dataPart.setFileName(DEFAULT_TEST_DATAPART_FILENAME);
		dataPart.setContentLength(((Integer)DEFAULT_TEST_BODY.length()).longValue());
		dataPart.setTransferEncoding(DEFAULT_TEST_DATAPART_TRANSFERENCODING);
		dataPart.setDataPartType(DEFAULT_TEST_DATAPART_TYPE);

		dataPart.setDataHandler(new DataHandler(DEFAULT_TEST_BODY, dataPart.getContentType()));

		testShsMessage.addDataPart(dataPart);
		Content content = new Content();
		content.setContentId(DEFAULT_TEST_CONTENT_ID);

		for (DataPart dp : testShsMessage.getDataParts()) {
			Data data = new Data();
			data.setDatapartType(dp.getDataPartType());
			data.setFilename(dp.getFileName());
			if (dp.getContentLength() > 0)
				data.setNoOfBytes("" + dp.getContentLength());
			content.getDataOrCompound().add(data);
		}
		shsLabel.setContent(content);

		return testShsMessage;
	}

}
