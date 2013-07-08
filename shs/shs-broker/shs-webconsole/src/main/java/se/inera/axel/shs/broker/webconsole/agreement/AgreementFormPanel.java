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

import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.broker.agreement.AgreementAdminService;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.product.ProductAdminService;
import se.inera.axel.shs.broker.webconsole.base.ControlGroupContainer;
import se.inera.axel.shs.broker.webconsole.common.Constant;
import se.inera.axel.shs.xml.agreement.*;
import se.inera.axel.shs.xml.product.ShsProduct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static se.inera.axel.shs.broker.webconsole.base.AdminPageParameters.*;

public class AgreementFormPanel extends Panel {
	private static final Logger log = LoggerFactory.getLogger(AgreementFormPanel.class); 

	@PaxWicketBean(name = "agreementService")
    @SpringBean(name = "agreementAdminService")
	AgreementAdminService agreementAdminService;

	@PaxWicketBean(name = "ldapDirectoryService")
    @SpringBean(name = "directoryAdminService")
    DirectoryAdminService ldapDirectoryService;

	@PaxWicketBean(name = "productService")
    @SpringBean(name = "productAdminService")
	ProductAdminService productAdminService;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AgreementFormPanel(final String panelId, final PageParameters parameters) {
		super(panelId);

		add(new FeedbackPanel("feedback"));

		ShsAgreement agreement = getAgreement(parameters);

		IModel<ShsAgreement> agreementModel = new CompoundPropertyModel<ShsAgreement>(agreement);

		final Form<ShsAgreement> form = new Form<ShsAgreement>("agreementForm", agreementModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
				super.onSubmit();
				ShsAgreement shsAgreement = getModelObject();
				parameters.set(CURRENT_UUID.toString(), shsAgreement.getUuid());
				
				agreementAdminService.save(shsAgreement);

				String view = parameters.get(VIEW.toString()).toString();
				if (view != null && view.equals("xml")) {
					setResponsePage(EditAgreementPage.class, parameters);
				} else {
					setResponsePage(AgreementPage.class);
				}
			}
		};
		
		boolean isEditMode = isEditMode(parameters);
		
		final TextField<String> uuidField = new TextField<String>("uuid");
		uuidField.setRequired(true).setEnabled(!isEditMode);
		uuidField.add(new IValidator<String>() {

			@Override
			public void validate(IValidatable<String> validatable) {
				String value = validatable.getValue();
				if (!isEditMode(parameters)) {
					if (agreementAdminService.findOne(value) != null) {
						ValidationError error = new ValidationError();
						error.addMessageKey("agreementForm.uuid.Exists");
						error.setVariable("uuid", value);
						validatable.error(error);
					}
				}
			}
		});
		form.add(new ControlGroupContainer(uuidField));
		
		List<Principal> principals = getPrincipals();
		IChoiceRenderer<Principal> principalRenderer = new PrincipalChoiceRenderer();
		form.add(new ControlGroupContainer(new DropDownChoice("shs.principal", Model.ofList(principals), principalRenderer)
		.setRequired(true)));

		List<Customer> customers = getCustomers();
		IChoiceRenderer<Customer> customerRenderer = new CustomerChoiceRenderer();
		DropDownChoice ddcCustomer = new DropDownChoice("shs.customer", Model.ofList(customers),
				customerRenderer);
		form.add(new ControlGroupContainer(ddcCustomer));

		DropDownChoice<String> transferType = new DropDownChoice<String>("transferType",
				Model.ofList(Constant.TRANSFER_TYPE_LIST));
		transferType.setRequired(true);
		form.add(new ControlGroupContainer(transferType));
		
		form.add(new ControlGroupContainer(new CheckBox("shs.confirm.required")));
		form.add(new ControlGroupContainer(new DropDownChoice<String>("shs.direction.flow", Model
				.ofList(Constant.DIRECTION_LIST)).setRequired(true)));

		List<Product> products = getProducts();
		IChoiceRenderer<Product> productRenderer = new ProductChoiceRenderer();
		form.add(new ControlGroupContainer(new DropDownChoice("shs.product.0", Model.ofList(products), productRenderer)
				.setRequired(true)));

		form.add(new ControlGroupContainer(new DateTextField("general.valid.validFrom.date", "yyyy-MM-dd").setRequired(true)));
		form.add(new ControlGroupContainer(new DateTextField("general.valid.validTo.date", "yyyy-MM-dd")));

		form.add(new ControlGroupContainer(new TextArea<String>("general.description")));
		
		form.add(new SubmitLink("showxml") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				parameters.set(VIEW.toString(), "xml");
			}
		});
		form.add(new SubmitLink("submit"));
		add(form);
	}

	private boolean isEditMode(final PageParameters parameters) {
		StringValue editValue = parameters.get(EDIT_MODE.toString()); 
		return editValue != null && editValue.toBoolean();
	}

	private List<Customer> getCustomers() {
		List<Customer> customers = new ArrayList<Customer>();
		final List<Organization> organizations = ldapDirectoryService.getOrganizations();
		Customer customer = null;
		for (Organization organization : organizations) {
			customer = new ObjectFactory().createCustomer();
			customer.setCommonName(organization.getOrgName());
			customer.setLabeledURI(organization.getLabeledUri());
			customer.setvalue(organization.getOrgNumber());
			customers.add(customer);
		}
		Collections.sort(customers, new Comparator<Customer>() {
			@Override
			public int compare(Customer o1, Customer o2) {
				return o1.getCommonName().compareTo(o2.getCommonName());
			}
		});
		return customers;
	}

	private List<Product> getProducts() {
		List<Product> products = new ArrayList<Product>();
		List<ShsProduct> shsProducts = productAdminService.findAll();
		Product product = null;
		for (ShsProduct shsProduct : shsProducts) {
			product = new ObjectFactory().createProduct();
			product.setCommonName(shsProduct.getCommonName());
			product.setLabeledURI(shsProduct.getLabeledURI());
			product.setvalue(shsProduct.getUuid());
			products.add(product);
		}
		return products;
	}

	private ShsAgreement getAgreement(PageParameters parameters) {
		ShsAgreement agreement = null;
		String uuid = parameters.get(CURRENT_UUID.toString()).toString();
		if (uuid != null) {
			agreement = agreementAdminService.findOne(uuid);
		} else {
			agreement = new ObjectFactory().createShsAgreement();
		}
		return agreement;
	}

	private List<Principal> getPrincipals() {
		List<Principal> principals = new ArrayList<Principal>();
		final List<Organization> organizations = ldapDirectoryService.getOrganizations();
		Principal principal = null;
		for (Organization organization : organizations) {
			principal = new ObjectFactory().createPrincipal();
			principal.setCommonName(organization.getOrgName());
			principal.setLabeledURI(organization.getLabeledUri());
			principal.setvalue(organization.getOrgNumber());
			principals.add(principal);
		}
		Collections.sort(principals, new Comparator<Principal>() {
			@Override
			public int compare(Principal o1, Principal o2) {
				if (o1 != null && o2 != null && o1.getCommonName() != null
						&& o2.getCommonName() != null) {
					return o1.getCommonName().compareTo(o2.getCommonName());
				} else {
					return 0;
				}
			}
		});
		return principals;
	}

	private static final long serialVersionUID = 1L;

}
