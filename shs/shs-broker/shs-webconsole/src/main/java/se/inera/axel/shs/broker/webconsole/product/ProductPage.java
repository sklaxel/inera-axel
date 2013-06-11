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
package se.inera.axel.shs.broker.webconsole.product;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;

import se.inera.axel.shs.broker.webconsole.base.BasePage;
import se.inera.axel.shs.broker.webconsole.base.SearchPanel;

/**
 * List SHS Products
 */
@PaxWicketMountPoint(mountPoint = "/shs/products")
public class ProductPage extends BasePage {
	private static final long serialVersionUID = 1L;

	public ProductPage(final PageParameters params) {
		super(params);

		BookmarkablePageLink<Void> addLink = new BookmarkablePageLink<Void>("add",
				EditProductPage.class, new PageParameters());
		BookmarkablePageLink<Void> addxmlLink = new BookmarkablePageLink<Void>("addxml",
				EditProductPage.class, new PageParameters().add("view", "xml"));
		add(new SearchPanel("search", params, addLink, addxmlLink));

		// add product list
		add(new ListProductsPanel("list", params));
	}
}
