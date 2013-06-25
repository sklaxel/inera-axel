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
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.processor.InputStreamDataSource;
import se.inera.axel.shs.processor.ShsHeaders;

import javax.activation.DataHandler;
import java.io.InputStream;

public class CamelMessageToDataPartProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
	
		Message in = exchange.getIn();
		
		DataPart dataPart = in.getBody(DataPart.class);
		if (dataPart != null)
			return;
		
		
		dataPart = new DataPart();
		dataPart.setDataPartType(in.getHeader(ShsHeaders.DATAPART_TYPE, String.class));
		
		// TODO gissa content type?? eller ta från produktfilen
		String contentType = in.getHeader(ShsHeaders.DATAPART_CONTENTTYPE, String.class);
		if (contentType == null) {
			contentType = in.getHeader(Exchange.CONTENT_TYPE, String.class);
		}
		dataPart.setContentType(contentType); 
		String fileName = in.getHeader(ShsHeaders.DATAPART_FILENAME, String.class);
		if (fileName == null) 
			fileName = in.getHeader(Exchange.FILE_NAME_ONLY, String.class);
		dataPart.setFileName(fileName);
		
		Long contentLength = in.getHeader(ShsHeaders.DATAPART_CONTENTLENGTH, Long.class);
		if (contentLength == null) {
			contentLength = in.getHeader(Exchange.CONTENT_LENGTH, Long.class);
		} 
		
		if (contentLength == null) {
			contentLength = 0L;
		}


		dataPart.setContentLength(contentLength.longValue());
		dataPart.setDataHandler(
				new DataHandler(
						new InputStreamDataSource(
								in.getMandatoryBody(InputStream.class), dataPart.getContentType(), dataPart.getFileName())));
		dataPart.setTransferEncoding(in.getHeader(ShsHeaders.DATAPART_TRANSFERENCODING, "binary", String.class));
				
		in.setBody(dataPart);
		in.removeHeaders("ShsDataPart*");
	}

}
