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
package se.inera.axel.riv.fk.vardgivare.sjukvard.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.wsaddressing10.AttributedURIType;
import se.fk.vardgivare.sjukvard.taemotsvar.v1.rivtabp20.TaEmotSvarResponderInterface;
import se.fk.vardgivare.sjukvard.taemotsvarresponder.v1.TaEmotSvarResponseType;
import se.fk.vardgivare.sjukvard.taemotsvarresponder.v1.TaEmotSvarType;

import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebService(
		serviceName = "TaEmotSvarResponderService", 
		endpointInterface="se.fk.vardgivare.sjukvard.taemotsvar.v1.rivtabp20.TaEmotSvarResponderInterface",
		targetNamespace = "urn:riv:fk:vardgivare:sjukvard:TaEmotSvar:1:rivtabp20")
public class TaEmotSvarImpl implements TaEmotSvarResponderInterface {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	static Map<String, List<String>> answerMapFK = new HashMap<String, List<String>>(); 
	static Map<String, List<String>> answerMapVard = new HashMap<String, List<String>>(); 
	

	public TaEmotSvarResponseType taEmotSvar(
			AttributedURIType logicalAddress, TaEmotSvarType parameters) {
		try {
			TaEmotSvarResponseType response = new TaEmotSvarResponseType();

			// Transform payload to xml string
            StringWriter writer = new StringWriter();
        	Marshaller marshaller = JAXBContext.newInstance(TaEmotSvarType.class).createMarshaller();
        	marshaller.marshal(new JAXBElement(new QName("urn:riv:fk:vardgivare:sjukvard:TaEmotSvarResponder:1", "TaEmotSvarSvar"), TaEmotSvarType.class, parameters), writer);
			String payload = (String)writer.toString();
			logger.debug("Payload: " + payload);
			
			String vardenhetHsaId = null;
			boolean isFromFK = false;

			// Store answer in a map with an array with vårdenhet HSA-id as the key. Answers can come from both directions so add this behaviour
			if (parameters.getFKSKLTaEmotSvarAnrop().getAdressering().getMottagare().getOrganisation().getEnhet() != null) {
				// Answers from FK
				vardenhetHsaId = parameters.getFKSKLTaEmotSvarAnrop().getAdressering().getMottagare().getOrganisation().getEnhet().getId().getValue();
				isFromFK = true;
			} else {
				// Question from Varden
				vardenhetHsaId = parameters.getFKSKLTaEmotSvarAnrop().getAdressering().getAvsandare().getOrganisation().getEnhet().getId().getValue();
				isFromFK = false;
			}
			
			if (isFromFK) {
				// Create an entry for this hsaid
				if (!answerMapFK.containsKey(vardenhetHsaId)) {
					List<String> questions = new ArrayList<String>();
					answerMapFK.put(vardenhetHsaId, questions);
				}
					
				// Add question for this key
				List<String> questions = answerMapFK.get(vardenhetHsaId);
				questions.add(payload);
				
				// Print out all questions for this id from FK
				for(int i = 0; i < questions.size(); i++) {
					logger.debug("Answers from FK, index:" + i + ". Value: " + questions.get(i));
				}
				
			} else {
				// Create an entry for this hsaid
				if (!answerMapVard.containsKey(vardenhetHsaId)) {
					List<String> questions = new ArrayList<String>();
					answerMapVard.put(vardenhetHsaId, questions);
				}
					
				// Add question for this key
				List<String> questions = answerMapVard.get(vardenhetHsaId);
				questions.add(payload);
				
				// Print out all questions for this id
				for(int i = 0; i < questions.size(); i++) {
					logger.debug("Answers from Varden, index:" + i + ". Value: " + questions.get(i));
				}				
			}

			return response;
		} catch (Exception e) {
            throw new RuntimeException("Error occured: " + e, e);
		}
	}
}
			

