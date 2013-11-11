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
