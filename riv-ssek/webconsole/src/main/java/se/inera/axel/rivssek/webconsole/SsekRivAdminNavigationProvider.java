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
package se.inera.axel.rivssek.webconsole;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import se.inera.axel.rivssek.webconsole.SsekServiceInfoPage;
import se.inera.axel.webconsole.NavigationProvider;

import java.util.ArrayList;
import java.util.List;

public class SsekRivAdminNavigationProvider implements NavigationProvider {


	@Override
	public List<Link<Page>> getItems(String componentId, String labelId) {
		List<Link<Page>> links = new ArrayList<>();

		links.add(createPageLink(componentId, labelId, SsekServiceInfoPage.class, "RIV/SSEK"));


		return links;
	}


	private BookmarkablePageLink createPageLink(String componentId, String labelId, Class<? extends Page> pageClass, String name) {
		BookmarkablePageLink link = new BookmarkablePageLink(componentId, pageClass);
		link.add(new Label(labelId, name));

		return link;
	}

}
