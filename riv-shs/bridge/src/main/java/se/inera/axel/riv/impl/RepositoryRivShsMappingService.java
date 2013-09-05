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
package se.inera.axel.riv.impl;

import org.apache.camel.Header;
import org.apache.camel.Property;
import org.apache.commons.lang.StringUtils;
import se.inera.axel.riv.RivShsMappingService;
import se.inera.axel.riv.RivShsServiceMapping;
import se.inera.axel.riv.RivShsServiceMappingRepository;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.ShsLabel;

import javax.annotation.Resource;


public class RepositoryRivShsMappingService implements RivShsMappingService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RepositoryRivShsMappingService.class);
	
	@Resource
	RivShsServiceMappingRepository repository;
	
	/* (non-Javadoc)
	 * @see se.inera.axel.riv.mongo.RivShsMappingService#mapRivServiceToShsProduct(java.lang.String)
	 */
	@Override
	public String mapRivServiceToShsProduct(@Header(RivShsMappingService.HEADER_SOAP_ACTION) String rivServiceNamespace) {
		
		log.debug("mapRivServiceToShsProduct({})", rivServiceNamespace);
		
		RivShsServiceMapping mapping = findByRivServiceNamespace(StringUtils.remove(rivServiceNamespace, '"'));
		
		if (mapping == null) {
			throw new RuntimeException("No SHS ProductId found for RIV Service " + rivServiceNamespace);
		}
		
		return mapping.getShsProductId();
	}

	@Override
	public String mapRivServiceToRivEndpoint(@Header(RivShsMappingService.HEADER_SOAP_ACTION) String rivServiceNamespace) {
		
		log.debug("mapRivServiceToRivEndpoint({})", rivServiceNamespace);
		
		RivShsServiceMapping mapping = findByRivServiceNamespace(StringUtils.remove(rivServiceNamespace, '"'));
		
		if (mapping == null) {
			throw new RuntimeException("No RIV Endpoint found for RIV Service " + rivServiceNamespace);
		}
		
		return mapping.getRivServiceEndpoint();
	}
	
	@Override
	public String mapShsProductToRivService(@Property(ShsHeaders.LABEL) ShsLabel shsLabel) {
		String productId = shsLabel.getProduct().getValue();
		log.debug("mapShsProductToRivService({})", productId);
		
		RivShsServiceMapping mapping = findByShsProductId(productId);
		
		if (mapping == null) {
			throw new RuntimeException("No RIV Service found for SHS ProductId: " + productId);
		}
		
		return mapping.getRivServiceNamespace();
	}


	private RivShsServiceMapping findByRivServiceNamespace(String rivServiceNamespace) {
		return repository.findByRivServiceNamespace(rivServiceNamespace);
	}

	private RivShsServiceMapping findByShsProductId(String shsProductId) {
		return repository.findByShsProductId(shsProductId);
	}

}
