package se.inera.axel.shs.broker.directory.internal.rest;

import se.inera.axel.shs.broker.directory.Address;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.directory.ProductType;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.List;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class OrganizationResource {
    private Organization organization;

    private DirectoryAdminService directoryAdminService;

    public OrganizationResource(Organization organization, DirectoryAdminService directoryAdminService) {
        this.organization = organization;
        this.directoryAdminService = directoryAdminService;
    }

    @GET
    public Organization getOrganization() {
        return organization;
    }

    @DELETE
    public void deleteOrganization() {
        directoryAdminService.deleteActor(organization);
    }

    @GET
    @Path("addresses")
    public List<Address> getAddresses() {
        return directoryAdminService.getAddresses(organization);
    }

    @POST
    @Path("addresses")
    public void updateAddress(Address address) {
        directoryAdminService.saveAddress(organization, address);
    }

    @POST
    @Path("products")
    public void updateProduct(ProductType productType) {
        directoryAdminService.saveProduct(organization, productType);
    }
}
