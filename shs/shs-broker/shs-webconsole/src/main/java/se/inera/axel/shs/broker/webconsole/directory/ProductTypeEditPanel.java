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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.directory.ProductType;
import se.inera.axel.shs.broker.product.ProductAdminService;
import se.inera.axel.shs.broker.routing.ShsRouter;
import se.inera.axel.shs.broker.webconsole.common.DirectoryAdminServiceUtil;
import se.inera.axel.shs.xml.product.ShsProduct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProductTypeEditPanel extends Panel {

	@PaxWicketBean(name = "directoryAdminServiceRegistry")
    @SpringBean(name = "directoryAdminServiceRegistry")
    DirectoryAdminServiceRegistry directoryAdminServiceRegistry;

	@PaxWicketBean(name = "productService")
    @SpringBean(name = "productAdminService")
	ProductAdminService productAdminService;

	@PaxWicketBean(name = "shsRouter")
    @SpringBean(name = "shsRouter")
	ShsRouter shsRouter;

	public ProductTypeEditPanel(String id, PageParameters params) {
		super(id);

		add(new FeedbackPanel("feedback"));

		final String productId = params.get("productId").toString();
		final String orgNumber = params.get("orgNumber").toString();

		ProductType product = null;
		if (isEditMode(productId, orgNumber)) {
			product = getDirectoryAdminService().getProductType(orgNumber, productId);
		} else {
			product = new ProductType();
			product.setPrincipal(orgNumber == null ? shsRouter.getOrgId() : orgNumber);
		}

		final IModel<ProductType> productModel = new CompoundPropertyModel<ProductType>(
				product);
		Form<ProductType> form = new Form<ProductType>("productForm",
				productModel) {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				ProductType submittedProductType = getModelObject();
				Organization organization = getDirectoryAdminService().getOrganization(orgNumber);
				getDirectoryAdminService().saveProduct(organization, submittedProductType);

				PageParameters params = new PageParameters();
				params.add("orgNumber", orgNumber);
				setResponsePage(ActorPage.class, params);
			}

			private static final long serialVersionUID = 1L;
		};

        AttributeModifier disabledAttributeModifier = new AttributeModifier("disabled", new Model("disabled"));

		final TextField<String> productName = new TextField<String>(
				"productName");
		productName.setRequired(true);
		productName.setOutputMarkupId(true);
        productName.add(disabledAttributeModifier);
		form.add(productName);

        TextField<String> principalField = new TextField<String>("principal");

        principalField.add(disabledAttributeModifier);

		form.add(principalField.setRequired(true));
		form.add(new TextField<String>("description"));
		form.add(new TextField<String>("prodDescr"));
		form.add(new TextField<String>("labeledUri").setRequired(true));
		form.add(new TextField<String>("keywords"));
		form.add(new TextField<String>("preferredDeliveryMethod")
				.setRequired(true));
		form.add(new TextField<String>("owner"));

		final List<DropdownProduct> products = getProducts(productId, orgNumber);

        IChoiceRenderer<DropdownProduct> productRenderer = new DropdownProductChoiceRenderer<DropdownProduct>();
		DropDownChoice<DropdownProduct> ddcProducts = new DropDownChoice<DropdownProduct>(
				"serialNumber", Model.ofList(products), productRenderer);
		ddcProducts.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				ProductType productFormObject = productModel.getObject();
				DropdownProduct selectedProduct = getProduct(
						productFormObject.getSerialNumber(), products);
				productFormObject.setProductName(selectedProduct
						.getProductName());
				target.add(productName);
			}

			private static final long serialVersionUID = 1L;
		});
		ddcProducts.setRequired(true);
        if (isEditMode(productId, orgNumber)) {
            ddcProducts.add(disabledAttributeModifier);
        }
		form.add(ddcProducts);

		form.add(new SubmitLink("submit"));
		PageParameters cancelParams = new PageParameters();
		cancelParams.add("orgNumber", orgNumber);
		form.add(new BookmarkablePageLink<Void>("cancel", ActorPage.class,
				cancelParams));

		add(form);
	}

    private List<DropdownProduct> getProducts(String productId, String orgNumber) {
        List<DropdownProduct> products;
        if (isEditMode(productId, orgNumber)) {
            products = getSelectedProductAsList(productId, orgNumber);
        } else {
            products = getProducts();
        }
        return products;
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

    private List<DropdownProduct> getSelectedProductAsList(String productId, String orgNumber) {
        ShsProduct shsProduct = productAdminService.getProduct(productId);

        if (shsProduct != null) {
            return Arrays.asList(DropDownProductUtils.createDropdownProduct(shsProduct));
        }

        ProductType productType = getDirectoryAdminService().getProductType(orgNumber, productId);

        if (productType != null) {
            return Arrays.asList(DropDownProductUtils.createDropdownProduct(productType));
        }

        return Arrays.asList(new DropdownProduct(productId, "", ""));
    }

	private List<DropdownProduct> getProducts() {
		List<DropdownProduct> products = new ArrayList<DropdownProduct>();
		List<ShsProduct> shsProducts = productAdminService.findAll();
		for (ShsProduct shsProduct : shsProducts) {
			products.add(DropDownProductUtils.createDropdownProduct(shsProduct));
		}
		return products;
	}

    private DirectoryAdminService getDirectoryAdminService() {
        return DirectoryAdminServiceUtil.getSelectedDirectoryAdminService(directoryAdminServiceRegistry);
    }

    private boolean isEditMode(String productId, String orgNumber) {
        return StringUtils.isNotBlank(productId)
                && StringUtils.isNotBlank(orgNumber);
    }

	private static final long serialVersionUID = 1L;

}
