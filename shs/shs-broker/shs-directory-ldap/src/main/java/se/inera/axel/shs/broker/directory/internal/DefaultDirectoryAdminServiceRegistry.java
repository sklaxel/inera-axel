package se.inera.axel.shs.broker.directory.internal;

import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceAggregator;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry from which all the configured DirectoryAdminService:s can be retrieved.
 *
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class DefaultDirectoryAdminServiceRegistry implements DirectoryAdminServiceRegistry {
    private Map<String, DirectoryAdminService> directoryAdminServices;
    private DirectoryAdminServiceAggregator directoryAdminServiceAggregator;

    public DefaultDirectoryAdminServiceRegistry(Map<String, DirectoryAdminService> directoryServices) {
        directoryAdminServices = new LinkedHashMap<String, DirectoryAdminService>(directoryServices);
        directoryAdminServiceAggregator = new DefaultDirectoryAdminServiceAggregator(
                new ArrayList<DirectoryAdminService>(directoryServices.values()));
    }

    @Override
    public DirectoryAdminService getDirectoryAdminService(String serverName) {
        return directoryAdminServices.get(serverName);
    }

    @Override
    public DirectoryAdminServiceAggregator getDirectoryAdminServiceAggregator() {
        return directoryAdminServiceAggregator;
    }

    @Override
    public List<String> getServerNames() {
        return new ArrayList<String>(directoryAdminServices.keySet());
    }
}
