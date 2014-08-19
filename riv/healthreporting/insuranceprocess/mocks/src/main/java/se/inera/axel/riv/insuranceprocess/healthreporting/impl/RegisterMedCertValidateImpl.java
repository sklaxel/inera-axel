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

import iso.v21090.dt.v1.II;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;
import se.inera.ifv.v2.*;
import se.inera.mu7263.v3.*;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Validation class that will certify a webservice call made for MU7263. We will check mandatory/optional fields and all other declared rules.
 * @author matsek
 *
 */

@WebService(endpointInterface = "se.inera.ifv.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface", targetNamespace = "urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificate:3:rivtabp20", name = "RegisterMedicalCertificateResponderInterface")
@XmlSeeAlso({se.inera.ifv.registermedicalcertificateresponder.v3.ObjectFactory.class, org.w3.wsaddressing10.ObjectFactory.class, se.inera.mu7263.v3.ObjectFactory.class, se.inera.ifv.v2.ObjectFactory.class, iso.v21090.dt.v1.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class RegisterMedCertValidateImpl implements RegisterMedicalCertificateResponderInterface {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private static final String PERSON_NUMBER_REGEX = "[0-9]{8}[-+]?[0-9]{4}";
	private static final String PERSON_NUMBER_WITHOUT_DASH_REGEX = "[0-9]{12}";

	public RegisterMedicalCertificateResponseType registerMedicalCertificate( AttributedURIType logicalAddress, RegisterMedicalCertificateType parameters) {
		logger.debug("Received call to validating");

		// List of validation errors
		ArrayList<String> validationErrors = new ArrayList<String>();

		// Create a response and set result of validation            
		RegisterMedicalCertificateResponseType outResponse = new RegisterMedicalCertificateResponseType();
		ResultOfCall outResCall = new ResultOfCall();
		outResponse.setResult(outResCall);

		// Validate incoming request
		try {
			// Check To field in SoapHeader
			if (logicalAddress == null || logicalAddress.getValue() == null || logicalAddress.getValue().length() < 1 ) {
				validationErrors.add("No To field or data found in incoming data!");
				throw new Exception();
			}
				
			// Check that we got any data at all
			if (parameters == null) {
				validationErrors.add("No RegisterMedicalCertificate found in incoming data!");
				throw new Exception();
			}
			
			// Check that we got a lakarutlatande element
			if (parameters.getLakarutlatande() == null) {
				validationErrors.add("No Lakarutlatande element found in incoming request data!");
				throw new Exception();
			}
			
			LakarutlatandeType inLakarutlatande = parameters.getLakarutlatande();
			
			/**
			 *  Check all meta-data, that is data not shown in the form
			 */
			
			// Check that we got an id - mandatory
			if ( inLakarutlatande.getLakarutlatandeId() == null ||
				 inLakarutlatande.getLakarutlatandeId().length() < 1 ) {
				validationErrors.add("No Lakarutlatande Id found!");				
			}
	
	        // Check skickat datum - mandatory
	        if (inLakarutlatande.getSkickatDatum() == null || !inLakarutlatande.getSkickatDatum().isValid()) {
				validationErrors.add("No or wrong skickatDatum found!");				
	        }
	
	        /**
	         * Check patient information
	         */          
	        // Check that we got a patient element 
			if ( inLakarutlatande.getPatient() == null) {
				validationErrors.add("No Patient element found!");	
				throw new Exception();
			}			
			PatientType inPatient = inLakarutlatande.getPatient();
			
	        // Check patient id - mandatory
			if (inPatient.getPersonId() == null ||	
				inPatient.getPersonId().getExtension() == null ||	
				inPatient.getPersonId().getExtension().length() < 1) {
				validationErrors.add("No Patient Id found!");								
			}
			// Check patient o.i.d.
			if (inPatient.getPersonId() == null ||	
				inPatient.getPersonId().getRoot() == null ||	
				(!inPatient.getPersonId().getRoot().equalsIgnoreCase("1.2.752.129.2.1.3.1") && !inPatient.getPersonId().getRoot().equalsIgnoreCase("1.2.752.129.2.1.3.3"))) {
					validationErrors.add("Wrong o.i.d. for Patient Id! Should be 1.2.752.129.2.1.3.1 or 1.2.752.129.2.1.3.3");								
				}

			//Correct personnummer without dashes
			String inPersonnummer = inPatient.getPersonId().getExtension();
			
        	if (inPersonnummer.length() == 12 && Pattern.matches(PERSON_NUMBER_WITHOUT_DASH_REGEX, inPersonnummer)) {
            	inPatient.getPersonId().setExtension(inPersonnummer.substring(0,8) + "-" + inPersonnummer.substring(8));
        	}

        	// Check format of patient id (has to be a valid personnummer)
            if (inPersonnummer == null || !Pattern.matches(PERSON_NUMBER_REGEX, inPersonnummer)) {
                validationErrors.add("Wrong format for person-id! Valid format is YYYYMMDD-XXXX or YYYYMMDD+XXXX.");
            }
	
	        // Check format of patient id - personnummer valid format is 19121212-1212 or 19121212+1212
//			if (!Pattern.matches("[0-9]{8}[-+][0-9]{4}", inPersonnummer) ) {
//				validationErrors.add("Wrong format for person-id! Valid format is YYYYMMDD-XXXX or YYYYMMDD+XXXX.");												
//			}
	        			
	        // Get namn for patient - mandatory
			if (inPatient.getFullstandigtNamn() == null || inPatient.getFullstandigtNamn().length() < 1 ) {
				validationErrors.add("No Patient fullstandigtNamn elements found or set!");								
			}
			
			/**
			 * Check hälso och sjukvårds personal information. Vårdgivare, vårdenhet och läkare.
			 */
	        // Check that we got a skapadAvHosPersonal element
			if ( inLakarutlatande.getSkapadAvHosPersonal() == null) {
				validationErrors.add("No SkapadAvHosPersonal element found!");	
				throw new Exception();
			}			
	        HosPersonalType inHoSP = inLakarutlatande.getSkapadAvHosPersonal() ;
	        
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
				validationErrors.add("No skapadAvHosPersonal fullstandigtNamn found.");								
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
			if (inEnhet.getPostadress() == null || inEnhet.getPostadress().length() < 1 ) {
				validationErrors.add("No postadress found for enhet!");								
			}
			if (inEnhet.getPostnummer() == null || inEnhet.getPostnummer().length() < 1 ) {
				validationErrors.add("No postnummer found for enhet!");								
			}
			if (inEnhet.getPostort() == null || inEnhet.getPostort().length() < 1 ) {
				validationErrors.add("No postort found for enhet!");								
			}
			if (inEnhet.getTelefonnummer() == null || inEnhet.getTelefonnummer().length() < 1 ) {
				validationErrors.add("No telefonnummer found for enhet!");								
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
	
	        /**
	         * Check form data
	         */
	        // Fält 1 - no rule
	        boolean inSmittskydd = findAktivitetWithCode(parameters.getLakarutlatande().getAktivitet(), Aktivitetskod.AVSTANGNING_ENLIGT_SM_L_PGA_SMITTA) != null ? true:false;
	
	        // Must be set as this element contains a lot of mandatory information
	        FunktionstillstandType inAktivitetFunktion = findFunktionsTillstandType(inLakarutlatande.getFunktionstillstand(), TypAvFunktionstillstand.AKTIVITET);
	        if (inAktivitetFunktion == null) {
				validationErrors.add("No funktionstillstand - aktivitet element found!");	
				throw new Exception();
	        }
	
	        // Declared outside as it may be used further down. 
	        ReferensType inAnnat = null;
	
	        // Many fields are optional if smittskydd is checked, if not set validate these below
	        if (!inSmittskydd) {
	            // Fält 2 - Check that we got a medicinsktTillstand element
	            if (inLakarutlatande.getMedicinsktTillstand() == null) {
	    			validationErrors.add("No medicinsktTillstand element found!");	
	    			throw new Exception();
	            }
	            // Fält 2 - Medicinskt tillstånd kod - mandatory
	            MedicinsktTillstandType medTillstand = inLakarutlatande.getMedicinsktTillstand();
	            if (medTillstand.getTillstandskod() == null ||
	            	medTillstand.getTillstandskod().getCode() == null ||
	            	medTillstand.getTillstandskod().getCode().length() < 1) {
	    			validationErrors.add("No tillstandskod in medicinsktTillstand found!");	            	
	            }
	            // Fält 2 - Medicinskt tillstånd kodsystemnamn - mandatory
	            if (medTillstand.getTillstandskod() == null || 
	            	medTillstand.getTillstandskod().getCodeSystemName() == null ||
	                !medTillstand.getTillstandskod().getCodeSystemName().equalsIgnoreCase("ICD-10")) {
	            	validationErrors.add("Wrong code system name for medicinskt tillstand - tillstandskod (diagnoskod)! Should be ICD-10");								
	            }
	            // Fält 2 - Medicinskt tillstånd beskrivning - optional
	                        
	            // Fält 3 - Not mandatory
	 
	            // Fält 4 - vänster Check that we got a funktionstillstand - kroppsfunktion element  
	            FunktionstillstandType inKroppsFunktion = findFunktionsTillstandType(inLakarutlatande.getFunktionstillstand(), TypAvFunktionstillstand.KROPPSFUNKTION);
	            if (inKroppsFunktion == null ) {
	    			validationErrors.add("No funktionstillstand - kroppsfunktion element found!");	
	    			throw new Exception();
	        	}
	            // Fält 4 - vänster Funktionstillstand - kroppsfunktion beskrivning - mandatory  
	            if (inKroppsFunktion.getBeskrivning() == null || 
	            	inKroppsFunktion.getBeskrivning().length() < 1) {
	    			validationErrors.add("No beskrivning in funktionstillstand - kroppsfunktion found!");	
	            }
	
	            // Fält 4 - höger översta kryssrutan
	            VardkontaktType inUndersokning = findVardkontaktTyp(inLakarutlatande.getVardkontakt(), Vardkontakttyp.MIN_UNDERSOKNING_AV_PATIENTEN);
	            
	            // Fält 4 - höger näst översta kryssrutan
	            VardkontaktType telefonkontakt = findVardkontaktTyp(inLakarutlatande.getVardkontakt(), Vardkontakttyp.MIN_TELEFONKONTAKT_MED_PATIENTEN);
	
	            // Fält 4 - höger näst nedersta kryssrutan
	            ReferensType journal = findReferensTyp(inLakarutlatande.getReferens(), Referenstyp.JOURNALUPPGIFTER);
	
	            // Fält 4 - höger nedersta kryssrutan
	            inAnnat = findReferensTyp(inLakarutlatande.getReferens(), Referenstyp.ANNAT);
	
	            // Fält 4 - höger Check that we at least got one field set
	            if (inUndersokning==null && telefonkontakt==null && journal==null && inAnnat==null) {
	    			validationErrors.add("No vardkontakt or referens element found ! At least one must be set!");
	    			throw new Exception();
	            }
	            // Fält 4 - höger - 1:a kryssrutan Check that we got a date if choice is set
	            if(inUndersokning != null && (inUndersokning.getVardkontaktstid() == null || !inUndersokning.getVardkontaktstid().isValid())) {
	    			validationErrors.add("No or wrong date for vardkontakt - min undersokning av patienten found!");                	
	            }
	            // Fält 4 - höger - 2:a kryssrutan Check that we got a date if choice is set
	            if(telefonkontakt != null && (telefonkontakt.getVardkontaktstid() == null || !telefonkontakt.getVardkontaktstid().isValid())) {
	    			validationErrors.add("No or wrong date for vardkontakt - telefonkontakt found!");                	
	            }
	            // Fält 4 - höger - 3:e kryssrutan Check that we got a date if choice is set
	            if(journal != null && (journal.getDatum() == null || !journal.getDatum().isValid())) {
	    			validationErrors.add("No or wrong date for referens - journal found!");                	
	            }
	            // Fält 4 - höger - 4:e kryssrutan Check that we got a date if choice is set
	            if(inAnnat != null && (inAnnat.getDatum() == null || !inAnnat.getDatum().isValid())) {
	    			validationErrors.add("No or wrong date for referens - annat found!");                	
	            }
	                   
	            // Fält 5 - not mandatory

	            // Fält 6 - not mandatory but if we have checked a box that needs more information this should be provided
//	            AktivitetType ovrigt = findAktivitetWithCode(inLakarutlatande.getAktivitet(), Aktivitetskod.OVRIGT);
//	            if (ovrigt != null && (ovrigt.getBeskrivning() == null || ovrigt.getBeskrivning().length() < 1)) {
//	    			validationErrors.add("No corresponding beskrivning for rekommendationer - ovrigt found!");                		
//	            }
//
//	            AktivitetType planeradAtgardInomSjukvarden = findAktivitetWithCode(inLakarutlatande.getAktivitet(), Aktivitetskod.PLANERAD_ELLER_PAGAENDE_BEHANDLING_ELLER_ATGARD_INOM_SJUKVARDEN);
//	            if (planeradAtgardInomSjukvarden != null && (planeradAtgardInomSjukvarden.getBeskrivning() == null || planeradAtgardInomSjukvarden.getBeskrivning().length() < 1)) {
//	    			validationErrors.add("No corresponding beskrivning for planerad eller pagaende behandling eller atgard - inom sjukvarden found!");                		
//	            }
//
//	            AktivitetType planeradAtgardAnnan = findAktivitetWithCode(inLakarutlatande.getAktivitet(), Aktivitetskod.PLANERAD_ELLER_PAGAENDE_ANNAN_ATGARD);
//	            if (planeradAtgardAnnan != null && (planeradAtgardAnnan.getBeskrivning() == null || planeradAtgardAnnan.getBeskrivning().length() < 1)) {
//	    			validationErrors.add("No corresponding beskrivning for planerad eller pagaende behandling eller atgard - annan atgard found!");                		
//	            }
	        
	            // Fält 7 - not mandatory
	            
	            // Fält 8a - Check that we got a arbetsformaga element
	            if (inAktivitetFunktion.getArbetsformaga() == null) {
	    			validationErrors.add("No arbetsformaga element found for field 8a!");	
	    			throw new Exception();                	
	            }
	
	            // Fält 8a
	            SysselsattningType inArbete = findTypAvSysselsattning(inAktivitetFunktion.getArbetsformaga().getSysselsattning(), TypAvSysselsattning.NUVARANDE_ARBETE);
	            SysselsattningType inArbetslos = findTypAvSysselsattning(inAktivitetFunktion.getArbetsformaga().getSysselsattning(), TypAvSysselsattning.ARBETSLOSHET);
	            SysselsattningType inForaldraledig = findTypAvSysselsattning(inAktivitetFunktion.getArbetsformaga().getSysselsattning(), TypAvSysselsattning.FORALDRALEDIGHET);
	            // Fält 8a - Check that we at least got one choice
	            if (inArbete==null && inArbetslos==null && inForaldraledig==null) {
	    			validationErrors.add("No sysselsattning element found for field 8a! Nuvarande arbete, arbestloshet or foraldraledig should be set.");	
	    			throw new Exception();                	
	            }                
	            ArbetsuppgiftType inArbetsBeskrivning = inAktivitetFunktion.getArbetsformaga().getArbetsuppgift();
	            // Fält 8a - Check that we got a arbetsuppgift element if arbete is set                
	            if (inArbete != null && inArbetsBeskrivning == null) {
	            	validationErrors.add("No arbetsuppgift element found when arbete set in field 8a!.");	
	    			throw new Exception();                	
	            }
                // Fält 8a - 1:a kryssrutan - beskrivning
                if (inArbete != null && (inArbetsBeskrivning.getTypAvArbetsuppgift() == null || inArbetsBeskrivning.getTypAvArbetsuppgift().length() < 1)) {
                	validationErrors.add("No typAvArbetsuppgift found when arbete set in field 8a!.");	
        			throw new Exception();                	
                }
	        	        
   	        // Many fields are optional if smittskydd is checked but some values depend on each other if a part is set like a checkbox
	        } else {
	            // Fält 2 - No check necessary
	                        
	            // Fält 3 - No check necessary
	 
	            // Fält 4 - vänster No check necessary  
	
	            // Fält 4 - höger översta kryssrutan
	            VardkontaktType inUndersokning = findVardkontaktTyp(inLakarutlatande.getVardkontakt(), Vardkontakttyp.MIN_UNDERSOKNING_AV_PATIENTEN);     
	            
	            // Fält 4 - höger näst översta kryssrutan
	            VardkontaktType telefonkontakt = findVardkontaktTyp(inLakarutlatande.getVardkontakt(), Vardkontakttyp.MIN_TELEFONKONTAKT_MED_PATIENTEN);
	
	            // Fält 4 - höger näst nedersta kryssrutan
	            ReferensType journal = findReferensTyp(inLakarutlatande.getReferens(), Referenstyp.JOURNALUPPGIFTER);
	
	            // Fält 4 - höger nedersta kryssrutan
	            inAnnat = findReferensTyp(inLakarutlatande.getReferens(), Referenstyp.ANNAT);
	
	            // Fält 4 - höger - 1:a kryssrutan Check that we got a date if choice is set
	            if(inUndersokning != null && (inUndersokning.getVardkontaktstid() == null || !inUndersokning.getVardkontaktstid().isValid())) {
	    			validationErrors.add("No or wrong date for vardkontakt - min undersokning av patienten found!");                	
	            }
	            // Fält 4 - höger - 2:a kryssrutan Check that we got a date if choice is set
	            if(telefonkontakt != null && (telefonkontakt.getVardkontaktstid() == null || !telefonkontakt.getVardkontaktstid().isValid())) {
	    			validationErrors.add("No or wrong date for vardkontakt - telefonkontakt found!");                	
	            }
	            // Fält 4 - höger - 3:e kryssrutan Check that we got a date if choice is set
	            if(journal != null && (journal.getDatum() == null || !journal.getDatum().isValid())) {
	    			validationErrors.add("No or wrong date for referens - journal found!");                	
	            }
	            // Fält 4 - höger - 4:e kryssrutan Check that we got a date if choice is set
	            if(inAnnat != null && (inAnnat.getDatum() == null || !inAnnat.getDatum().isValid())) {
	    			validationErrors.add("No or wrong date for referens - annat found!");                	
	            }
	                   
	            // Fält 5 - not mandatory
	
	            // Fält 6 - not mandatory
	            AktivitetType ovrigt = findAktivitetWithCode(inLakarutlatande.getAktivitet(), Aktivitetskod.OVRIGT);
	            if (ovrigt != null && (ovrigt.getBeskrivning() == null || ovrigt.getBeskrivning().length() < 1)) {
	    			validationErrors.add("No corresponding beskrivning for rekommendationer - ovrigt found!");                		
	            }
	
	            AktivitetType planeradAtgardInomSjukvarden = findAktivitetWithCode(inLakarutlatande.getAktivitet(), Aktivitetskod.PLANERAD_ELLER_PAGAENDE_BEHANDLING_ELLER_ATGARD_INOM_SJUKVARDEN);
	            if (planeradAtgardInomSjukvarden != null && (planeradAtgardInomSjukvarden.getBeskrivning() == null || planeradAtgardInomSjukvarden.getBeskrivning().length() < 1)) {
	    			validationErrors.add("No corresponding beskrivning for planerad eller pagaende behandling eller atgard - inom sjukvarden found!");                		
	            }
	
	            AktivitetType planeradAtgardAnnan = findAktivitetWithCode(inLakarutlatande.getAktivitet(), Aktivitetskod.PLANERAD_ELLER_PAGAENDE_ANNAN_ATGARD);
	            if (planeradAtgardAnnan != null && (planeradAtgardAnnan.getBeskrivning() == null || planeradAtgardAnnan.getBeskrivning().length() < 1)) {
	    			validationErrors.add("No corresponding beskrivning for planerad eller pagaende behandling eller atgard - annan atgard found!");                		
	            }
	        
	            // Fält 7 - not mandatory
	            
	            // Fält 8a - Check that we got a description if arbete is set
	            if (inAktivitetFunktion.getArbetsformaga() != null ) {
	            	SysselsattningType inArbete = findTypAvSysselsattning(inAktivitetFunktion.getArbetsformaga().getSysselsattning(), TypAvSysselsattning.NUVARANDE_ARBETE);
	
	            	ArbetsuppgiftType inArbetsBeskrivning = inAktivitetFunktion.getArbetsformaga().getArbetsuppgift();
	            	
		            if (inArbete != null &&  ( inArbetsBeskrivning.getTypAvArbetsuppgift() == null || inArbetsBeskrivning.getTypAvArbetsuppgift().length() < 1)) {
		    			validationErrors.add("No typAvArbetsuppgift found when arbete set in field 8a!.");                		
		            }
	            }
	        }

	
	        // Fält 8b - kryssruta 1
	        ArbetsformagaNedsattningType nedsatt14del =  findArbetsformaga(inAktivitetFunktion.getArbetsformaga().getArbetsformagaNedsattning(), se.inera.mu7263.v3.Nedsattningsgrad.NEDSATT_MED_1_4);
	
	        // Fält 8b - kryssruta 2
	        ArbetsformagaNedsattningType nedsatthalften =  findArbetsformaga(inAktivitetFunktion.getArbetsformaga().getArbetsformagaNedsattning(), se.inera.mu7263.v3.Nedsattningsgrad.NEDSATT_MED_1_2);
	        
	        // Fält 8b - kryssruta 3
	        ArbetsformagaNedsattningType nedsatt34delar =  findArbetsformaga(inAktivitetFunktion.getArbetsformaga().getArbetsformagaNedsattning(), se.inera.mu7263.v3.Nedsattningsgrad.NEDSATT_MED_3_4);
	
	        // Fält 8b - kryssruta 4
	        ArbetsformagaNedsattningType heltNedsatt =  findArbetsformaga(inAktivitetFunktion.getArbetsformaga().getArbetsformagaNedsattning(), se.inera.mu7263.v3.Nedsattningsgrad.HELT_NEDSATT);
	
	        // Check that we at least got one choice
	        if (nedsatt14del == null && nedsatthalften == null && nedsatt34delar == null && heltNedsatt == null) {
	        	validationErrors.add("No arbetsformaganedsattning element found 8b!.");	
				throw new Exception();                	
	        }            
	        // Fält 8b - kryssruta 1 - varaktighet From
	        if (nedsatt14del != null && (nedsatt14del.getVaraktighetFrom() == null || !nedsatt14del.getVaraktighetFrom().isValid())) {
	        	validationErrors.add("No or wrong date for nedsatt 1/4 from date found!");		
	        }
	        // Fält 8b - kryssruta 1 - varaktighet Tom
	        if (nedsatt14del != null && (nedsatt14del.getVaraktighetTom() == null || !nedsatt14del.getVaraktighetTom().isValid())) {
	        	validationErrors.add("No or wrong date for nedsatt 1/4 tom date found!");		
	        }
	        // Fält 8b - kryssruta 2 - varaktighet From
	        if (nedsatthalften != null && (nedsatthalften.getVaraktighetFrom() == null || !nedsatthalften.getVaraktighetFrom().isValid())) {
	        	validationErrors.add("No or wrong date for nedsatt 1/2 from date found!");		
	        }
	        // Fält 8b - kryssruta 2 - varaktighet Tom
	        if (nedsatthalften != null && (nedsatthalften.getVaraktighetTom() == null || !nedsatthalften.getVaraktighetTom().isValid())) {
	        	validationErrors.add("No or wrong date for nedsatt 1/2 tom date found!");		
	        }
	        // Fält 8b - kryssruta 3 - varaktighet From
	        if (nedsatt34delar != null && (nedsatt34delar.getVaraktighetFrom() == null || !nedsatt34delar.getVaraktighetFrom().isValid())) {
	        	validationErrors.add("No or wrong date for nedsatt 3/4 from date found!");		
	        }
	        // Fält 8b - kryssruta 3 - varaktighet Tom
	        if (nedsatt34delar != null && (nedsatt34delar.getVaraktighetTom() == null || !nedsatt34delar.getVaraktighetTom().isValid())) {
	        	validationErrors.add("No or wrong date for nedsatt 3/4 tom date found!");		
	        }
	        // Fält 8b - kryssruta 4 - varaktighet From
	        if (heltNedsatt != null && (heltNedsatt.getVaraktighetFrom() == null || !heltNedsatt.getVaraktighetFrom().isValid())) {
	        	validationErrors.add("No or wrong date for helt nedsatt from date found!");		
	        }
	        // Fält 8b - kryssruta 4 - varaktighet Tom
	        if (heltNedsatt != null && (heltNedsatt.getVaraktighetTom() == null || !heltNedsatt.getVaraktighetTom().isValid())) {
	        	validationErrors.add("No or wrong date for helt nedsatt tom date found!");		
	        }
	        
	        // Fält 9 - Motivering - optional
	        
	        // Fält 10 - Prognosangivelse - optional
	        boolean inArbetsformagaAterstallasHelt = false;
	        boolean inArbetsformagaAterstallasDelvis = false;
	        boolean inArbetsformagaEjAterstallas = false;
	        boolean inArbetsformagaGarEjAttBedomma = false;

	        if (inAktivitetFunktion.getArbetsformaga().getPrognosangivelse() != null) {
		        inArbetsformagaAterstallasHelt = inAktivitetFunktion.getArbetsformaga().getPrognosangivelse().compareTo(Prognosangivelse.ATERSTALLAS_HELT) == 0;
		        inArbetsformagaAterstallasDelvis = inAktivitetFunktion.getArbetsformaga().getPrognosangivelse().compareTo(Prognosangivelse.ATERSTALLAS_DELVIS) == 0;
		        inArbetsformagaEjAterstallas = inAktivitetFunktion.getArbetsformaga().getPrognosangivelse().compareTo(Prognosangivelse.INTE_ATERSTALLAS) == 0;
		        inArbetsformagaGarEjAttBedomma = inAktivitetFunktion.getArbetsformaga().getPrognosangivelse().compareTo(Prognosangivelse.DET_GAR_INTE_ATT_BEDOMMA) == 0;	        	
	        }

	        // If we got more then one prognoselement these will not be read as only the first is set!
	        int inPrognosCount = 0;
	        if (inArbetsformagaAterstallasHelt) {
	        	inPrognosCount++;
	        }
	        if (inArbetsformagaAterstallasDelvis) {
	        	inPrognosCount++;
	        }
	        if (inArbetsformagaEjAterstallas) {
	        	inPrognosCount++;
	        }
	        if (inArbetsformagaGarEjAttBedomma) {
	        	inPrognosCount++;
	        }
	        
	        // Fält 10 - Prognosangivelse - Check that we only got one choice            
	        if (inPrognosCount > 2) {
	            validationErrors.add("Only one prognosangivelse should be set for field 10.");	
	        }            
	        
	        // Fält 11 - optional
	        AktivitetType inForandratRessatt = findAktivitetWithCode(inLakarutlatande.getAktivitet(), Aktivitetskod.FORANDRAT_RESSATT_TILL_ARBETSPLATSEN_AR_AKTUELLT);
	        AktivitetType inEjForandratRessatt = findAktivitetWithCode(inLakarutlatande.getAktivitet(), Aktivitetskod.FORANDRAT_RESSATT_TILL_ARBETSPLATSEN_AR_EJ_AKTUELLT);
	        
	        // Fält 11 - If set only one should be set
	        if (inForandratRessatt != null && inEjForandratRessatt != null) {
	            validationErrors.add("Only one forandrat ressatt could be set for field 11.");	
	        }
	        
	        // Fält 12 - kryssruta 1 - optional
	        
	        // Fält 13 - Upplysningar - optional
	        // If field 4 annat satt or field 10 går ej att bedömma is set then field 13 should contain data.
			String kommentar = parameters.getLakarutlatande().getKommentar();
	        if ( (inAnnat!=null || inArbetsformagaGarEjAttBedomma) && (kommentar == null || kommentar.length() < 1) ){
	            validationErrors.add("Upplysningar should contain data as field 4 or fields 10 is checked.");	            	
	        }
	
	        // Fält 14 - Signeringstidpunkt
	        if (inLakarutlatande.getSigneringsdatum() == null || !inLakarutlatande.getSigneringsdatum().isValid()) {
	            validationErrors.add("Signeringsdatum must be set (14)");	            	
	        }
	        
	        // Fält 17 - arbetsplatskod - Check that we got an element
	        if (inEnhet.getArbetsplatskod() == null ) {
				validationErrors.add("No Arbetsplatskod element found!");	
				throw new Exception();
	        }
	        II inArbetsplatskod = inEnhet.getArbetsplatskod();
	        // Fält 17 arbetsplatskod id            
	        if (inArbetsplatskod.getExtension() == null || 
	        	inArbetsplatskod.getExtension().length() < 1) {
	            validationErrors.add("Arbetsplatskod for enhet not found!");	            	            	
	        }
	        // Fält 17 arbetsplatskod o.i.d.
	        if (inArbetsplatskod.getRoot() == null || 
	            !inArbetsplatskod.getRoot().equalsIgnoreCase("1.2.752.29.4.71") ) {
				validationErrors.add("Wrong o.i.d. for arbetsplatskod! Should be 1.2.752.29.4.71");								
	        }
       
			// Check if we got any validation errors that not caused an Exception
			if (validationErrors.size() > 0) {
				throw new Exception();
			} 
			
			// No validation errors! Return OK!            
			outResCall.setResultCode(ResultCodeEnum.OK);
			outResponse.setResult(outResCall);
			logger.debug("Returned OK for validating!");

			return outResponse;
		} catch (Exception e) {
			logger.debug("Exception for validating! Errors = " + getValidationErrors(validationErrors));
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

	private AktivitetType findAktivitetWithCode(List<AktivitetType> aktiviteter, Aktivitetskod aktivitetskod) {
		AktivitetType foundAktivitet = null;
		if (aktiviteter != null) {
			for (int i = 0; i< aktiviteter.size(); i++) {
				AktivitetType listAktivitet = (AktivitetType)aktiviteter.get(i);
				if (listAktivitet.getAktivitetskod() != null && listAktivitet.getAktivitetskod().compareTo(aktivitetskod) == 0) {
					foundAktivitet = listAktivitet;
					break;
				}
			}	
		}
		return foundAktivitet;
	}
	
	private FunktionstillstandType findFunktionsTillstandType(List<FunktionstillstandType> funktionstillstand, TypAvFunktionstillstand funktionstillstandsTyp) {
		FunktionstillstandType foundFunktionstillstand = null;
		if (funktionstillstand != null) {
			for (int i = 0; i< funktionstillstand.size(); i++) {
				FunktionstillstandType listFunktionstillstand = (FunktionstillstandType)funktionstillstand.get(i);
				if (listFunktionstillstand.getTypAvFunktionstillstand() != null && listFunktionstillstand.getTypAvFunktionstillstand().compareTo(funktionstillstandsTyp) == 0) {
					foundFunktionstillstand = listFunktionstillstand;
					break;
				}
			}		
		}
		return foundFunktionstillstand;
	}

	private VardkontaktType findVardkontaktTyp(List<VardkontaktType> vardkontakter, Vardkontakttyp vardkontaktTyp) {
		VardkontaktType foundVardkontaktType = null;
		if (vardkontakter != null) {
			for (int i = 0; i< vardkontakter.size(); i++) {
				VardkontaktType listVardkontakter = (VardkontaktType)vardkontakter.get(i);
				if (listVardkontakter.getVardkontakttyp() != null && listVardkontakter.getVardkontakttyp().compareTo(vardkontaktTyp) == 0) {
					foundVardkontaktType = listVardkontakter;
					break;
				}
			}
		}
		return foundVardkontaktType;
	}
	
	private ReferensType findReferensTyp(List<ReferensType> referenser, Referenstyp referensTyp) {
		ReferensType foundReferensType = null;
		if (referenser != null) {
			for (int i = 0; i< referenser.size(); i++) {
				ReferensType listReferenser = (ReferensType)referenser.get(i);
				if (listReferenser.getReferenstyp() != null && listReferenser.getReferenstyp().compareTo(referensTyp) == 0) {
					foundReferensType = listReferenser;
					break;
				}
			}	
		}
		return foundReferensType;
	}	

	private SysselsattningType findTypAvSysselsattning(List<SysselsattningType> sysselsattning, TypAvSysselsattning sysselsattningsTyp) {
		SysselsattningType foundSysselsattningType = null;
		if (sysselsattning != null) {
			for (int i = 0; i< sysselsattning.size(); i++) {
				SysselsattningType listSysselsattning = (SysselsattningType)sysselsattning.get(i);
				if (listSysselsattning.getTypAvSysselsattning() != null && listSysselsattning.getTypAvSysselsattning().compareTo(sysselsattningsTyp) == 0) {
					foundSysselsattningType = listSysselsattning;
					break;
				}
			}
		}
		return foundSysselsattningType;
	}	

	private ArbetsformagaNedsattningType findArbetsformaga(List<ArbetsformagaNedsattningType> arbetsformaga, se.inera.mu7263.v3.Nedsattningsgrad arbetsformagaNedsattningTyp) {
		ArbetsformagaNedsattningType foundArbetsformagaType = null;
		if (arbetsformaga != null) {
			for (int i = 0; i< arbetsformaga.size(); i++) {
				ArbetsformagaNedsattningType listArbetsformaga = (ArbetsformagaNedsattningType)arbetsformaga.get(i);
				if (listArbetsformaga.getNedsattningsgrad() != null && listArbetsformaga.getNedsattningsgrad().compareTo(arbetsformagaNedsattningTyp) == 0) {
					foundArbetsformagaType = listArbetsformaga;
					break;
				}
			}	
		}
		return foundArbetsformagaType;
	}		
}