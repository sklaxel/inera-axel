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
package se.inera.axel.shs.broker.directory.internal.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.broker.directory.Address;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.directory.ProductType;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class OrganizationResource {
    private static final Logger LOG = LoggerFactory.getLogger(OrganizationResource.class);

    private Organization organization;

    private DirectoryAdminService directoryAdminService;

    public OrganizationResource(Organization organization, DirectoryAdminService directoryAdminService) {
        LOG.debug("OrganizationResource created for organization {}", organization);
        this.organization = organization;
        this.directoryAdminService = directoryAdminService;
    }

    @GET
    public Organization getOrganization() {
        return organization;
    }

    @DELETE
    public void deleteOrganization() {
        if (organization == null) {
        }
        directoryAdminService.deleteActor(organization);
    }

    @GET
    @Path("addresses")
    public List<Address> getAddresses() {
        return directoryAdminService.getAddresses(organization);
    }

    @POST
    @Path("addresses")
    public Response updateAddress(Address address, @Context UriInfo uriInfo) {
        directoryAdminService.saveAddress(organization, address);

        return Response.created(
                uriInfo.getAbsolutePathBuilder()
                        .path("{productTypeId}").build(address.getSerialNumber())).build();
    }

    @POST
    @Path("products")
    public void updateProduct(ProductType productType) {
        directoryAdminService.saveProduct(organization, productType);
    }
}
