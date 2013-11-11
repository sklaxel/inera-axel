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
package se.inera.axel.shs.broker.directory.internal;

import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.KeyGenerator;
import se.inera.axel.shs.broker.directory.*;

import java.util.Collections;
import java.util.List;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class DirectoryServiceGateway implements DirectoryService {
    List<DirectoryService> directoryServices;

    public void setDirectoryServices(List<DirectoryService> directoryServices) {
        this.directoryServices = directoryServices;
    }

    @Cacheable(cacheName="ldap", keyGenerator=@KeyGenerator(name = "StringCacheKeyGenerator"))
    @Override
    public Organization getOrganization(String orgNumber) {
        Organization organization = null;

        for(DirectoryService directoryService : directoryServices) {
            organization = directoryService.getOrganization(orgNumber);

            if (organization != null) {
                break;
            }
        }

        return organization;
    }

    @Cacheable(cacheName="ldap", keyGenerator=@KeyGenerator (name = "StringCacheKeyGenerator"))
    @Override
    public Address getAddress(String orgNumber, String productId) {
        Address address = null;

        for(DirectoryService directoryService : directoryServices) {
            address = directoryService.getAddress(orgNumber, productId);

            if (address != null) {
                break;
            }
        }

        return address;
    }

    @Cacheable(cacheName="ldap", keyGenerator=@KeyGenerator (name = "StringCacheKeyGenerator"))
    @Override
    public Agreement getAgreement(String orgNumber, String productId, String transferType) {
        Agreement agreement = null;

        for(DirectoryService directoryService : directoryServices) {
            agreement = directoryService.getAgreement(orgNumber, productId, transferType);

            if (agreement != null) {
                break;
            }
        }

        return agreement;
    }

    @Cacheable(cacheName="ldap", keyGenerator=@KeyGenerator (name = "StringCacheKeyGenerator"))
    @Override
    public List<Agreement> findAgreements(String orgNumber, String productId) {
        for(DirectoryService directoryService : directoryServices) {
            List<Agreement> agreements = directoryService.findAgreements(orgNumber, productId);

            if (agreements.size() > 0) {
                return agreements;
            }
        }

        return Collections.emptyList();
    }

    @Cacheable(cacheName="ldap", keyGenerator=@KeyGenerator (name = "StringCacheKeyGenerator"))
    @Override
    public ProductType getProductType(String orgNumber, String productId) {
        ProductType productType = null;

        for(DirectoryService directoryService : directoryServices) {
            productType = directoryService.getProductType(orgNumber, productId);

            if (productType != null) {
                break;
            }
        }

        return productType;
    }
}
