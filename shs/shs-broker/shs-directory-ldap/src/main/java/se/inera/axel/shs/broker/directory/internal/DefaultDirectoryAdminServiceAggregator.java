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
