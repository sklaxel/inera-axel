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
package se.inera.axel.shs.mime;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.listOf;
import static com.natpryce.makeiteasy.Property.newProperty;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.*;

import java.net.URL;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import com.natpryce.makeiteasy.SameValueDonor;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.mime.TransferEncoding;
import se.inera.axel.shs.xml.label.Content;
import se.inera.axel.shs.xml.label.Data;
import se.inera.axel.shs.xml.label.ShsLabel;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;

public class ShsMessageMaker {
	static final String DEFAULT_TEST_DATAPART_TYPE = "txt";
	static final String DEFAULT_TEST_DATAPART_CONTENTTYPE = "text/plain";
	static final String DEFAULT_TEST_DATAPART_FILENAME = "testfile.txt";
	static final TransferEncoding DEFAULT_TEST_DATAPART_TRANSFERENCODING = TransferEncoding.BINARY;
	
	public static class ShsMessageInstantiator implements Instantiator<se.inera.axel.shs.mime.ShsMessage> {
		public static final Property<ShsMessage, ShsLabel> label = newProperty();
		public static final Property<ShsMessage, List<se.inera.axel.shs.mime.DataPart>> dataParts = newProperty();
		
		@Override
		public ShsMessage instantiate(
				PropertyLookup<ShsMessage> lookup) {
			ShsMessage message = new ShsMessage();
			message.setLabel(lookup.valueOf(label, a(ShsLabel)));

            List<DataPart> dataPartsValue = lookup.valueOf(dataParts, listOf(a(DataPart)));
            if (dataPartsValue != null) {
                for (DataPart dataPart : dataPartsValue) {
                    addDataPart(message, dataPart);
                }
            }

            return message;
		}
	}

    private static void addDataPart(ShsMessage message, DataPart dataPart) {
        message.addDataPart(dataPart);
        Content content = message.getLabel().getContent();
        if (content != null) {
            se.inera.axel.shs.xml.label.Data data = new Data();
            data.setDatapartType(dataPart.getDataPartType());
            data.setFilename(dataPart.getFileName());

            if (dataPart.getContentLength() > 0)
                data.setNoOfBytes("" + dataPart.getContentLength());
            content.getDataOrCompound().add(data);
        }
    }
	
	public static final ShsMessageInstantiator ShsMessage = new ShsMessageInstantiator();

	public static class DataPartInstantiator implements Instantiator<DataPart> {
		public static final Property<DataPart, Long> contentLength = newProperty();
		public static final Property<DataPart, String> contentType = newProperty();
		public static final Property<DataPart, DataHandler> dataHandler = newProperty();
		public static final Property<DataPart, String> dataPartType = newProperty();
		public static final Property<DataPart, String> fileName = newProperty();
		public static final Property<DataPart, TransferEncoding> transferEncoding = newProperty();
		
		@Override
		public DataPart instantiate(
				PropertyLookup<DataPart> lookup) {
			DataPart dataPart = new DataPart();
			dataPart.setContentLength(lookup.valueOf(contentLength, (long) DEFAULT_TEST_BODY.length()));
			dataPart.setContentType(lookup.valueOf(contentType, DEFAULT_TEST_DATAPART_CONTENTTYPE));
			dataPart.setDataHandler(lookup.valueOf(dataHandler, a(DataHandler)));
			dataPart.setDataPartType(lookup.valueOf(dataPartType, DEFAULT_TEST_DATAPART_TYPE));
			dataPart.setFileName(lookup.valueOf(fileName, DEFAULT_TEST_DATAPART_FILENAME));
			dataPart.setTransferEncoding(lookup.valueOf(transferEncoding, DEFAULT_TEST_DATAPART_TRANSFERENCODING));
            
            return dataPart;
		}
	}
	
	public static final DataPartInstantiator DataPart = new DataPartInstantiator();

    public static class DataHandlerInstantiator implements Instantiator<DataHandler> {
        public static final Property<DataHandler, URL> url = newProperty();
        public static final Property<DataHandler, DataSource> dataSource = newProperty();
        public static final Property<DataHandler, Object> body = newProperty();
        public static final Property<DataHandler, String> contentType = newProperty();


        @Override
        public DataHandler instantiate(
                PropertyLookup<DataHandler> lookup) {
            DataHandler dataHandler = null;

            URL urlValue = lookup.valueOf(url, new SameValueDonor<URL>(null));
            DataSource dataSourceValue = lookup.valueOf(dataSource, new SameValueDonor<DataSource>(null));


            if (urlValue != null) {
                dataHandler = new DataHandler(urlValue);
            } else if (dataSourceValue != null) {
                dataHandler = new DataHandler(dataSourceValue);
            } else {
                dataHandler = new DataHandler(
                        lookup.valueOf(body, DEFAULT_TEST_BODY),
                        lookup.valueOf(contentType, DEFAULT_TEST_DATAPART_CONTENTTYPE));
            }

            return dataHandler;
        }
    }

    public static final DataHandlerInstantiator DataHandler = new DataHandlerInstantiator();

}
