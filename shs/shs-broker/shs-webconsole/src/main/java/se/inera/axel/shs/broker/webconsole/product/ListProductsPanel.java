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
package se.inera.axel.shs.broker.webconsole.product;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.ops4j.pax.wicket.api.PaxWicketBean;

import se.inera.axel.shs.broker.webconsole.base.AdminPageParameters;
import se.inera.axel.shs.broker.product.ProductAdminService;
import se.inera.axel.shs.xml.product.ShsProduct;

/**
 * List SHS Products
 */
public class ListProductsPanel extends Panel {
	private static final long serialVersionUID = 1L;

	@PaxWicketBean(name = "productService")
	ProductAdminService productAdminService;

	IDataProvider<ShsProduct> listData;

	public ListProductsPanel(String id, PageParameters params) {
		super(id);

		String query = params.get("search:q").toString();
		listData = new ProductAdminServiceDataProvider(productAdminService,
				query);

		DataView<ShsProduct> dataView = new DataView<ShsProduct>("list",
				listData) {
			private static final long serialVersionUID = 1L;

			protected void populateItem(final Item<ShsProduct> item) {
				item.setModel(new CompoundPropertyModel<ShsProduct>(item
						.getModel()));

				String uuid = item.getModelObject().getUuid();
				item.add(labelWithLink("commonName", uuid));
				item.add(labelWithLink("uuid", uuid));
				item.add(labelWithLink("respRequired", uuid));
				item.add(labelWithLink("principal.commonName", uuid));

				item.add(new Link<String>("delete") {
					@Override
					public void onClick() {
						productAdminService.delete(item.getModelObject());
						setResponsePage(ProductPage.class);
					}

					private static final long serialVersionUID = 1L;
				});
			}

		};
		add(dataView);

		dataView.setItemsPerPage(20);
		PagingNavigator pagingNavigator = new PagingNavigator(
				"productNavigator", dataView);
		pagingNavigator.setVisibilityAllowed(listData.size() > 20);
		add(pagingNavigator);
	}

	protected Component labelWithLink(String labelId, String uuid) {
		PageParameters params = new PageParameters();
		params.add(AdminPageParameters.CURRENT_UUID.toString(), uuid);
		params.add(AdminPageParameters.EDIT_MODE.toString(), true);
		Link<String> link = new BookmarkablePageLink<String>(labelId + ".link",
				EditProductPage.class, params);
		link.add(new Label(labelId));
		return link;
	}
}
