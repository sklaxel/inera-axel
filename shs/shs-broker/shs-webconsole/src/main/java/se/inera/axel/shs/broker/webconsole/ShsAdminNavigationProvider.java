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
package se.inera.axel.shs.broker.webconsole;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import se.inera.axel.shs.broker.webconsole.agreement.AgreementPage;
import se.inera.axel.shs.broker.webconsole.directory.DirectoryPage;
import se.inera.axel.shs.broker.webconsole.product.ProductPage;
import se.inera.axel.webconsole.NavigationProvider;

import java.util.ArrayList;
import java.util.List;

public class ShsAdminNavigationProvider implements NavigationProvider {


	@Override
	public List<Link<Page>> getItems(String componentId, String labelId) {
		List<Link<Page>> links = new ArrayList<Link<Page>>();

		links.add(createPageLink(componentId, labelId, AgreementPage.class, "Agreements"));
		links.add(createPageLink(componentId, labelId, ProductPage.class, "Products"));
		links.add(createPageLink(componentId, labelId, DirectoryPage.class, "Directory"));

		return links;
	}


	private BookmarkablePageLink createPageLink(String componentId, String labelId, Class<? extends Page> pageClass, String name) {
		BookmarkablePageLink link = new BookmarkablePageLink(componentId, pageClass);
		link.add(new Label(labelId, name));

		return link;
	}

}
