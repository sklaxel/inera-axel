package se.inera.axel.shs.broker.directory;

import se.inera.axel.shs.broker.directory.DirectoryAdminService;

import java.util.List;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public interface DirectoryAdminServiceRegistry {
    DirectoryAdminService getDirectoryAdminService(String serverName);

    DirectoryAdminServiceAggregator getDirectoryAdminServiceAggregator();

    List<String> getServerNames();
}
