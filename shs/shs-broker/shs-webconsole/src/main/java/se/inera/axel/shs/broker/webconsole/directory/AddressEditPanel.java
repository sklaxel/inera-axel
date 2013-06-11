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
package se.inera.axel.shs.broker.webconsole.directory;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import se.inera.axel.shs.broker.directory.Address;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.product.ProductAdminService;
import se.inera.axel.shs.xml.product.ShsProduct;

import java.util.ArrayList;
import java.util.List;

public class AddressEditPanel extends Panel {

	@PaxWicketBean(name = "ldapDirectoryService")
	DirectoryAdminService ldapDirectoryService;

	@PaxWicketBean(name = "productService")
	ProductAdminService productAdminService;

	public AddressEditPanel(String id, PageParameters params) {
		super(id);

		add(new FeedbackPanel("feedback"));

		final String productId = params.get("serialNumber").toString();
		final String orgNumber = params.get("orgNumber").toString();

		Address address = null;
		if (StringUtils.isNotBlank(productId)
				&& StringUtils.isNotBlank(orgNumber)) {
			address = ldapDirectoryService.getAddress(orgNumber, productId);
		} else {
			address = new Address();
			address.setOrganizationNumber(orgNumber);
		}

		final IModel<Address> addressModel = new CompoundPropertyModel<Address>(
				address);
		Form<Address> form = new Form<Address>("addressForm", addressModel) {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				Address submittedAddress = getModelObject();
				Organization organization = ldapDirectoryService.getOrganization(orgNumber);
				ldapDirectoryService.saveAddress(organization, submittedAddress);

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
				.setRequired(true));

		final List<DropdownProduct> products = getProducts();
		IChoiceRenderer<DropdownProduct> productRenderer = new DropdownProductChoiceRenderer<DropdownProduct>();
		DropDownChoice<DropdownProduct> ddcProducts = new DropDownChoice<DropdownProduct>(
				"serialNumber", Model.ofList(products), productRenderer);
		ddcProducts.setRequired(true);
		form.add(ddcProducts);

		form.add(new SubmitLink("submit"));
		PageParameters cancelParams = new PageParameters();
		cancelParams.add("orgNumber", orgNumber);
		form.add(new BookmarkablePageLink<Void>("cancel", ActorPage.class,
				cancelParams));

		add(form);
	}

	protected DropdownProduct getProduct(String serialNumber,
			List<DropdownProduct> products) {
		DropdownProduct result = null;
		for (DropdownProduct product : products) {
			if (product.getSerialNumber().equals(serialNumber))
				result = product;
		}
		return result;
	}

	private List<DropdownProduct> getProducts() {
		List<DropdownProduct> products = new ArrayList<DropdownProduct>();
		List<ShsProduct> shsProducts = productAdminService.findAll();
		
		products.add(new DropdownProduct("confirm", "confirm", ""));
		products.add(new DropdownProduct("error", "error", ""));
		
		for (ShsProduct shsProduct : shsProducts) {
			products.add(new DropdownProduct(shsProduct.getUuid(), shsProduct
					.getCommonName(), shsProduct.getLabeledURI()));
		}
		return products;
	}

	private static final long serialVersionUID = 1L;

}
