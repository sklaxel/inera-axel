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
import se.fk.vardgivare.sjukvard.taemotlakarintyg.v1.rivtabp20.TaEmotLakarintygResponderInterface;
import se.fk.vardgivare.sjukvard.taemotlakarintygresponder.v1.TaEmotLakarintygResponseType;
import se.fk.vardgivare.sjukvard.taemotlakarintygresponder.v1.TaEmotLakarintygType;

import javax.jws.WebService;

@WebService(serviceName = "TaEmotLakarintygResponderService",
        endpointInterface = "se.fk.vardgivare.sjukvard.taemotlakarintyg.v1.rivtabp20.TaEmotLakarintygResponderInterface",
        targetNamespace = "urn:riv:fk:vardgivare:sjukvard:TaEmotLakarintyg:1:rivtabp20")
public class TaEmotLakarintygImpl implements TaEmotLakarintygResponderInterface {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public TaEmotLakarintygResponseType taEmotLakarintyg(
			org.w3.wsaddressing10.AttributedURIType logicalAddress,
			TaEmotLakarintygType parameters) {

		logger.info("taEmotLakarintyg({}, {})", logicalAddress.getValue(),
				parameters);

		try {
			TaEmotLakarintygResponseType response = new TaEmotLakarintygResponseType();
			logger.info("response sent!");
			return response;
		} catch (RuntimeException e) {
			throw new RuntimeException("Error occured in taEmotLakarintyg: " + e, e);
		}
	}

}