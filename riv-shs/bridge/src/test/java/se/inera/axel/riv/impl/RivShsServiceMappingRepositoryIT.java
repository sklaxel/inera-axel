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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import javax.annotation.Resource;

import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.inera.axel.riv.RivShsServiceMapping;
import se.inera.axel.riv.RivShsServiceMappingRepository;

@ContextConfiguration("classpath:RivShsBridgeTest-context.xml")
public class RivShsServiceMappingRepositoryIT extends
        AbstractTestNGSpringContextTests {

    @Resource(name = "rivShsServiceMappingRepository")
    RivShsServiceMappingRepository repository;

    @Resource
    CacheManager cacheManager;

    final static String TEST_RIV_ENDPOINT = "http://localhost:1919/riv/test";
    final static String TEST_RIV_ENDPOINT_2 = "http://XXX";
    final static String TEST_RIV_NAMESPACE = "urn:riv:test:1";
    final static String TEST_SHS_PRODUCT = "071d294f-ad2e-4089-86bd-055991abc17f";
    final static String TEST_SHS_PRODUCT_2 = "XXX";

    private RivShsServiceMapping testMapping;

    @BeforeMethod
    public void beforeMethod() {
        // Make sure that this object is correct for every testcase
        testMapping = new RivShsServiceMapping();
        testMapping.setRivServiceEndpoint(TEST_RIV_ENDPOINT);
        testMapping.setRivServiceNamespace(TEST_RIV_NAMESPACE);
        testMapping.setShsProductId(TEST_SHS_PRODUCT);
    }

    private void clearCache() {
        cacheManager.getCache("riv-shs-mapping").clear();
    }

    @AfterMethod
    public void afterMethod() {
        repository.deleteAll();
        clearCache();
    }

    @Test
    public void testMappingCrud() throws Exception {
        // Find mapping
        RivShsServiceMapping savedEntity = repository
                .findByRivServiceNamespace(TEST_RIV_NAMESPACE);
        assertNull(savedEntity);

        // Create mapping
        savedEntity = repository.save(testMapping);
        assertNotNull(savedEntity);
        assertNotNull(savedEntity.getId());

        // findone()
        savedEntity = repository.findOne(savedEntity.getId());
        assertNotNull(savedEntity);

        // Update mapping
        savedEntity.setRivServiceEndpoint("http://kalle");
        savedEntity = repository.save(savedEntity);
        assertNotEquals(savedEntity.getRivServiceEndpoint(), TEST_RIV_ENDPOINT);
        assertEquals(savedEntity.getRivServiceEndpoint(), "http://kalle");

        // Find mapping
        clearCache();
        savedEntity = repository.findByRivServiceNamespace(TEST_RIV_NAMESPACE);
        assertNotNull(savedEntity);

        // Find mapping
        clearCache();
        savedEntity = repository.findByShsProductId(TEST_RIV_NAMESPACE);
        assertNull(savedEntity);

        // Find mapping
        clearCache();
        savedEntity = repository.findByShsProductId(TEST_SHS_PRODUCT);
        assertNotNull(savedEntity);

        // Delete mapping
        repository.delete(testMapping);

        // Find mapping
        clearCache();
        savedEntity = repository.findByRivServiceNamespace(TEST_RIV_NAMESPACE);
        assertNull(savedEntity);
    }

    @Test
    public void insertDuplicatesTest() throws Exception {

        // Create mapping
        RivShsServiceMapping savedEntity = repository.save(testMapping);
        assertNotNull(savedEntity);
        assertNotNull(savedEntity.getId());

        try {
            // Create duplicate mapping
            RivShsServiceMapping mapping = new RivShsServiceMapping();
            mapping.setRivServiceEndpoint("http://localhost");
            mapping.setRivServiceNamespace("urn:riv:test:2");
            mapping.setShsProductId(TEST_SHS_PRODUCT);

            repository.save(mapping);
            fail("Inserting duplicate product ids should not be allowed");
        } catch (Exception e) {

        }

        try {
            // Create duplicate mapping
            RivShsServiceMapping mapping = new RivShsServiceMapping();

            mapping.setRivServiceEndpoint("http://localhost");
            mapping.setRivServiceNamespace(TEST_RIV_NAMESPACE);
            mapping.setShsProductId("1a2a40a5-8872-43b2-9e08-e85c5bd60e0d");

            repository.save(mapping);
            fail("Inserting duplicate riv namespaces should not be allowed");
        } catch (Exception e) {

        }

        // Cleanup
        repository.delete(testMapping);
    }

    @Test
    public void shouldCacheMethodFindByRivServiceNamespace() {
        // Create mapping
        RivShsServiceMapping createdMapping1 = repository.save(testMapping);
        assertThat(createdMapping1, notNullValue());

        // Find mapping
        RivShsServiceMapping foundMapping1 = repository
                .findByRivServiceNamespace(TEST_RIV_NAMESPACE);
        assertThat(foundMapping1, notNullValue());
        assertThat(foundMapping1.getShsProductId(), equalTo(TEST_SHS_PRODUCT));

        // Update mapping
        createdMapping1.setShsProductId(TEST_SHS_PRODUCT_2);
        RivShsServiceMapping createdMapping2 = repository.save(createdMapping1);
        assertThat(createdMapping2, notNullValue());

        // Find mapping
        RivShsServiceMapping foundMapping2 = repository
                .findByRivServiceNamespace(TEST_RIV_NAMESPACE);
        assertThat(foundMapping2, notNullValue());
        assertThat(foundMapping2.getShsProductId(), equalTo(TEST_SHS_PRODUCT));

        // Find mapping after clearing the cache
        clearCache();
        RivShsServiceMapping foundMapping3 = repository
                .findByRivServiceNamespace(TEST_RIV_NAMESPACE);
        assertThat(foundMapping3, notNullValue());
        assertThat(foundMapping3.getShsProductId(), equalTo(TEST_SHS_PRODUCT_2));
    }

    @Test
    public void shouldCacheMethodFindByRivServiceNamespaceEvenWhenNull() {
        // Find mapping
        RivShsServiceMapping foundMapping1 = repository
                .findByRivServiceNamespace(TEST_RIV_NAMESPACE);
        assertThat(foundMapping1, nullValue());

        // Create mapping
        RivShsServiceMapping createdMapping1 = repository.save(testMapping);
        assertThat(createdMapping1, notNullValue());

        // Find mapping
        // Should still not find anything because the fact that it did not exist
        // during
        // the first access attempt is stored in the cache
        RivShsServiceMapping foundMapping2 = repository
                .findByRivServiceNamespace(TEST_RIV_NAMESPACE);
        assertThat(foundMapping2, nullValue());

        // Find mapping after clearing the cache
        clearCache();
        RivShsServiceMapping foundMapping3 = repository
                .findByRivServiceNamespace(TEST_RIV_NAMESPACE);
        assertThat(foundMapping3, notNullValue());
    }

    @Test
    public void shouldCacheMethodFindByShsProductId() {
        // Create mapping
        RivShsServiceMapping createdMapping = repository.save(testMapping);
        assertThat(createdMapping, notNullValue());

        // Find mapping
        RivShsServiceMapping foundMapping = repository
                .findByShsProductId(TEST_SHS_PRODUCT);
        assertThat(foundMapping.getRivServiceEndpoint(),
                equalTo(TEST_RIV_ENDPOINT));

        // Update mapping
        createdMapping.setRivServiceEndpoint(TEST_RIV_ENDPOINT_2);
        repository.save(createdMapping);

        // Find mapping
        foundMapping = repository.findByShsProductId(TEST_SHS_PRODUCT);
        assertThat(foundMapping.getRivServiceEndpoint(),
                equalTo(TEST_RIV_ENDPOINT));

        // Find mapping
        clearCache();
        foundMapping = repository.findByShsProductId(TEST_SHS_PRODUCT);
        assertThat(foundMapping.getRivServiceEndpoint(),
                equalTo(TEST_RIV_ENDPOINT_2));
    }
}
