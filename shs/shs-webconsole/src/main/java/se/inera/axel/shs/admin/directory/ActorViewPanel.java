/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.admin.directory;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import se.inera.axel.shs.directory.DirectoryService;
import se.inera.axel.shs.directory.Organization;

public class ActorViewPanel extends Panel {

	@PaxWicketBean(name = "ldapDirectoryService")
	DirectoryService ldapDirectoryService;

	public ActorViewPanel(String id, PageParameters params) {
		super(id);

		String orgNumber = params.get("orgNumber").toString();
		if (StringUtils.isNotBlank(orgNumber)) {
			add(new BookmarkablePageLink<Void>("back", DirectoryPage.class));

			Organization organization = ldapDirectoryService.getOrganization(orgNumber);
			// CompoundPropertyModel<Organization> actorModel = new
			// CompoundPropertyModel<Organization>(
			// organization);
			// TODO Use model
			add(new Label("orgName", organization.getOrgName()));
			add(new Label("streetAddress", organization.getStreetAddress()));
			add(new Label("postalCode", organization.getPostalCode()));
			add(new Label("postalAddress", organization.getPostalAddress()));
			add(new Label("postOfficeBox", organization.getPostOfficeBox()));
			add(new Label("orgNumber", organization.getOrgNumber()));
			add(new Label("description", organization.getDescription()));
			add(new Label("phoneNumber", organization.getPhoneNumber()));
			add(new Label("faxNumber", organization.getFaxNumber()));
			add(new Label("labeledUri", organization.getLabeledUri()));

			PageParameters editParams = new PageParameters();
			editParams.add("type", "organization");
			editParams.add("orgNumber", orgNumber);
			add(new BookmarkablePageLink<Void>("edit", ActorEditPage.class,
					editParams));
		} else {
			throw new RuntimeException("Do something else here");
		}

	}

	private static final long serialVersionUID = 1L;

}
