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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import se.inera.axel.shs.broker.directory.Address;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.directory.ProductType;
import se.inera.axel.shs.broker.product.ProductAdminService;
import se.inera.axel.shs.broker.webconsole.common.DirectoryAdminServiceUtil;
import se.inera.axel.shs.xml.product.ShsProduct;
import se.inera.axel.webconsole.NodeInfo;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AddressEditPanel extends Panel {

    @Inject
	@Named("directoryAdminServiceRegistry")
    @SpringBean(name = "directoryAdminServiceRegistry")
    DirectoryAdminServiceRegistry directoryAdminServiceRegistry;

    @Inject
	@Named("productService")
    @SpringBean(name = "productAdminService")
	ProductAdminService productAdminService;

    @Inject
    @Named("nodeInfo")
    @SpringBean(name = "nodeInfo")
    NodeInfo nodeInfo;

	public AddressEditPanel(String id, PageParameters params) {
		super(id);

		add(new FeedbackPanel("feedback"));

		final String productId = params.get("serialNumber").toString();
		final String orgNumber = params.get("orgNumber").toString();

		Address address = null;
		if (isEditMode(productId, orgNumber)) {
			address = getDirectoryAdminService().getAddress(orgNumber, productId);
		} else {
			address = new Address();
			address.setOrganizationNumber(orgNumber);
            if (nodeInfo.getOrganizationNumber().equals(orgNumber)) {
                address.setDeliveryMethods(nodeInfo.getExternalReceiveServiceUrl());
            }
		}

		final IModel<Address> addressModel = new CompoundPropertyModel<Address>(
				address);
		Form<Address> form = new Form<Address>("addressForm", addressModel) {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				Address submittedAddress = getModelObject();
				Organization organization = getDirectoryAdminService().getOrganization(orgNumber);
				getDirectoryAdminService().saveAddress(organization, submittedAddress);

				PageParameters params = new PageParameters();
				params.add("orgNumber", orgNumber);
				setResponsePage(ActorPage.class, params);
			}

			private static final long serialVersionUID = 1L;
		};

		final TextField<String> deliveryMethods = new TextField<String>(
				"deliveryMethods");
		deliveryMethods.setOutputMarkupId(true);
		deliveryMethods.setRequired(true);
		form.add(deliveryMethods);
		form.add(new TextField<String>("organizationNumber")
				.setRequired(true).setEnabled(false));

		Map<String, DropdownProduct> products = getProducts(productId, orgNumber);

		IChoiceRenderer<String> productRenderer = new DropdownProductChoiceRenderer(products);
		DropDownChoice<String> ddcProducts = new DropDownChoice<String>(
				"serialNumber", Model.ofList(new ArrayList(products.keySet())), productRenderer);

        if (isEditMode(productId, orgNumber)) {
            ddcProducts.setEnabled(false);
        }

		ddcProducts.setRequired(true);
		form.add(ddcProducts);

		form.add(new SubmitLink("submit"));
		PageParameters cancelParams = new PageParameters();
		cancelParams.add("orgNumber", orgNumber);
		form.add(new BookmarkablePageLink<Void>("cancel", ActorPage.class,
				cancelParams));

		add(form);
	}

    private boolean isEditMode(String productId, String orgNumber) {
        return StringUtils.isNotBlank(productId)
                && StringUtils.isNotBlank(orgNumber);
    }

    private Map<String, DropdownProduct> getProducts(String productId, String orgNumber) {
        Map<String, DropdownProduct> products;
        if (isEditMode(productId, orgNumber)) {
            products = getSelectedProductAsList(productId, orgNumber);
        } else {
            products = getProducts();
        }
        return products;
    }

    private Map<String, DropdownProduct> getSelectedProductAsList(String productId, String orgNumber) {
        ShsProduct shsProduct = productAdminService.getProduct(productId);

        if (shsProduct != null) {
            Map<String, DropdownProduct> result = new HashMap<>();
            result.put(productId, DropDownProductUtils.createDropdownProduct(shsProduct));
            return result;
        }

        ProductType productType = getDirectoryAdminService().getProductType(orgNumber, productId);

        if (productType != null) {
            Map<String, DropdownProduct> result = new HashMap<>();
            result.put(productId, DropDownProductUtils.createDropdownProduct(productType));
            return result;
        }

        Map<String, DropdownProduct> result = new HashMap<>();
        result.put(productId, new DropdownProduct(productId, "", ""));
        return result;
    }

    private Map<String, DropdownProduct> getProducts() {
        LinkedHashMap products = new LinkedHashMap();

        products.put("confirm", new DropdownProduct("confirm", "confirm", ""));
        products.put("error", new DropdownProduct("error", "error", ""));

        List<ShsProduct> shsProducts = productAdminService.findAll();
        for (ShsProduct shsProduct : shsProducts) {
            products.put(shsProduct.getUuid(), DropDownProductUtils.createDropdownProduct(shsProduct));
        }

        return products;
    }

    private DirectoryAdminService getDirectoryAdminService() {
        return DirectoryAdminServiceUtil.getSelectedDirectoryAdminService(directoryAdminServiceRegistry);
    }

	private static final long serialVersionUID = 1L;

}
