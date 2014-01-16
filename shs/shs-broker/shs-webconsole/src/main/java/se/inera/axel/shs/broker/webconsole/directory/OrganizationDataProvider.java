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

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.wicket.Component;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.webconsole.common.DirectoryAdminServiceUtil;
import se.inera.axel.webconsole.InjectorHelper;

public class OrganizationDataProvider implements IDataProvider<Organization> {

	private static final long serialVersionUID = 1L;
    @Inject
    @Named("directoryAdminServiceRegistry")
    @SpringBean(name = "directoryAdminServiceRegistry")
	private DirectoryAdminServiceRegistry directoryAdminServiceRegistry;

	private List<Organization> organizations;
	private Component feedbackPanel;

	public OrganizationDataProvider(Component feedbackPanel) {
		super();
		this.feedbackPanel = feedbackPanel;
        InjectorHelper.inject(this, getClass().getClassLoader());
	}

	@Override
	public void detach() {
		organizations = null;
	}

	@Override
	public Iterator<Organization> iterator(long first, long count) {
		if (organizations == null) {
			try {
				organizations = getDirectoryAdminService().getOrganizations();
			} catch (Exception e) {
		        feedbackPanel.error(e.getMessage());
		        return null;
			}
		}
		return organizations.subList((int) first, (int) (first + count)).iterator();
	}

    private DirectoryAdminService getDirectoryAdminService() {
        return DirectoryAdminServiceUtil.getSelectedDirectoryAdminService(directoryAdminServiceRegistry);
    }

    @Override
	public long size() {
		if (organizations == null) {
			try {
				organizations = getDirectoryAdminService().getOrganizations();
			} catch (Exception e) {
		        feedbackPanel.error(e.getMessage());
		        return 0;
			}
		}
		return organizations.size();
	}

	@Override
	public IModel<Organization> model(Organization organization) {
		return new CompoundPropertyModel<>(organization);
	}

}
