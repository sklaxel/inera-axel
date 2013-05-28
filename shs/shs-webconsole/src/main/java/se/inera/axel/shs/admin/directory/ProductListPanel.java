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
import se.inera.axel.shs.directory.DirectoryAdminService;
import se.inera.axel.shs.directory.Organization;
import se.inera.axel.shs.directory.ProductType;

public class ProductListPanel extends Panel {

	@PaxWicketBean(name = "ldapDirectoryService")
	DirectoryAdminService ldapDirectoryService;

	IDataProvider<ProductType> listData;

	public ProductListPanel(String id, PageParameters params) {
		super(id);
		final String orgNumber = params.get("orgNumber").toString();
		if (StringUtils.isNotBlank(orgNumber)) {

			add(new BookmarkablePageLink<String>("add", ActorEditPage.class,
					new PageParameters().add("type", "product").add(
							"orgNumber", orgNumber)));

			final Organization organization = ldapDirectoryService.getOrganization(orgNumber);

			listData = new ProductTypeDataProvider(ldapDirectoryService, organization);

			DataView<ProductType> dataView = new DataView<ProductType>("list",
					listData) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(final Item<ProductType> item) {
					item.setModel(new CompoundPropertyModel<ProductType>(item
							.getModel()));

					String productId = item.getModelObject().getSerialNumber();

					item.add(labelWithLink("productName", orgNumber, productId));
					item.add(labelWithLink("serialNumber", orgNumber, productId));
					item.add(labelWithLink("description", orgNumber, productId));

					item.add(new Link<Void>("delete") {
						@Override
						public void onClick() {
							ldapDirectoryService.removeProduct(organization,
									item.getModelObject());
						}

						private static final long serialVersionUID = 1L;
					});
				}
			};
			add(dataView);

			dataView.setItemsPerPage(10);
			PagingNavigator pagingNavigator = new PagingNavigator(
					"productNavigator", dataView);
			pagingNavigator.setVisibilityAllowed(listData.size() > 10);
			add(pagingNavigator);
		}
	}

	protected Component labelWithLink(String labelId, String orgNumber,
			String productId) {
		PageParameters params = new PageParameters();
		params.add("type", "product");
		params.add("productId", productId);
		params.add("orgNumber", orgNumber);
		Link<Void> link = new BookmarkablePageLink<Void>(labelId + ".link",
				ActorEditPage.class, params);
		link.add(new Label(labelId));
		return link;
	}

	private static final long serialVersionUID = 1L;

}
