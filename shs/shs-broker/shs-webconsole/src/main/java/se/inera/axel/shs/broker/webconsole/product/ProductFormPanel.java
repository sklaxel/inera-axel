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

import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.PropertyListView;
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
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.webconsole.base.AdminPageParameters;
import se.inera.axel.shs.broker.webconsole.base.ControlGroupContainer;
import se.inera.axel.shs.broker.webconsole.common.YesNoBooleanConverterModel;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.product.ProductAdminService;
import se.inera.axel.shs.xml.product.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static se.inera.axel.shs.broker.webconsole.base.AdminPageParameters.EDIT_MODE;

/**
 * Display form for editing products
 * 
 */
public class ProductFormPanel extends Panel {

	@PaxWicketBean(name = "productService")
    @SpringBean(name = "productAdminService")
	ProductAdminService productAdminService;

	@PaxWicketBean(name = "directoryAdminServiceRegistry")
    @SpringBean(name = "directoryAdminServiceRegistry")
    DirectoryAdminServiceRegistry directoryAdminServiceRegistry;

	/**
	 * Constructor
	 * 
	 * @param panelId
	 * @param parameters
	 */
	public ProductFormPanel(final String panelId, final PageParameters parameters) {

		super(panelId);

		add(new FeedbackPanel("feedback"));

		final IModel<ShsProduct> product = getProduct(parameters);

		// Create form
		final Form<ShsProduct> form = new Form<ShsProduct>("productForm", product);

		form.add(new ControlGroupContainer(new TextField<String>("commonName")));
		TextField<String> uuidField = new TextField<String>("uuid");
		uuidField.setRequired(true);
		
		uuidField.setEnabled(!isEditMode(parameters));
		uuidField.add(new IValidator<String>() {

			@Override
			public void validate(IValidatable<String> validatable) {
				String value = validatable.getValue();
				if (!isEditMode(parameters)) {
					if (productAdminService.getProduct(value) != null) {
						ValidationError error = new ValidationError();
						error.addMessageKey("Exists");
						error.setVariable("uuid", value);
						validatable.error(error);
					}
				}
			}
		});
		
		form.add(new ControlGroupContainer(uuidField));

		form.add(new ControlGroupContainer(new TextField<String>("labeledURI")));
		form.add(new ControlGroupContainer(new TextArea<String>("description")));

		CheckBox respRequiredField = new CheckBox("respRequired") {
			private static final long serialVersionUID = 1L;

			@Override
			protected IModel<?> initModel() {
				return new YesNoBooleanConverterModel(super.initModel());
			}
		};
		
		form.add(new ControlGroupContainer(respRequiredField));

		form.add(new ControlGroupContainer(getPrincipalDropDownChoice()));

		form.add(getDataListView(product));
		form.add(new SubmitLink("addData") {
			@Override
			public void onSubmit() {
				product.getObject().getData().add(new Data());
			}

			private static final long serialVersionUID = 1L;
		}.setOutputMarkupId(true));

		form.add(getReplyDataListView(product));
		form.add(new SubmitLink("addReplyData") {
			@Override
			public void onSubmit() {
				product.getObject().getReplyData().add(new ReplyData());
			}

			private static final long serialVersionUID = 1L;
		}.setOutputMarkupId(true));

		form.add(new SubmitLink("showxml") {
			@Override
			public void onSubmit() {
				super.onSubmit();

				// The xml-view loads the model from the data store so we need to save it first.
				ShsProduct shsProduct = form.getModelObject();
				productAdminService.save(shsProduct);

				parameters.remove("view");
				parameters.add("view", "xml");
				parameters.add(AdminPageParameters.EDIT_MODE.toString(), true);
				parameters.add("uuid", ((ShsProduct) getParent().getDefaultModelObject()).getUuid());
				setResponsePage(EditProductPage.class, parameters);
			}

			private static final long serialVersionUID = 1L;
		});
		form.add(new SubmitLink("submit") {
			@Override
			public void onSubmit() {

				ShsProduct shsProduct = form.getModelObject();
				productAdminService.save(shsProduct);

				setResponsePage(ProductPage.class);
			}

			private static final long serialVersionUID = 1L;
		});
		add(form);
	}

	@SuppressWarnings("rawtypes")
	protected ListView getReplyDataListView(final IModel<ShsProduct> product) {
		ListView lvReply = new PropertyListView("replyData") {
			@Override
			protected void populateItem(final ListItem item) {
				item.add(new TextField("datapartType"));
				item.add(new TextField("dataType"));
				item.add(new TextField("description"));
				item.add(new TextField("maxOccurs"));
				item.add(new TextField("minOccurs"));
				item.add(new TextField("mime.type"));
				item.add(new TextField("mime.subtype"));
				item.add(new TextField("mime.textCharset"));
				item.add(new TextField("mime.transferEncoding"));
				item.add(new Link<Void>("delete") {
					@Override
					public void onClick() {
						product.getObject().getReplyData().remove(item.getIndex());
					}

					private static final long serialVersionUID = 1L;
				}.setOutputMarkupId(true));
			}

			private static final long serialVersionUID = 1L;
		};
		lvReply.setReuseItems(true);
		lvReply.setOutputMarkupId(true);
		return lvReply;
	}

	@SuppressWarnings("rawtypes")
	protected ListView getDataListView(final IModel<ShsProduct> product) {
		ListView lv = new PropertyListView("data") {
			@Override
			protected void populateItem(final ListItem item) {
				item.add(new TextField("datapartType"));
				item.add(new TextField("dataType"));
				item.add(new TextField("description"));
				item.add(new TextField("maxOccurs"));
				item.add(new TextField("minOccurs"));
				item.add(new TextField("mime.type"));
				item.add(new TextField("mime.subtype"));
				item.add(new TextField("mime.textCharset"));
				item.add(new TextField("mime.transferEncoding"));
				item.add(new Link<Void>("delete") {
					@Override
					public void onClick() {
						product.getObject().getData().remove(item.getIndex());
					}

					private static final long serialVersionUID = 1L;
				}.setOutputMarkupId(true));
			}

			private static final long serialVersionUID = 1L;
		};
		lv.setReuseItems(true);
		lv.setOutputMarkupId(true);
		return lv;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected DropDownChoice getPrincipalDropDownChoice() {
		// Create drop down with principals
		List<Principal> principals = getPrincipals();
		IChoiceRenderer<Principal> renderer = new PrincipalChoiceRenderer<Principal>();
		DropDownChoice ddc = new DropDownChoice("principal", Model.ofList(principals), renderer);
		ddc.setRequired(true);
		return ddc;
	}

	/**
	 * Get a list of available principals, if no directory available return
	 * empty lists
	 * 
	 * @return
	 */
	protected List<Principal> getPrincipals() {
		List<Principal> principals = new ArrayList<Principal>();
		try {
			final List<Organization> organizations = directoryAdminServiceRegistry.getDirectoryAdminServiceAggregator().getOrganizations();
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
					return o1.getCommonName().compareTo(o2.getCommonName());
				}
			});
		} catch (Exception e) {
			// TODO display info if principal is not required
		}
		return principals;
	}

	/**
	 * Get product and convert it to a Model
	 * 
	 * @param parameters
	 *            contains id parameter identifying the product
	 * @return
	 */
	protected IModel<ShsProduct> getProduct(final PageParameters parameters) {
		ShsProduct product = null;
		String uuid = parameters.get("uuid").toString();
		if (uuid != null) {
			product = productAdminService.getProduct(uuid);
		} else {
			product = new ObjectFactory().createShsProduct();
			product.setRespRequired("no");
		}
		IModel<ShsProduct> productModel = new CompoundPropertyModel<ShsProduct>(product);
		return productModel;
	}
	
	private boolean isEditMode(final PageParameters parameters) {
		StringValue editValue = parameters.get(EDIT_MODE.toString()); 
		return editValue != null && editValue.toBoolean();
	}

	private static final long serialVersionUID = 1L;

}
