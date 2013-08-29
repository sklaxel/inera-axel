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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.directory.Organization;

public class ListDirectoryPanel extends Panel {
	private static final long serialVersionUID = 1L;

	@PaxWicketBean(name = "directoryAdminServiceRegistry")
    @SpringBean(name = "directoryAdminServiceRegistry")
    DirectoryAdminServiceRegistry directoryAdminServiceRegistry;

	IDataProvider<Organization> listData;

	public ListDirectoryPanel(String id) {
		super(id);

		listData = new OrganizationDataProvider(directoryAdminServiceRegistry);
		DataView<Organization> dataView = new DataView<Organization>("list", listData) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(Item<Organization> item) {
				String orgNumber = item.getModelObject().getOrgNumber();
				item.add(labelWithLink("orgName", orgNumber));
				item.add(labelWithLink("orgNumber", orgNumber));
				item.add(labelWithLink("description", orgNumber));
			}

		};
		add(dataView);

		dataView.setItemsPerPage(20);
		PagingNavigator pagingNavigator = new PagingNavigator(
				"directoryNavigator", dataView);
		pagingNavigator.setVisibilityAllowed(listData.size() > 20);

		add(pagingNavigator);
	}

	protected Component labelWithLink(String labelId, String orgNumber) {
		PageParameters params = new PageParameters();
		params.add("orgNumber", orgNumber);
		Link<Void> link = new BookmarkablePageLink<Void>(labelId + ".link",
				ActorPage.class, params);
		link.add(new Label(labelId));
		return link;
	}
}
