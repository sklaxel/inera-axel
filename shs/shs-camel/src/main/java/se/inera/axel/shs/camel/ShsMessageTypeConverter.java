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

import org.apache.camel.Converter;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.processor.SharedDeferredStream;
import se.inera.axel.shs.processor.ShsMessageMarshaller;
import se.inera.axel.shs.mime.ShsMessage;

import javax.mail.util.SharedByteArrayInputStream;
import javax.mail.util.SharedFileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

@Converter
public class ShsMessageTypeConverter {
	static final Logger log = LoggerFactory.getLogger(ShsMessageMarshaller.class);

	static final ShsMessageMarshaller marshaller = new ShsMessageMarshaller();


	@Converter
	public static InputStream shsMessageToInputStream(ShsMessage message) throws Exception {
		return marshaller.marshal(message);
	}

	@Converter
	public static byte[] shsMessageToByteArray(ShsMessage message) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			marshaller.marshal(message, outputStream);
		} finally {
			IOUtils.closeQuietly(outputStream);
		}

		return outputStream.toByteArray();
	}

	@Converter
	public static String shsMessageToString(ShsMessage message) throws Exception {
		return new String(shsMessageToByteArray(message));
	}

	@Converter
	public static RequestEntity shsMessageToRequestEntity(ShsMessage message) throws Exception {

		DeferredFileOutputStream outputStream = SharedDeferredStream.createDeferredOutputStream();

		try {
			marshaller.marshal(message, outputStream);
		} finally {
			IOUtils.closeQuietly(outputStream);
		}

		if (outputStream.isInMemory()) {
			log.info("written to memory");
			return new ByteArrayRequestEntity(outputStream.getData(), "multipart/mixed");
		} else {
			log.info("written to file: " + outputStream.getFile());

            File outputFile = outputStream.getFile();
            if (outputFile != null) {
                outputFile.deleteOnExit();
            }

			return new FileRequestEntity(outputStream.getFile(), "multipart/mixed");
		}
	}

	@Converter
	public static ShsMessage inputStreamToShsMessage(InputStream inputStream) throws Exception {
		try {
			return marshaller.unmarshal(inputStream);
		} catch (Exception e) {
			return null;
		}
	}

	@Converter
	public static ShsMessage fileToShsMessage(File file) throws Exception {
		return inputStreamToShsMessage(new SharedFileInputStream(file));
	}

	@Converter
	public static ShsMessage byteArrayToShsMessage(byte[] byteArray) throws Exception {
		return inputStreamToShsMessage(new SharedByteArrayInputStream(byteArray));
	}

	@Converter
	public static ShsMessage stringToShsMessage(String string) throws Exception {
		return inputStreamToShsMessage(new SharedByteArrayInputStream(string.getBytes()));
	}

}
