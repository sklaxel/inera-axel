package se.inera.axel.shs.broker.directory;

import se.inera.axel.shs.broker.directory.DirectoryAdminService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public interface DirectoryAdminServiceRegistry {
    DirectoryAdminService getDirectoryAdminService(String serverName);

    DirectoryAdminServiceAggregator getDirectoryAdminServiceAggregator();

    List<String> getServerNames();
}
