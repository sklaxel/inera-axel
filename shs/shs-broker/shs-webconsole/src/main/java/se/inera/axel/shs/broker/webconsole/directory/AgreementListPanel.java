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
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import se.inera.axel.shs.broker.directory.Agreement;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.webconsole.common.DirectoryAdminServiceUtil;

import javax.inject.Inject;
import javax.inject.Named;

public class AgreementListPanel extends Panel {

    @Inject
    @Named("directoryAdminServiceRegistry")
    @SpringBean(name = "directoryAdminServiceRegistry")
    DirectoryAdminServiceRegistry directoryAdminServiceRegistry;

	IDataProvider<Agreement> listData;

	public AgreementListPanel(String id, final IModel<Organization> model) {
		super(id, model);

        final String organizationNumber = model.getObject().getOrgNumber();

        add(new BookmarkablePageLink<String>("add", ActorEditPage.class,
                new PageParameters().add("type", "agreement").add(
                        "orgno", organizationNumber)));

        listData = new AgreementDataProvider(model.getObject());

        DataView<Agreement> dataView = new DataView<Agreement>("list",
                listData) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(final Item<Agreement> item) {
                item.setModel(new CompoundPropertyModel<Agreement>(item
                        .getModel()));
                String serialNumber = item.getModelObject().getSerialNumber();
                item.add(labelWithLink("productId", organizationNumber,
                        serialNumber));
                item.add(labelWithLink("productName", organizationNumber,
                        serialNumber));
                item.add(labelWithLink("principal", organizationNumber,
                        serialNumber));

                item.add(new Link<Void>("delete") {
                    @Override
                    public void onClick() {
                        getDirectoryAdminService().removeAgreement(model.getObject(),
                                item.getModelObject());
                    }

                    private static final long serialVersionUID = 1L;
                });
            }
        };
        add(dataView);

        dataView.setItemsPerPage(10);
        PagingNavigator pagingNavigator = new PagingNavigator(
                "agreementsNavigator", dataView);
        pagingNavigator.setVisibilityAllowed(listData.size() > 10);
        add(pagingNavigator);
	}

	protected Component labelWithLink(String labelId, String orgNumber, String serialNumber) {
		PageParameters params = new PageParameters();
		params.add("type", "agreement");
		params.add("serialNumber", serialNumber);
		params.add("orgno", orgNumber);
		Link<Void> link = new BookmarkablePageLink<Void>(labelId + ".link",
				ActorEditPage.class, params);
		link.add(new Label(labelId));
		return link;
	}

    private DirectoryAdminService getDirectoryAdminService() {
        return DirectoryAdminServiceUtil.getSelectedDirectoryAdminService(directoryAdminServiceRegistry);
    }

	private static final long serialVersionUID = 1L;

}
