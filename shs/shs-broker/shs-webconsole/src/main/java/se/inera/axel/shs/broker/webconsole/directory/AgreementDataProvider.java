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

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.directory.Agreement;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;

public class AgreementDataProvider implements IDataProvider<Agreement> {

	private DirectoryAdminService directoryAdminService;
	private Organization organization;
	List<Agreement> agreements;

	public AgreementDataProvider(DirectoryAdminService ldapDirectoryService,
			Organization organization) {
		this.directoryAdminService = ldapDirectoryService;
		this.organization = organization;
	}

	@Override
	public void detach() {
		agreements = null;
	}

	@Override
	public Iterator<Agreement> iterator(int first, int count) {
		if (agreements == null) {
			agreements = directoryAdminService.getAgreements(organization);
		}
		return agreements.subList(first, first + count).iterator();
	}

	@Override
	public int size() {
		if (agreements == null) {
			agreements = directoryAdminService.getAgreements(organization);
		}
		return agreements.size();
	}

	@Override
	public IModel<Agreement> model(Agreement agreement) {
		return new CompoundPropertyModel<Agreement>(agreement);
	}

	private static final long serialVersionUID = 1L;
}
