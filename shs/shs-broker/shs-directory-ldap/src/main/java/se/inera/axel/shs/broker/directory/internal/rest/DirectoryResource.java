package se.inera.axel.shs.broker.directory.internal.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryResource.class);

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
    @Path("servers/{serverName}/organizations")
    public List<Organization> getOrganizations(@PathParam("serverName") String serverName) {
        DirectoryAdminService directoryAdminService = getDirectoryAdminService(serverName);

        if (directoryAdminService == null) {
            throw new NotFoundException();
        }

        return directoryAdminService.getOrganizations();
    }

    @POST
    @Path("servers/{serverName}/organizations")
    public Response updateOrganization(@PathParam("serverName") String serverName,
                                   Organization organization) {
        DirectoryAdminService directoryAdminService = getDirectoryAdminService(serverName);

        if (directoryAdminService == null) {
            throw new NotFoundException();
        }

        directoryAdminService.saveActor(organization);

        return Response.ok()
                .location(UriBuilder.fromPath("server/{serverName}/organization/{orgNumber}")
                        .build(serverName, organization.getOrgNumber())).build();
    }

    @Path("server/{server}/organizations/{orgNumber}")
    public OrganizationResource getOrganizationResource(@PathParam("server") String server,
                                                        @PathParam("orgNumber") String orgNumber) {



        DirectoryAdminService directoryAdminService = getDirectoryAdminService(server);

        if (directoryAdminService == null) {
            throw new NotFoundException();
        }

        Organization organization = directoryAdminService.getOrganization(orgNumber);

        if (organization == null) {
            throw new NotFoundException();
        }

        return new OrganizationResource(organization, directoryAdminService);
    }

    /**
     * Gets the DirectoryAdminService for the supplied server.
     *
     * <p>If the supplied server name does not equal any of the configured servers. It is treated as a 0 based
     * index. So if server == "0" the first directory server is used.</p>
     *
     * @param server the name of the server to use or the servers index.
     *
     * @return the DirectoryAdminService instance that match server.
     *
     * @throws NotFoundException if the server is not found.
     */
    private DirectoryAdminService getDirectoryAdminService(String server) {
        DirectoryAdminService directoryAdminService = directoryAdminServiceRegistry.getDirectoryAdminService(server);

        if (directoryAdminService != null) {
            return directoryAdminService;
        }

        int serverNumber = -1;

        try {
            serverNumber = Integer.parseInt(server);
        } catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        if (serverNumber < 0 || serverNumber >= directoryAdminServiceRegistry.getServerNames().size()) {
            throw new NotFoundException();
        }

        return directoryAdminServiceRegistry.getDirectoryAdminService(directoryAdminServiceRegistry.getServerNames().get(serverNumber));
    }

}
