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

import se.inera.axel.shs.exception.*;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.management.Appinfo;
import se.inera.axel.shs.xml.management.Error;
import se.inera.axel.shs.xml.management.ObjectFactory;
import se.inera.axel.shs.xml.management.ShsManagement;

import java.util.Date;
import java.util.Map;

public class ShsExceptionConverter {
	private static ObjectFactory shsManagementFactory = new ObjectFactory();

	static ShsManagementMarshaller managementMarshaller = new ShsManagementMarshaller();



	public static ShsManagement toShsManagement(ShsException src) {
		ShsManagement dest = shsManagementFactory.createShsManagement();
		
		dest.setContentId(src.getContentId());
		dest.setCorrId(src.getContentId());
		// TODO should we have a Datetime om ShsException?
		dest.setDatetime(new Date());
		Error error = shsManagementFactory.createError();
		error.setErrorcode(src.getErrorCode());
		error.setErrorinfo(src.getErrorInfo());
		for (Map.Entry<String, String> appInfoEntry : src.getAppInfo().entrySet()) {
			Appinfo appInfo = shsManagementFactory.createAppinfo();
			appInfo.setName(appInfoEntry.getKey());
			appInfo.setValue(appInfoEntry.getValue());
			error.getAppinfo().add(appInfo);
		}
		
		dest.getConfirmationOrError().add(error);
		return dest;
	}
	
	public static ShsException toShsException(ShsManagement management) {
		se.inera.axel.shs.xml.management.Error error;

		if (management == null) {
			return null;
		}

		error = management.getError();
		if (error == null) {
			return null;
		}

		ShsException dest = createShsException(error);
		
		for (Appinfo appinfo : error.getAppinfo()) {
			dest.appendAppInfo(appinfo.getName(), appinfo.getValue());
		}
		
		dest.setContentId(management.getContentId());
		dest.setCorrId(management.getCorrId());
		
		return dest;
	}

	public static ShsException toShsException(DataPart dataPart) {
		if (dataPart == null) {
			return null;
		}

		ShsManagement management = null;
		try {
			management = managementMarshaller.unmarshal(dataPart.getDataHandler().getInputStream());
			if (management == null)
				return null;

			return toShsException(management);
		} catch (ShsException e) {
			throw e;
		} catch (Exception e) {
			throw new OtherErrorException("SHS Message is not valid", e);
		}
	}


	public static ShsException toShsException(ShsMessage shsMessage) {
		if (shsMessage == null) {
			return null;
		}

		if (shsMessage.getLabel().getSequenceType() == SequenceType.ADM
				&& (shsMessage.getLabel().getProduct().getValue().equals("error")))
		{

			if (shsMessage.getDataParts().isEmpty())
				return null;

			DataPart dataPart = shsMessage.getDataParts().get(0);

			return toShsException(dataPart);
		}

		return null;
	}

    public static ShsException createShsException(Error error) {
        return createShsException(error.getErrorcode(), error.getErrorinfo());
    }

	public static ShsException createShsException(String errorCode, String errorInfo) {
		ShsException exception = null;
		
		if (errorCode == null || errorCode.isEmpty()) {
			throw new IllegalArgumentException("Errorcode must have a value");
		}
		
		if (errorCode.equalsIgnoreCase("UnresolvedReceiver")) {
			exception = new UnresolvedReceiverException(errorInfo);
		} else if (errorCode.equalsIgnoreCase("MissingAgreement")) {
			exception = new MissingAgreementException(errorInfo);
		} else if (errorCode.equalsIgnoreCase("MissingDeliveryAddress")) {
			exception = new MissingDeliveryAddressException(errorInfo);
		} else if (errorCode.equalsIgnoreCase("MissingDeliveryExecution")) {
			exception = new MissingDeliveryExecutionException(errorInfo);
		} else if (errorCode.equalsIgnoreCase("IllegalProductType")) {
			exception = new IllegalProductTypeException(errorInfo);
		} else if (errorCode.equalsIgnoreCase("UnknownProductType")) {
			exception = new UnknownProductTypeException(errorInfo);
		} else if (errorCode.equalsIgnoreCase("IllegalReceiver")) {
			exception = new IllegalReceiverException(errorInfo);
		} else if (errorCode.equalsIgnoreCase("UnknownReceiver")) {
			exception = new UnknownReceiverException(errorInfo);
		} else if (errorCode.equalsIgnoreCase("IllegalSender")) {
			exception = new IllegalSenderException(errorInfo);
		} else if (errorCode.equalsIgnoreCase("UnknownSender")) {
			exception = new UnknownSenderException(errorInfo);
		} else if (errorCode.equalsIgnoreCase("IllegalOriginator")) {
			exception = new IllegalOriginatorException(errorInfo);
		} else if (errorCode.equalsIgnoreCase("IllegalEndRecipient")) {
			exception = new IllegalEndRecipientException(errorInfo);
		} else if (errorCode.equalsIgnoreCase("IllegalMessageStructure")) {
			exception = new IllegalMessageStructureException(errorInfo);
		} else if (errorCode.equalsIgnoreCase("IllegalDatapartContent")) {
			exception = new IllegalDatapartContentException(errorInfo);
		} else {
			exception = new OtherErrorException(errorInfo);
		}
		
		return exception;
	}

}
