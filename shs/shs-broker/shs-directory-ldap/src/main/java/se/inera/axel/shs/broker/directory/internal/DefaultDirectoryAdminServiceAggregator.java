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

import se.inera.axel.shs.broker.directory.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class DefaultDirectoryAdminServiceAggregator implements DirectoryAdminServiceAggregator {
    private List<DirectoryAdminService> directoryAdminServices;

    public DefaultDirectoryAdminServiceAggregator(List<DirectoryAdminService> directoryAdminServices) {
        this.directoryAdminServices = new ArrayList<DirectoryAdminService>(directoryAdminServices);
    }

    @Override
    public List<Organization> getOrganizations() {
        Map<String, Organization> organizations = new LinkedHashMap<String, Organization>();

        for (DirectoryAdminService directoryAdminService : directoryAdminServices) {
            for (Organization organization : directoryAdminService.getOrganizations()) {
                if (!organizations.containsKey(organization.getOrgNumber())) {
                    organizations.put(organization.getOrgNumber(), organization);
                }
            }
        }

        return new ArrayList<Organization>(organizations.values());
    }

    @Override
    public List<ProductType> getProductTypes(Organization organization) {
        Map<String, ProductType> productTypes = new LinkedHashMap<String, ProductType>();

        for (DirectoryAdminService directoryAdminService : directoryAdminServices) {
            for (ProductType productType : directoryAdminService.getProductTypes(organization)) {
                if (!productTypes.containsKey(productType.getSerialNumber())) {
                    productTypes.put(productType.getSerialNumber(), productType);
                }
            }
        }

        return new ArrayList<ProductType>(productTypes.values());
    }

    @Override
    public List<Agreement> getAgreements(Organization organization) {
        Map<String, Agreement> agreements = new LinkedHashMap<String, Agreement>();

        for (DirectoryAdminService directoryAdminService : directoryAdminServices) {
            for (Agreement agreement : directoryAdminService.getAgreements(organization)) {
                if (!agreements.containsKey(agreement.getSerialNumber())) {
                    agreements.put(agreement.getSerialNumber(), agreement);
                }
            }
        }

        return new ArrayList<Agreement>(agreements.values());
    }

    @Override
    public List<Address> getAddresses(Organization organization) {
        Map<String, Address> addresses = new LinkedHashMap<String, Address>();

        for (DirectoryAdminService directoryAdminService : directoryAdminServices) {
            for (Address address : directoryAdminService.getAddresses(organization)) {
                String key = getAddressKey(address);
                if (!addresses.containsKey(key)) {
                    addresses.put(key, address);
                }
            }
        }

        return new ArrayList<Address>(addresses.values());
    }

    private String getAddressKey(Address address) {
        return address.getSerialNumber() + address.getOrganizationNumber();
    }
}
