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
import se.inera.ifv.sendmedicalcertificateanswer.v1.rivtabp20.SendMedicalCertificateAnswerResponderInterface;
import se.inera.ifv.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType;
import se.inera.ifv.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType;
import se.inera.ifv.v2.ResultCodeEnum;
import se.inera.ifv.v2.ResultOfCall;

import javax.jws.WebService;

@WebService(
		serviceName = "SendMedicalCertificateAnswerResponderService", 
		endpointInterface="se.inera.ifv.sendmedicalcertificateanswer.v1.rivtabp20.SendMedicalCertificateAnswerResponderInterface",
        targetNamespace = "urn:riv:insuranceprocess:healthreporting:SendMedicalCertificateAnswer:1:rivtabp20")
public class SendMedCertAnswerImpl implements SendMedicalCertificateAnswerResponderInterface {

	public SendMedicalCertificateAnswerResponseType sendMedicalCertificateAnswer(
			AttributedURIType logicalAddress,
			SendMedicalCertificateAnswerType parameters) {
		try {
			SendMedicalCertificateAnswerResponseType response = new SendMedicalCertificateAnswerResponseType();
			
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