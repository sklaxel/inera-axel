/**
 * Copyright 2009 Sjukvardsradgivningen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public

 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the

 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,

 *   Boston, MA 02111-1307  USA
 */
package se.inera.axel.riv.insuranceprocess.healthreporting.impl;

import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.sendmedicalcertificatequestion.v1.rivtabp20.SendMedicalCertificateQuestionResponderInterface;
import se.inera.ifv.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.ifv.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionType;
import se.inera.ifv.v2.ResultCodeEnum;
import se.inera.ifv.v2.ResultOfCall;

import javax.jws.WebService;


@WebService(
		serviceName = "SendMedicalCertificateQuestionResponderService", 
		endpointInterface="se.inera.ifv.sendmedicalcertificatequestion.v1.rivtabp20.SendMedicalCertificateQuestionResponderInterface")
public class SendMedCertQuestionImpl implements SendMedicalCertificateQuestionResponderInterface {

	public SendMedicalCertificateQuestionResponseType sendMedicalCertificateQuestion(
			AttributedURIType logicalAddress,
			SendMedicalCertificateQuestionType parameters) {
		try {
			SendMedicalCertificateQuestionResponseType response = new SendMedicalCertificateQuestionResponseType();
			
			// Ping response
			ResultOfCall resCall = new ResultOfCall();
			resCall.setResultCode(ResultCodeEnum.OK);
			response.setResult(resCall);

			return response;
		} catch (RuntimeException e) {
			System.out.println("Error occured: " + e);
			throw e;
		}
	}
}