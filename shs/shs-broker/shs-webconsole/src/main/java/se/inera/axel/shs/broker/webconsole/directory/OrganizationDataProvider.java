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

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.Organization;

import java.util.Iterator;
import java.util.List;

public class OrganizationDataProvider implements IDataProvider<Organization> {

	private static final long serialVersionUID = 1L;
	private DirectoryAdminService directoryService;
	private List<Organization> organizations;

	public OrganizationDataProvider(
			DirectoryAdminService ldapDirectoryService) {
		super();
		this.directoryService = ldapDirectoryService;
	}

	@Override
	public void detach() {
		organizations = null;
	}

	@Override
	public Iterator<Organization> iterator(int first, int count) {
		if (organizations == null) {
			organizations = directoryService.getOrganizations();
		}
		return organizations.subList(first, first + count).iterator();
	}

	@Override
	public int size() {
		if (organizations == null) {
			organizations = directoryService.getOrganizations();
		}
		return organizations.size();
	}

	@Override
	public IModel<Organization> model(Organization organization) {
		return new CompoundPropertyModel<Organization>(organization);
	}

}
