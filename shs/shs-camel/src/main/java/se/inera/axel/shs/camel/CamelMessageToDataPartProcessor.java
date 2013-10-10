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
import org.apache.camel.Processor;
import se.inera.axel.shs.exception.IllegalDatapartContentException;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.processor.InputStreamDataSource;
import se.inera.axel.shs.processor.ShsHeaders;

import javax.activation.DataHandler;
import java.io.File;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CamelMessageToDataPartProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
	
		Message in = exchange.getIn();

        Object body = in.getBody();

        if (body instanceof DataPart) {
    		return;
        }

		DataPart dataPart = new DataPart();

		dataPart.setDataPartType(in.getHeader(ShsHeaders.DATAPART_TYPE, String.class));
        if (dataPart.getDataPartType() == null) {
            throw new IllegalDatapartContentException("Header '" + ShsHeaders.DATAPART_TYPE + "' must be specified");
        }
		
		String contentType = in.getHeader(ShsHeaders.DATAPART_CONTENTTYPE, String.class);
		if (contentType == null) {
			contentType = in.getHeader(Exchange.CONTENT_TYPE, String.class);
		}


        if (contentType != null) {
            if (!contentType.contains("charset")) {
                String charset = in.getHeader(Exchange.CHARSET_NAME, String.class);
                if (charset != null) {
                    contentType += ";charset=" + charset;
                }
            }
        }

		dataPart.setContentType(contentType);

		String fileName = in.getHeader(ShsHeaders.DATAPART_FILENAME, String.class);
		if (fileName == null) 
			fileName = in.getHeader(Exchange.FILE_NAME_ONLY, String.class);

        if (fileName == null) {
            if (contentType != null) {
                Pattern pattern = Pattern.compile(".+;[ ]*name=(.+?)([ ]*;.+)*");
                Matcher matcher = pattern.matcher(contentType);
                if (matcher.matches()) {
                    fileName = matcher.group(1);
                }
            }
        }

        if (fileName == null) {
            if (body instanceof File) {
                fileName = ((File)body).getName();
            }
        }
		dataPart.setFileName(fileName);
		
		Long contentLength = in.getHeader(ShsHeaders.DATAPART_CONTENTLENGTH, Long.class);
		if (contentLength == null) {
			contentLength = in.getHeader(Exchange.CONTENT_LENGTH, Long.class);
		} 
        if (contentLength == null) {
            if (body instanceof File) {
                contentLength = ((File)body).length();
            }
   		}

		if (contentLength == null) {
			contentLength = 0L;
		}


		dataPart.setContentLength(contentLength.longValue());
		dataPart.setDataHandler(
				new DataHandler(
						new InputStreamDataSource(
								in.getMandatoryBody(InputStream.class),
                                dataPart.getContentType(), dataPart.getFileName())));

        String transferEncoding = in.getHeader(ShsHeaders.DATAPART_TRANSFERENCODING, "binary", String.class);

        if ("binary".equalsIgnoreCase(transferEncoding) == false
                && "base64".equalsIgnoreCase(transferEncoding) == false)
        {
            throw new IllegalDatapartContentException("transfer encoding not supported: " + transferEncoding);
        }

		dataPart.setTransferEncoding(transferEncoding);
				
		in.setBody(dataPart);
		in.removeHeaders("ShsDataPart*");
	}

}
