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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
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
import se.inera.axel.shs.broker.directory.Agreement;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.directory.ProductType;
import se.inera.axel.shs.broker.product.ProductAdminService;
import se.inera.axel.shs.broker.routing.ShsRouter;
import se.inera.axel.shs.broker.webconsole.common.Constant;
import se.inera.axel.shs.broker.webconsole.common.DirectoryAdminServiceUtil;
import se.inera.axel.shs.xml.product.ShsProduct;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AgreementEditPanel extends Panel {

    @Inject
	@Named("directoryAdminServiceRegistry")
    @SpringBean(name = "directoryAdminServiceRegistry")
    DirectoryAdminServiceRegistry directoryAdminServiceRegistry;

    @Inject
    @Named("productService")
    @SpringBean(name = "productAdminService")
    ProductAdminService productAdminService;

    @Inject
	@Named("shsRouter")
    @SpringBean(name = "shsRouter")
	ShsRouter shsRouter;

	public AgreementEditPanel(String id, PageParameters params) {
		super(id);

		add(new FeedbackPanel("feedback"));

		final String orgNumber = params.get("orgno").toString();
		String serialNumber = params.get("serialNumber").toString();

		Agreement agreement = null;
		if (StringUtils.isNotBlank(serialNumber) && StringUtils.isNotBlank(orgNumber)) {
            Organization organization = getDirectoryAdminService().getOrganization(orgNumber);
			agreement = getDirectoryAdminService().lookupAgreement(organization, serialNumber);
		} else {
			agreement = new Agreement();
            agreement.setSerialNumber(UUID.randomUUID().toString());
			agreement.setPrincipal(orgNumber == null ? shsRouter.getOrgId() : orgNumber);
		}

		final IModel<Agreement> agreementModel = new CompoundPropertyModel<Agreement>(agreement);
		Form<Agreement> form = new Form<Agreement>("agreementForm", agreementModel) {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				Agreement submittedAgreement = getModelObject();
				Organization organization = getDirectoryAdminService().getOrganization(orgNumber);
				try {
					getDirectoryAdminService().saveAgreement(organization, submittedAgreement);
				} catch (Exception e) {
					e.printStackTrace();
				}
				PageParameters params = new PageParameters();
				params.add("orgNumber", orgNumber);
				setResponsePage(ActorPage.class, params);
			}

			private static final long serialVersionUID = 1L;
		};

        final TextField<String> serialNumberField = new TextField<String>("serialNumber");
        serialNumberField.setOutputMarkupId(true);
        serialNumberField.setEnabled(false);
        serialNumberField.setRequired(true);
        form.add(serialNumberField);

        final TextField<String> productName = new TextField<String>("productName");
        productName.setOutputMarkupId(true);
        productName.setEnabled(false);
        form.add(productName);

        final Map<String, DropdownProduct> products = getProducts(agreement.getProductId(), orgNumber);

        IChoiceRenderer<String> productRenderer = new DropdownProductChoiceRenderer(products);
        DropDownChoice<String> ddcProducts = new DropDownChoice<String>(
                "productId", Model.ofList(new ArrayList<String>(products.keySet())), productRenderer);
        ddcProducts.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                Agreement agreement = agreementModel.getObject();
                DropdownProduct selectedProduct = products.get(agreement.getProductId());

                if (selectedProduct != null) {
                    agreement.setProductName(
                            selectedProduct.getProductName());
                } else {
                    agreement.setProductName("");
                }

                target.add(productName);
            }

            private static final long serialVersionUID = 1L;
        });

        ddcProducts.setRequired(true);
        form.add(ddcProducts);

		form.add(new TextField<String>("principal").setRequired(true).setEnabled(false));
		form.add(getDeliveryConfirmationRadioGroup());

		form.add(new DropDownChoice<String>("transferType", Constant.TRANSFER_TYPE_LIST)
				.setRequired(true));

		form.add(new TextField<String>("description"));
		form.add(new TextField<String>("error"));
		form.add(new TextField<String>("labeledUri"));

		form.add(new SubmitLink("submit"));
		PageParameters cancelParams = new PageParameters();
		cancelParams.add("orgNumber", orgNumber);
		form.add(new BookmarkablePageLink<Void>("cancel", ActorPage.class, cancelParams));

		add(form);
	}

    private Map<String, DropdownProduct> getProducts(String productId, String orgNumber) {
        Map<String, DropdownProduct> products = new LinkedHashMap<String, DropdownProduct>();

        addLocalProducts(products);

        if (StringUtils.isNotBlank(productId) && !products.containsKey(productId)) {
            addSelectedProduct(products, productId, orgNumber);
        }

        return products;
    }

    private void addSelectedProduct(Map<String, DropdownProduct> productMap, String productId, String orgNumber) {
        ProductType productType = getDirectoryAdminService().getProductType(orgNumber, productId);

        if (productType != null) {
            productMap.put(productId, DropDownProductUtils.createDropdownProduct(productType));
        } else {
            productMap.put(productId, new DropdownProduct(productId, "", ""));
        }
    }

    private void addLocalProducts(Map<String, DropdownProduct> productMap) {
        List<ShsProduct> shsProducts = productAdminService.findAll();
        for (ShsProduct shsProduct : shsProducts) {
            productMap.put(shsProduct.getUuid(), DropDownProductUtils.createDropdownProduct(shsProduct));
        }
    }

    protected RadioGroup<String> getDeliveryConfirmationRadioGroup() {
		RadioGroup<String> deliveryConfirmationRadioGroup = new RadioGroup<String>(
				"deliveryConfirmation");
		deliveryConfirmationRadioGroup.add(new Radio<String>(Constant.YES, Model.of(Constant.YES)));
		deliveryConfirmationRadioGroup.add(new Radio<String>(Constant.NO, Model.of(Constant.NO)));
		return deliveryConfirmationRadioGroup;
	}

    private DirectoryAdminService getDirectoryAdminService() {
        return DirectoryAdminServiceUtil.getSelectedDirectoryAdminService(directoryAdminServiceRegistry);
    }

	private static final long serialVersionUID = 1L;

}
