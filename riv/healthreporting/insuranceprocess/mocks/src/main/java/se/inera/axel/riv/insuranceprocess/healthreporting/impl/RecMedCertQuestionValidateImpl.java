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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.qa.v1.Amnetyp;
import se.inera.ifv.qa.v1.LakarutlatandeEnkelType;
import se.inera.ifv.receivemedicalcertificatequestion.v1.rivtabp20.ReceiveMedicalCertificateQuestionResponderInterface;
import se.inera.ifv.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;
import se.inera.ifv.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionResponseType;
import se.inera.ifv.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType;
import se.inera.ifv.v2.*;

import javax.jws.WebService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Validation class that will certify a webservice call made for a question regarding a medical certificate.. We will check mandatory/optional fields and all other declared rules.
 * @author matsek
 *
 */

@WebService(
		serviceName = "ReceiveMedicalCertificateQuestionResponderService", 
		endpointInterface="se.inera.ifv.receivemedicalcertificatequestion.v1.rivtabp20.ReceiveMedicalCertificateQuestionResponderInterface",
		targetNamespace = "urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateQuestion:1:rivtabp20")
public class RecMedCertQuestionValidateImpl implements ReceiveMedicalCertificateQuestionResponderInterface {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	public ReceiveMedicalCertificateQuestionResponseType receiveMedicalCertificateQuestion(
			AttributedURIType logicalAddress,
			ReceiveMedicalCertificateQuestionType parameters) {
		
		log.info("receiveMedicalCertificateQuestion({}, {})", logicalAddress, parameters);
		
		// List of validation errors
		ArrayList<String> validationErrors = new ArrayList<String>();

		// Create a response and set result of validation            
		ReceiveMedicalCertificateQuestionResponseType outResponse = new ReceiveMedicalCertificateQuestionResponseType();
		ResultOfCall outResCall = new ResultOfCall();
		outResponse.setResult(outResCall);

		// Validate incoming request
		try {
			// Check that we got any data at all
			if (parameters == null) {
				validationErrors.add("No ReceiveMedicalCertificateQuestion found in incoming data!");
				throw new Exception();
			}
			
			// Check that we got an question element
			if (parameters.getQuestion() == null) {
				validationErrors.add("No Question element found in incoming request data!");
				throw new Exception();
			}
			
			QuestionFromFkType inQuestion = parameters.getQuestion();
			
			/**
			 *  Check meddelande data + lakarutlatande reference
			 */
			
			// Ämne - mandatory
			Amnetyp inAmne = inQuestion.getAmne();
			if ( inAmne == null) {
				validationErrors.add("No Amne element found!");				
			}
						
			// Komplettering - optional
			if (inQuestion.getFkKomplettering() != null && inQuestion.getFkKomplettering().size() > 0) {
				// Check kompletterings data
				
			}
			
			// Avsänt tidpunkt - mandatory
            if (inQuestion.getAvsantTidpunkt() == null || !inQuestion.getAvsantTidpunkt().isValid()) {
				validationErrors.add("No or wrong avsantTidpunkt found!");				
            }
			
			// Sista datum för komplettering - optional
            if (inQuestion.getFkSistaDatumForSvar() != null) {
            	if (!inQuestion.getFkSistaDatumForSvar().isValid()) {
    				validationErrors.add("Wrong sistaDatumForKomplettering found!");				
            	}
            }
			
			// Läkarutlåtande referens - mandatory
            if (inQuestion.getLakarutlatande() == null ) {
				validationErrors.add("No lakarutlatande element found!");	
				throw new Exception();
            }
            LakarutlatandeEnkelType inLakarUtlatande = inQuestion.getLakarutlatande();
            
			// Läkarutlåtande referens - id - mandatory
			if ( inLakarUtlatande.getLakarutlatandeId() == null ||
				inLakarUtlatande.getLakarutlatandeId().length() < 1 ) {
				validationErrors.add("No lakarutlatande-id found!");				
			}

			// Läkarutlåtande referens - avsantTidpunkt - mandatory
            if (inLakarUtlatande.getSigneringsTidpunkt() == null || !inLakarUtlatande.getSigneringsTidpunkt().isValid()) {
				validationErrors.add("No or wrong lakarutlatande-avsantTidpunkt found!");				
            }

			// Läkarutlåtande referens - patient - mandatory
            if (inLakarUtlatande.getPatient() == null ) {
				validationErrors.add("No lakarutlatande patient element found!");	
				throw new Exception();
            }
            PatientType inPatient = inLakarUtlatande.getPatient();
            
			// Läkarutlåtande referens - patient - personid mandatory
            // Check patient id - mandatory
			if (inPatient.getPersonId() == null ||	
				inPatient.getPersonId().getExtension() == null ||	
				inPatient.getPersonId().getExtension().length() < 1) {
				validationErrors.add("No lakarutlatande-Patient Id found!");								
			}
			// Check patient o.i.d.
			if (inPatient.getPersonId() == null ||	
				inPatient.getPersonId().getRoot() == null ||	
				(!inPatient.getPersonId().getRoot().equalsIgnoreCase("1.2.752.129.2.1.3.1") && !inPatient.getPersonId().getRoot().equalsIgnoreCase("1.2.752.129.2.1.3.3"))) {
					validationErrors.add("Wrong o.i.d. for Patient Id! Should be 1.2.752.129.2.1.3.1 or 1.2.752.129.2.1.3.3");								
				}
			String inPersonnummer = inPatient.getPersonId().getExtension();

            // Check format on personnummer? samordningsnummer?
            
			// Läkarutlåtande referens - patient - namn - mandatory
			if (inPatient.getFullstandigtNamn() == null || inPatient.getFullstandigtNamn().length() < 1 ) {
				validationErrors.add("No lakarutlatande Patient fullstandigtNamn elements found or set!");								
			}
		
			/**
			 *  Check mottagare data. Depending on direction of question (from varden or FK) validate mottagare.
			 */
			if ( inQuestion.getAdressVard() == null) {
				validationErrors.add("No Vard adress element found!");				
				throw new Exception();
			} 
			
			if ( inQuestion.getAdressVard().getHosPersonal() == null) {
				validationErrors.add("No vard - hosPersonal element found!");				
				throw new Exception();
			}
			checkHoSPersonal(inQuestion.getAdressVard().getHosPersonal(), validationErrors);				
						
			
			// Check if we got any validation errors that not caused an Exception
			if (validationErrors.size() > 0) {
				throw new Exception();
			} 
			
			// No validation errors! Return OK!            
			outResCall.setResultCode(ResultCodeEnum.OK);
			outResponse.setResult(outResCall);

			return outResponse;
		} catch (Exception e) {
			outResCall.setErrorText(getValidationErrors(validationErrors));
			outResCall.setResultCode(ResultCodeEnum.ERROR);
			return outResponse;
		}
	}
	
	private String getValidationErrors(ArrayList<String> validationErrors) {
		int i = 1;
		StringBuffer validationString = new StringBuffer();
		Iterator<String> iterValidationErrors = validationErrors.iterator();
		validationString.append("Validation error " + i++ + ":");
		validationString.append((String)iterValidationErrors.next());
		while (iterValidationErrors.hasNext()) {
			validationString.append("\n\rValidation error " + i++ + ":");
			validationString.append((String)iterValidationErrors.next());
		}
		return validationString.toString();
	}
	
	private void checkHoSPersonal(HosPersonalType inHoSP, List<String> validationErrors) throws Exception {
        
        // Check lakar id - mandatory
        if (inHoSP.getPersonalId() == null || 
        	inHoSP.getPersonalId().getExtension() == null ||
        	inHoSP.getPersonalId().getExtension().length() < 1) {
			validationErrors.add("No personal-id found!");	            	
        }
        // Check lakar id o.i.d.
        if (inHoSP.getPersonalId() == null || 
        	inHoSP.getPersonalId().getRoot() == null ||
            !inHoSP.getPersonalId().getRoot().equalsIgnoreCase("1.2.752.129.2.1.4.1")) {
			validationErrors.add("Wrong o.i.d. for personalId! Should be 1.2.752.129.2.1.4.1");								
        }
        
        // Check lakarnamn - mandatory
		if (inHoSP.getFullstandigtNamn() == null || inHoSP.getFullstandigtNamn().length() < 1 ) {
			validationErrors.add("No skapadAvHosPersonal namn elements found! fullstandigtNamn or (fornamn and efternamn) should be set.");								
		}

        // Check that we got a enhet element
        if (inHoSP.getEnhet() == null) {
			validationErrors.add("No enhet element found!");	  
			throw new Exception();
        }
        EnhetType inEnhet = inHoSP.getEnhet() ;
       
        // Check enhets id - mandatory
        if (inEnhet.getEnhetsId() == null ||
        	inEnhet.getEnhetsId().getExtension() == null ||
        	inEnhet.getEnhetsId().getExtension().length() < 1) {
			validationErrors.add("No enhets-id found!");	            	
        }
        // Check enhets o.i.d
        if (inEnhet.getEnhetsId() == null || 
        	inEnhet.getEnhetsId().getRoot() == null ||
            !inEnhet.getEnhetsId().getRoot().equalsIgnoreCase("1.2.752.129.2.1.4.1")) {
			validationErrors.add("Wrong o.i.d. for enhetsId! Should be 1.2.752.129.2.1.4.1");								
        }
        
        // Check enhetsnamn - mandatory
        if (inEnhet.getEnhetsnamn() == null || 
        	inEnhet.getEnhetsnamn().length() < 1) {
        	validationErrors.add("No enhetsnamn found!");	            	
        }

        // Check enhetsadress - mandatory      
		if (inEnhet.getPostadress() == null && inEnhet.getPostadress().length() < 1 ) {
			validationErrors.add("No postadress found for enhet!");								
		}
		if (inEnhet.getPostnummer() == null && inEnhet.getPostnummer().length() < 1 ) {
			validationErrors.add("No postnummer found for enhet!");								
		}
		if (inEnhet.getPostort() == null && inEnhet.getPostort().length() < 1 ) {
			validationErrors.add("No postort found for enhet!");								
		}

        // Check that we got a vardgivare element
        if (inEnhet.getVardgivare() == null) {
			validationErrors.add("No vardgivare element found!");	  
			throw new Exception();
        }
        VardgivareType inVardgivare = inEnhet.getVardgivare();
       
        // Check vardgivare id - mandatory
        if (inVardgivare.getVardgivareId() == null ||
        	inVardgivare.getVardgivareId().getExtension() == null ||
        	inVardgivare.getVardgivareId().getExtension().length() < 1) {
			validationErrors.add("No vardgivare-id found!");	            	
        }
        // Check vardgivare o.i.d.
        if (inVardgivare.getVardgivareId() == null || 
        	inVardgivare.getVardgivareId().getRoot() == null ||
            !inVardgivare.getVardgivareId().getRoot().equalsIgnoreCase("1.2.752.129.2.1.4.1")) {
        	validationErrors.add("Wrong o.i.d. for vardgivareId! Should be 1.2.752.129.2.1.4.1");								
        }

        // Check vardgivarename - mandatory
        if (inVardgivare.getVardgivarnamn() == null || 
        	inVardgivare.getVardgivarnamn().length() < 1) {
			validationErrors.add("No vardgivarenamn found!");	            	
        }
	}
}