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

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.webconsole.base.BasePage;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * List LDAP Directory
 */
@PaxWicketMountPoint(mountPoint = "/shs/directory")
public class DirectoryPage extends BasePage {
	private static final long serialVersionUID = 1L;

    @Inject
    @Named("directoryAdminServiceRegistry")
    @SpringBean(name = "directoryAdminServiceRegistry")
    protected DirectoryAdminServiceRegistry directoryAdminServiceRegistry;

    private DirectoryServerNameModel directoryServerNameModel;

	public DirectoryPage(final PageParameters parameters) {
		super(parameters);

        directoryServerNameModel = new DirectoryServerNameModel(directoryAdminServiceRegistry);

        DropDownChoice<String> directoryServers = new DropDownChoice<String>("directoryServer",
                directoryServerNameModel,
                directoryAdminServiceRegistry.getServerNames()) {
            @Override
            protected boolean wantOnSelectionChangedNotifications() {
                return true;
            }
        };

        add(directoryServers);

		add(new ListDirectoryPanel("list"));

	}

}
