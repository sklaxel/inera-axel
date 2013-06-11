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

import java.util.Date;
import java.util.UUID;

import javax.activation.DataHandler;

import se.inera.axel.shs.exception.OtherErrorException;
import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.EndRecipient;
import se.inera.axel.shs.xml.label.From;
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
		
		return errorLabel;
	}
	
	public ShsLabel buildConfirmLabel(ShsLabel requestLabel) {
		ShsLabel confirmLabel = buildReplyLabel(requestLabel);

		Product confirmProduct = new Product();
		confirmProduct.setvalue("confirm");
		confirmLabel.setProduct(confirmProduct);
		confirmLabel.setSequenceType(SequenceType.ADM);
		
		return confirmLabel;
	}	
	
	public ShsLabel buildReplyLabel(ShsLabel requestLabel) {
		// TODO fix clone
		ShsLabelMarshaller labelMarshaller = new ShsLabelMarshaller(); 
		String labelXml = labelMarshaller.marshal(requestLabel);
		ShsLabel replyLabel = labelMarshaller.unmarshal(labelXml);
		
		To requestTo = requestLabel.getTo();
		From requestFrom = requestLabel.getFrom();
		Originator requestOriginator = requestLabel.getOriginator();
		EndRecipient requestEndRecipient = requestLabel.getEndRecipient();
	
		From newFrom = null;
		
			newFrom = new From();
			if (requestTo != null) {
			newFrom.setCommonName(requestTo.getCommonName());
			newFrom.setvalue(requestTo.getvalue());
		}
		
		To newTo = null;
		
		if (requestFrom != null) {
			newTo = new To();
			newTo.setCommonName(requestFrom.getCommonName());
			newTo.setvalue(requestFrom.getvalue());
		}
		
		EndRecipient newEndRecipient = null;
		
		if (requestOriginator != null) {
			newEndRecipient = new EndRecipient();
			newEndRecipient.setLabeledURI(requestOriginator.getLabeledURI());
			newEndRecipient.setName(requestOriginator.getName());
			newEndRecipient.setvalue(requestOriginator.getvalue());
		}
		
		Originator newOriginator = null;
		
		if (requestEndRecipient != null) {
			newOriginator = new Originator();
			newOriginator.setLabeledURI(requestEndRecipient.getLabeledURI());
			newOriginator.setName(requestEndRecipient.getName());
			newOriginator.setvalue(requestEndRecipient.getvalue());
		}
		
		replyLabel.setTo(newTo);
		replyLabel.setEndRecipient(newEndRecipient);
		replyLabel.getOriginatorOrFrom().clear();
		
		if (newOriginator != null) {
			replyLabel.getOriginatorOrFrom().add(newOriginator);
		}
		
		if (newFrom != null) {
			replyLabel.getOriginatorOrFrom().add(newFrom);
		}
		
		replyLabel.setSequenceType(SequenceType.REPLY);
		replyLabel.setCorrId(requestLabel.getCorrId());
		
		if (replyLabel.getContent() != null) {
			replyLabel.getContent().setContentId(UUID.randomUUID().toString());
			replyLabel.getContent().setComment(null);
			replyLabel.getContent().getDataOrCompound().clear();
		}
		
		if (replyLabel.getTransferType() == TransferType.SYNCH)
			replyLabel.setTxId(requestLabel.getTxId());
		else
			replyLabel.setTxId(UUID.randomUUID().toString());
				
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
	
	
	
	public ShsMessage buildReplyMessage(ShsMessage requestShsMessage) {
		
		ShsLabel requestLabel = requestShsMessage.getLabel();
		
		ShsMessage responseMessage = new ShsMessage();
		
		responseMessage.setLabel(buildReplyLabel(requestLabel));
		
		return responseMessage;
		
	}
	
}
