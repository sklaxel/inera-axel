package se.inera.axel.shs.broker.directory.internal.rest;

import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.directory.Organization;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
@Path("/")
@Produces(MediaType.APPLICATION_XML)
public class DirectoryResource {
    private DirectoryAdminServiceRegistry directoryAdminServiceRegistry;

    public void setDirectoryAdminServiceRegistry(DirectoryAdminServiceRegistry directoryAdminServiceRegistry) {
        this.directoryAdminServiceRegistry = directoryAdminServiceRegistry;
    }

    @GET
    @Path("servers")
    public List<DirectoryServer> getServerNames() {
        List<DirectoryServer> servers = new ArrayList<DirectoryServer>();

        for (String serverName: directoryAdminServiceRegistry.getServerNames()) {
            DirectoryServer directoryServer = new DirectoryServer(serverName);
            servers.add(directoryServer);
        }

        return servers;
    }

    @GET
    @Path("server/{serverName}/organizations")
    public List<Organization> getOrganizations(@PathParam("serverName") String serverName) {
        DirectoryAdminService directoryAdminService = directoryAdminServiceRegistry.getDirectoryAdminService(serverName);

        if (directoryAdminService == null) {
            return null;
        }

        return directoryAdminService.getOrganizations();
    }

    @POST
    @Path("server/{serverName}/organizations")
    public Response updateOrganization(@PathParam("serverName") String serverName,
                                   Organization organization) {
        DirectoryAdminService directoryAdminService = directoryAdminServiceRegistry.getDirectoryAdminService(serverName);

        if (directoryAdminService == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        directoryAdminService.saveActor(organization);

        return Response.ok()
                .location(UriBuilder.fromPath("server/{serverName}/organization/{orgNumber}")
                        .build(serverName, organization.getOrgNumber())).build();
    }

    @Path("server/{serverName}/organization/{orgNumber}")
    public OrganizationResource getOrganizationResource(@PathParam("serverName") String serverName,
                                                        @PathParam("orgNumber") String orgNumber) {
        DirectoryAdminService directoryAdminService = directoryAdminServiceRegistry.getDirectoryAdminService(serverName);

        if (directoryAdminService == null) {
            return null;
        }

        Organization organization = directoryAdminService.getOrganization(orgNumber);

        if (organization == null) {
            return null;
        }

        return new OrganizationResource(organization, directoryAdminService);
    }

}
