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
package se.inera.axel.shs.broker.webconsole.agreement;

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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ops4j.pax.wicket.api.PaxWicketBean;

import se.inera.axel.shs.broker.webconsole.base.AdminPageParameters;
import se.inera.axel.shs.broker.agreement.AgreementAdminService;
import se.inera.axel.shs.xml.agreement.ShsAgreement;

public class ListAgreementsPanel extends Panel {
	private static final long serialVersionUID = 1L;

	@PaxWicketBean(name = "agreementService")
    @SpringBean(name = "agreementAdminService")
	AgreementAdminService agreementAdminService;

	IDataProvider<ShsAgreement> listData;

	public ListAgreementsPanel(String id, PageParameters params) {
		super(id);

		String query = params.get("search:q").toString();
		listData = new AgreementAdminServiceDataProvider(agreementAdminService,
				query);

		DataView<ShsAgreement> dataView = new DataView<ShsAgreement>("list",
				listData) {
			private static final long serialVersionUID = 1L;

			protected void populateItem(final Item<ShsAgreement> item) {
				item.setModel(new CompoundPropertyModel<ShsAgreement>(item
						.getModel()));

				String uuid = item.getModelObject().getUuid();
				item.add(labelWithLink("shs.product[0].value", uuid));
				item.add(labelWithLink("shs.principal.commonName", uuid));
				item.add(labelWithLink("shs.customer.commonName", uuid));
				item.add(labelWithLink("shs.product[0].commonName", uuid));

				item.add(new Link<String>("delete") {
					@Override
					public void onClick() {
						agreementAdminService.delete(item.getModelObject());
						setResponsePage(AgreementPage.class);
					}

					private static final long serialVersionUID = 1L;
				});
			}

		};
		add(dataView);

		dataView.setItemsPerPage(20);
		PagingNavigator pagingNavigator = new PagingNavigator(
				"agreementNavigator", dataView);
		pagingNavigator.setVisibilityAllowed(listData.size() > 20);

		add(pagingNavigator);
	}

	protected Component labelWithLink(String labelId, String uuid) {
		PageParameters params = new PageParameters();
		params.add(AdminPageParameters.CURRENT_UUID.toString(), uuid);
		params.add(AdminPageParameters.EDIT_MODE.toString(), true);
		Link<String> link = new BookmarkablePageLink<String>(labelId + ".link",
				EditAgreementPage.class, params);
		link.add(new Label(labelId));
		return link;
	}
}
