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
package se.inera.axel.shs.broker.webconsole.directory;

import org.apache.wicket.Session;
import org.apache.wicket.model.IModel;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.webconsole.WicketApplication;

import java.util.List;

/**
* @author Jan Hallonstén, jan.hallonsten@r2m.se
*/
class DirectoryServerNameModel implements IModel<String> {
    private DirectoryAdminServiceRegistry directoryAdminServiceRegistry;

    public DirectoryServerNameModel(DirectoryAdminServiceRegistry directoryAdminServiceRegistry) {
        this.directoryAdminServiceRegistry = directoryAdminServiceRegistry;
    }

    @Override
    public String getObject() {

        String directoryServerName = Session.get().getMetaData(WicketApplication.DIRECTORY_SERVER_NAME_KEY);
        List<String> serverNames = directoryAdminServiceRegistry.getServerNames();
        if (!serverNames.contains(directoryServerName)) {
            directoryServerName = serverNames.get(0);
        }

        return directoryServerName;
    }

    @Override
    public void setObject(String object) {
        // TODO validation?
        Session.get().setMetaData(WicketApplication.DIRECTORY_SERVER_NAME_KEY, object);
    }

    @Override
    public void detach() {
    }
}
