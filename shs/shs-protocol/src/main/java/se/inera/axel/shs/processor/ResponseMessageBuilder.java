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
package se.inera.axel.shs.processor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.activation.DataHandler;

import se.inera.axel.shs.exception.OtherErrorException;
import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.Content;
import se.inera.axel.shs.xml.label.EndRecipient;
import se.inera.axel.shs.xml.label.From;
import se.inera.axel.shs.xml.label.MessageType;
import se.inera.axel.shs.xml.label.Originator;
import se.inera.axel.shs.xml.label.Product;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.To;
import se.inera.axel.shs.xml.label.TransferType;
import se.inera.axel.shs.xml.management.ShsManagement;

public class ResponseMessageBuilder {
	ShsManagementMarshaller marshaller = new ShsManagementMarshaller();
	
	public ShsLabel buildErrorLabel(ShsLabel requestLabel) {
		ShsLabel errorLabel = buildReplyLabel(requestLabel);

		Product errorProduct = new Product();
		errorProduct.setvalue("error");
		errorLabel.setProduct(errorProduct);
		errorLabel.setSequenceType(SequenceType.ADM);
		
		// Content content;
		if (errorLabel.getContent() != null
				&& errorLabel.getContent().getContentId() != null) {

			String c = errorLabel.getContent().getContentId();
			c = c.replaceAll("-reply$", "-error");
			errorLabel.getContent().setContentId(c);
		}

		return errorLabel;
	}
	
	public ShsLabel buildConfirmLabel(ShsLabel requestLabel) {
		ShsLabel confirmLabel = buildReplyLabel(requestLabel);

		Product confirmProduct = new Product();
		confirmProduct.setvalue("confirm");
		confirmLabel.setProduct(confirmProduct);
		confirmLabel.setSequenceType(SequenceType.ADM);

		// Content content;
		if (confirmLabel.getContent() != null
				&& confirmLabel.getContent().getContentId() != null) {

			String c = confirmLabel.getContent().getContentId();
			c = c.replaceAll("-reply$", "-confirm");
			confirmLabel.getContent().setContentId(c);
		}

		return confirmLabel;
	}	
	
	public ShsLabel buildReplyLabel(ShsLabel requestLabel) {
		ShsLabel replyLabel = new ShsLabel();
		
		// String version;
		replyLabel.setVersion(requestLabel.getVersion());

		// String txId;
		if (requestLabel.getTransferType() == TransferType.SYNCH)
			replyLabel.setTxId(requestLabel.getTxId());
		else
			replyLabel.setTxId(UUID.randomUUID().toString());

		// String corrId;
		replyLabel.setCorrId(requestLabel.getCorrId());

		// String shsAgreement;
		replyLabel.setShsAgreement(requestLabel.getShsAgreement());

		// TransferType transferType;
		replyLabel.setTransferType(requestLabel.getTransferType());

		// MessageType messageType;
		replyLabel.setMessageType(MessageType.SIMPLE);

		// MessageType documentType;
		replyLabel.setDocumentType(MessageType.SIMPLE);

		// SequenceType sequenceType;
		replyLabel.setSequenceType(SequenceType.REPLY);
		
		// Status status;
		replyLabel.setStatus(requestLabel.getStatus());
		
		// List<Object> originatorOrFrom;
		To requestTo = requestLabel.getTo();
		if (requestTo != null) {
			From newFrom = new From();
			newFrom.setCommonName(requestTo.getCommonName());
			newFrom.setvalue(requestTo.getvalue());
			replyLabel.getOriginatorOrFrom().add(newFrom);
		}
		
		EndRecipient requestEndRecipient = requestLabel.getEndRecipient();
		if (requestEndRecipient != null) {
			Originator newOriginator = new Originator();
			newOriginator.setLabeledURI(requestEndRecipient.getLabeledURI());
			newOriginator.setName(requestEndRecipient.getName());
			newOriginator.setvalue(requestEndRecipient.getvalue());
			replyLabel.getOriginatorOrFrom().add(newOriginator);
		}

		// To to;
		From requestFrom = requestLabel.getFrom();
		if (requestFrom != null) {
			To newTo = new To();
			newTo.setCommonName(requestFrom.getCommonName());
			newTo.setvalue(requestFrom.getvalue());
			replyLabel.setTo(newTo);
		}

		// EndRecipient endRecipient;
		Originator requestOriginator = requestLabel.getOriginator();
		if (requestOriginator != null) {
			EndRecipient newEndRecipient = new EndRecipient();
			newEndRecipient.setLabeledURI(requestOriginator.getLabeledURI());
			newEndRecipient.setName(requestOriginator.getName());
			newEndRecipient.setvalue(requestOriginator.getvalue());
			replyLabel.setEndRecipient(newEndRecipient);
		}

		// Product product;
		// OK because set by calling function

		// List<Meta> meta;
		// OK because should be empty

		// String subject;
		// OK because should be empty

		// Date datetime;
		replyLabel.setDatetime(new Date());

		// Content content;
		Content requestContent = requestLabel.getContent();
		if (requestContent != null) {
			Content newContent = new Content();
			newContent.setComment(requestContent.getComment());
			newContent.setContentId(requestContent.getContentId() + "-reply");
			
			List<Object> requestDataOrCompound = requestContent.getDataOrCompound();
			List<Object> newDataOrCompound = newContent.getDataOrCompound();
			newDataOrCompound.addAll(requestDataOrCompound);
			
			replyLabel.setContent(newContent);
		}
		
		// List<History> history;		
		// OK because should be empty
				
		return replyLabel;
	}
		
	public DataPart buildConfirmDataPart(ShsLabel requestLabel) {
		ShsManagement management = new ShsManagement();
		if (requestLabel.getContent() != null)
			management.setContentId(requestLabel.getContent().getContentId());
		
		management.setCorrId(requestLabel.getCorrId());
		management.setDatetime(new Date());
		
		DataPart confirmDataPart = new DataPart();
		confirmDataPart.setDataPartType("confirm");
		confirmDataPart.setContentType("text/xml");
		confirmDataPart.setFileName("confirm.xml");
		confirmDataPart.setDataHandler(new DataHandler(marshaller.marshal(management), "text/xml"));
		
		return confirmDataPart;
	}
	
	public ShsMessage buildConfirmMessage(ShsMessage requestShsMessage) {
		ShsMessage confirmMessage = new ShsMessage();
		
		ShsLabel requestLabel = requestShsMessage.getLabel();
		ShsLabel confirmLabel = buildConfirmLabel(requestLabel);
		confirmMessage.setLabel(confirmLabel);
		
		DataPart confirmDataPart = buildConfirmDataPart(requestLabel);
		confirmMessage.getDataParts().add(confirmDataPart);
				
		return confirmMessage;
	}

    public ShsMessage buildConfirmMessage(ShsLabel requestLabel) {
        ShsMessage confirmMessage = new ShsMessage();

        ShsLabel confirmLabel = buildConfirmLabel(requestLabel);
        confirmMessage.setLabel(confirmLabel);

        DataPart confirmDataPart = buildConfirmDataPart(requestLabel);
        confirmMessage.getDataParts().add(confirmDataPart);

        return confirmMessage;
    }
	
	public DataPart buildErrorDataPart(ShsLabel requestLabel, Exception exception) {
		// create shs exception and...
		ShsException shsException = null;
		if (exception instanceof ShsException)
			shsException = (ShsException)exception;
		else
			shsException = new OtherErrorException(exception);
		
		ShsManagement management = ShsExceptionConverter.toShsManagement(shsException);
		management.setCorrId(requestLabel.getCorrId());
		management.setContentId(requestLabel.getContent().getContentId());

		DataPart errorDataPart = new DataPart();
		errorDataPart.setDataPartType("error");
		errorDataPart.setContentType("text/xml");
		errorDataPart.setFileName("error.xml");
		errorDataPart.setDataHandler(new DataHandler(marshaller.marshal(management), "text/xml"));
		
		return errorDataPart;
	}
	
	
	public ShsMessage buildErrorMessage(ShsMessage requestShsMessage, Exception exception) {
		
		ShsMessage errorMessage = new ShsMessage();
		
		ShsLabel requestLabel = requestShsMessage.getLabel();
		ShsLabel errorLabel = buildErrorLabel(requestLabel);
		errorMessage.setLabel(errorLabel);
		
		DataPart errorDataPart = buildErrorDataPart(requestLabel, exception);
		errorMessage.getDataParts().add(errorDataPart);
		
		return errorMessage;
	}

    public ShsMessage buildErrorMessage(ShsLabel requestLabel, Exception exception) {

        ShsMessage errorMessage = new ShsMessage();

        ShsLabel errorLabel = buildErrorLabel(requestLabel);
        errorMessage.setLabel(errorLabel);

        DataPart errorDataPart = buildErrorDataPart(requestLabel, exception);
        errorMessage.getDataParts().add(errorDataPart);

        return errorMessage;
    }

	public ShsMessage buildReplyMessage(ShsMessage requestShsMessage) {
		
		ShsLabel requestLabel = requestShsMessage.getLabel();
		
		ShsMessage responseMessage = new ShsMessage();
		
		responseMessage.setLabel(buildReplyLabel(requestLabel));
		
		return responseMessage;
		
	}
	
}
