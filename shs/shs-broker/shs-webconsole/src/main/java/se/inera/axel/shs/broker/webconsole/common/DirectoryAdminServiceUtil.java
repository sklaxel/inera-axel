package se.inera.axel.shs.broker.webconsole.common;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.webconsole.WicketApplication;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class DirectoryAdminServiceUtil {
    /**
     * Utility class should not be constructed
     */
    private DirectoryAdminServiceUtil() {

    }

    public static DirectoryAdminService getSelectedDirectoryAdminService(DirectoryAdminServiceRegistry registry) {
        String selectedServer = WicketApplication.getDirectoryServerName();

        if (!registry.getServerNames().contains(selectedServer)) {
            selectedServer = registry.getServerNames().get(0);
        }

        return registry.getDirectoryAdminService(selectedServer);
    }
}
