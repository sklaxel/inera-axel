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
package se.inera.axel.shs.processor;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.exception.IllegalMessageStructureException;
import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.mime.TransferEncoding;
import se.inera.axel.shs.xml.label.Compound;
import se.inera.axel.shs.xml.label.Content;
import se.inera.axel.shs.xml.label.Data;
import se.inera.axel.shs.xml.label.ShsLabel;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.SharedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

/**
 * Marshals {@link se.inera.axel.shs.mime.ShsMessage} to a stream of SHS mime format.
 *
 */
public class ShsMessageMarshaller {
	Logger log = LoggerFactory.getLogger(ShsMessageMarshaller.class);

	ShsLabelMarshaller shsLabelMarshaller = new ShsLabelMarshaller();

    private Session session;

	public ShsMessageMarshaller() {
		Properties props = new Properties(System.getProperties());

		props.put("mail.mime.encodefilename", "true");
		props.put("mail.mime.decodefilename", "true");
		
		try {
			session = Session.getInstance(props);
		} catch(SecurityException e) {
			session = Session.getDefaultInstance(System.getProperties());
		}
	}


	/**
	 * Marshals the shs message to a byte array buffer or a temporary file.
	 * If the resulting shs message is larger than {@link SharedDeferredStream#DEFAULT_OVERFLOW_TO_DISK_BYTES} bytes a temporary file is used.
	 * <p/>
	 *
	 * The stream returned implements {@link javax.mail.internet.SharedInputStream} so different
	 * readers can position itself at the different mime parts and re-use the same backing input stream.
	 *
	 * @param message An {@link ShsMessage} populated with data.
	 * @return An instance of {@link InputStream} that conforms to {@link SharedInputStream}.
	 * @throws Exception If a marshaling or IO problem occurs.
	 */
	public InputStream marshal(ShsMessage message) throws Exception {

		DeferredFileOutputStream outputStream = SharedDeferredStream.createDeferredOutputStream();

		try {
			marshal(message, outputStream);
		} finally {
			IOUtils.closeQuietly(outputStream);
		}

        return SharedDeferredStream.toSharedInputStream(outputStream);
	}

	public void marshal(ShsMessage shsMessage, OutputStream outputStream) throws Exception {

		MimeMultipart multipart = new MimeMultipart();
		BodyPart bodyPart = new MimeBodyPart();
		
        try {

        	ShsLabel label = shsMessage.getLabel();
            if (label == null) {
                throw new IllegalMessageStructureException("label not found in shs message");
            }
            
            Content content = label.getContent();
            if (content == null) {
                throw new IllegalMessageStructureException("label/content not found in shs label");
            } else {
            	// we will update this according to our data parts below.
            	content.getDataOrCompound().clear();
            }
            

            List<DataPart> dataParts = shsMessage.getDataParts();

            if (dataParts.isEmpty()) {
                throw new IllegalMessageStructureException("dataparts not found in message");
            }

    		for (DataPart dp : dataParts) {
    			Data data = new Data();
    			data.setDatapartType(dp.getDataPartType());
    			data.setFilename(dp.getFileName());
    			if (dp.getContentLength() != null && dp.getContentLength() > 0) 
    				data.setNoOfBytes("" + dp.getContentLength());
    			content.getDataOrCompound().add(data);
    		}
            
    		bodyPart.setContent(shsLabelMarshaller.marshal(label), "text/xml");
    		bodyPart.setHeader("Content-Transfer-Encoding", "binary");

    		multipart.addBodyPart(bodyPart);

    		
    		
    		for (DataPart dataPart : dataParts) {
    		
    			bodyPart = new MimeBodyPart();

    			bodyPart.setDisposition(Part.ATTACHMENT);
    			bodyPart.setFileName(dataPart.getFileName());
    			bodyPart.setDataHandler(dataPart.getDataHandler()); 
    			
    			if (dataPart.getTransferEncoding() != null) {
    				bodyPart.addHeader("Content-Transfer-Encoding", 
    						dataPart.getTransferEncoding().toString().toLowerCase());
    			}
    			multipart.addBodyPart(bodyPart);
    		}
    		
            
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setSubject("SHS-message");
            mimeMessage.addHeader("Content-Transfer-Encoding", "binary");
            
            mimeMessage.setContent(multipart);
			mimeMessage.saveChanges();
    		
    		String ignoreList[] = { "Message-ID" };

			mimeMessage.writeTo(outputStream, ignoreList);


        } catch (Exception e) {
        	if (e instanceof IllegalMessageStructureException) {
        		throw (IllegalMessageStructureException)e;
        	}
            throw new IllegalMessageStructureException(e);
        }

	}
	
	public ShsMessage unmarshal(InputStream stream)	throws Exception {
		
		try {
            stream = SharedDeferredStream.toSharedInputStream(stream);
        	MimeMessage mimeMessage = new MimeMessage(session, stream);
        	Object msgContent = mimeMessage.getContent();
        	
			if (!(msgContent instanceof MimeMultipart)) {
				throw new IllegalMessageStructureException("Expected a multipart mime message, got " + msgContent.getClass());
			}
			
			MimeMultipart multipart = (MimeMultipart) msgContent;
	
			if (multipart.getCount() < 2) {
			    throw new IllegalMessageStructureException("SHS message must containt at least two mime bodyparts");
			}
			
			ShsMessage shsMessage = new ShsMessage();
			
			BodyPart labelPart = multipart.getBodyPart(0);		
			if (!labelPart.isMimeType("text/xml")) {
				throw new IllegalMessageStructureException("First bodypart is not text/xml but " + labelPart.getContentType());
			}
	
			ShsLabel label = shsLabelMarshaller.unmarshal((String) labelPart.getContent());
			
			shsMessage.setLabel(label);
			
			Content content = label.getContent();
            if (content == null) {
            	throw new IllegalMessageStructureException("Label contains no content elements");
            }

            // this reads only as many mime body parts as there are content/data elements in the label
            int i = 1;
			for (Object o : content.getDataOrCompound()) {
				MimeBodyPart dp = (MimeBodyPart) multipart.getBodyPart(i);
				DataHandler dh = dp.getDataHandler();
				DataPart dataPart = new DataPart();
				dataPart.setDataHandler(new DataHandler(new InputStreamDataSource(dh.getDataSource().getInputStream())));
				dataPart.setContentType(dh.getContentType());

				String encoding = dp.getEncoding();
				if (encoding != null) {
					encoding = encoding.toUpperCase();
					dataPart.setTransferEncoding(TransferEncoding.valueOf(encoding));
				}
				
				dataPart.setFileName(dp.getFileName());
				
				if (o instanceof Data) {
					Data data = (Data)o;
					dataPart.setDataPartType(data.getDatapartType());
				} else if (o instanceof Compound) {
					continue;
				}
				shsMessage.addDataPart(dataPart);
				i++;
			}
			
			return shsMessage;
            
		} catch (ShsException e) {
			throw e;
        } catch (Exception e) {
            throw new IllegalMessageStructureException(e);
        }
	}


}
