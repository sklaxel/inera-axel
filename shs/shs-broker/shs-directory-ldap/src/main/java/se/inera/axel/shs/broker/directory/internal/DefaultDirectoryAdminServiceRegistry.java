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
