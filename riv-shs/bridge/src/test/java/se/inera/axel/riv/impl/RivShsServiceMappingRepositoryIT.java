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
package se.inera.axel.riv.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import javax.annotation.Resource;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import se.inera.axel.riv.RivShsServiceMapping;
import se.inera.axel.riv.RivShsServiceMappingRepository;

@ContextConfiguration("classpath:RivShsBridgeTest-context.xml")
public class RivShsServiceMappingRepositoryIT extends AbstractTestNGSpringContextTests {

	@Resource(name="rivShsServiceMappingRepository")
	RivShsServiceMappingRepository repository;
	
	
	final static String TEST_RIV_ENDPOINT = "http://localhost:1919/riv/test"; 
	final static String TEST_RIV_NAMESPACE = "urn:riv:test:1"; 
	final static String TEST_SHS_PRODUCT = "071d294f-ad2e-4089-86bd-055991abc17f"; 
	
	final RivShsServiceMapping testMapping;
	
	public RivShsServiceMappingRepositoryIT() {
		testMapping = new RivShsServiceMapping();
		
		testMapping.setRivServiceEndpoint(TEST_RIV_ENDPOINT);
		testMapping.setRivServiceNamespace(TEST_RIV_NAMESPACE);
		testMapping.setShsProductId(TEST_SHS_PRODUCT);
	}
	
	
	@Test
	public void testMappingCrud() throws Exception {
		RivShsServiceMapping savedEntity = repository.findByRivServiceNamespace(TEST_RIV_NAMESPACE);
		
		if (savedEntity == null) {
			savedEntity = testMapping;
		}
		
		savedEntity = repository.save(savedEntity);
		
		assertNotNull(savedEntity);
		assertNotNull(savedEntity.getId());	
		
		savedEntity = repository.findOne(savedEntity.getId());
		assertNotNull(savedEntity);
		
		savedEntity.setRivServiceEndpoint("http://kalle");
		savedEntity = repository.save(savedEntity);
		
		assertNotEquals(savedEntity.getRivServiceEndpoint(), TEST_RIV_ENDPOINT);
		assertEquals(savedEntity.getRivServiceEndpoint(), "http://kalle");
		
		savedEntity = repository.findByRivServiceNamespace(TEST_RIV_NAMESPACE);
		assertNotNull(savedEntity);
		

		savedEntity = repository.findByShsProductId(TEST_RIV_NAMESPACE);
		assertNull(savedEntity);
		
		savedEntity = repository.findByShsProductId(TEST_SHS_PRODUCT);
		assertNotNull(savedEntity);
		
		repository.delete(savedEntity);
		
		savedEntity = repository.findByRivServiceNamespace(TEST_RIV_NAMESPACE);
		assertNull(savedEntity);
		
	}

	
	@Test
	public void insertDuplicatesTest() throws Exception {
	
		RivShsServiceMapping savedEntity = repository.save(testMapping);
		
		assertNotNull(savedEntity);
		assertNotNull(savedEntity.getId());	
		
		
		try {
			RivShsServiceMapping mapping = new RivShsServiceMapping();
			
			mapping.setRivServiceEndpoint("http://localhost");
			mapping.setRivServiceNamespace("urn:riv:test:2");
			mapping.setShsProductId(TEST_SHS_PRODUCT);
			
			repository.save(mapping);
			fail("Inserting duplicate product ids should not be allowed");
		} catch (Exception e) {
			
		}
		
		try {
			RivShsServiceMapping mapping = new RivShsServiceMapping();
			
			mapping.setRivServiceEndpoint("http://localhost");
			mapping.setRivServiceNamespace(TEST_RIV_NAMESPACE);
			mapping.setShsProductId("1a2a40a5-8872-43b2-9e08-e85c5bd60e0d");
			
			repository.save(mapping);
			fail("Inserting duplicate riv namespaces should not be allowed");
		} catch (Exception e) {
			
		}
		
	}
	
	
}
