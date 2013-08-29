package se.inera.axel.shs.broker.directory;

import java.util.List;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public interface DirectoryAdminServiceAggregator {
    List<Organization> getOrganizations();

    List<ProductType> getProductTypes(Organization organization);

    List<Agreement> getAgreements(Organization organization);

    List<Address> getAddresses(Organization organization);
}
