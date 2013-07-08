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

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import se.inera.axel.shs.broker.webconsole.base.ControlGroupContainer;
import se.inera.axel.shs.broker.product.ProductAdminService;
import se.inera.axel.shs.processor.ShsProductMarshaller;
import se.inera.axel.shs.xml.XmlException;
import se.inera.axel.shs.xml.product.ObjectFactory;
import se.inera.axel.shs.xml.product.ShsProduct;

import java.io.Serializable;

import static se.inera.axel.shs.broker.webconsole.base.AdminPageParameters.CURRENT_UUID;
import static se.inera.axel.shs.broker.webconsole.base.AdminPageParameters.EDIT_MODE;

/**
 * Display xml view of a product
 * 
 */
public class ProductXmlPanel extends Panel {

	@PaxWicketBean(name = "productService")
    @SpringBean(name = "productAdminService")
	ProductAdminService productAdminService;
	
	private ShsProductMarshaller marshaller;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param parameters
	 */
	public ProductXmlPanel(final String id, final PageParameters parameters) {
		super(id);

		add(new FeedbackPanel("feedback"));

		ShsProduct product = getProduct(parameters);

		marshaller = new ShsProductMarshaller(
				ObjectFactory.class.getClassLoader());
		String xml = marshaller.marshal(product);

		XmlForm xmlForm = new XmlForm("uuid1", xml);
		IModel<XmlForm> xmlModel = new CompoundPropertyModel<XmlForm>(xmlForm);

		Form<XmlForm> form = new Form<XmlForm>("productForm", xmlModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
				super.onSubmit();
				XmlForm xmlForm = getModelObject();
				try {
					ShsProduct shsProduct = marshaller.unmarshal(xmlForm.getXml());
					parameters.add("uuid", shsProduct.getUuid());
					productAdminService.save(shsProduct);

					String view = parameters.get("view").toString();
					if (view != null && view.equals("form")) {
						setResponsePage(EditProductPage.class, parameters);
					} else {
						setResponsePage(ProductPage.class);
					}
				} catch (Exception e) {
					error(e.getMessage());
					e.printStackTrace();
				}
			}

		};
		form.add(new HiddenField<String>("uuid"));
		TextArea<String> xmlField = new TextArea<String>("xml");
		xmlField.add(new IValidator<String>() {

			@Override
			public void validate(IValidatable<String> validatable) {
				ShsProduct shsProduct = null;
				
				try {
					shsProduct = marshaller.unmarshal(validatable.getValue());
				} catch (XmlException e) {
					ValidationError error = new ValidationError();
					error.addMessageKey("xml.InvalidXml");
					validatable.error(error);
					return;
				}
				
				if (isEditMode(parameters)) {
					String currentUUID = parameters.get(CURRENT_UUID.toString()).toOptionalString(); 
					if (currentUUID != null && !currentUUID.equalsIgnoreCase(shsProduct.getUuid())) {
						ValidationError error = new ValidationError();
						error.addMessageKey("uuid.ReadOnly");
						error.setVariable("originalUUID", currentUUID);
						validatable.error(error);
						return;
					}
				} else {
					if (productAdminService.getProduct(shsProduct.getUuid()) != null) {
						ValidationError error = new ValidationError();
						error.addMessageKey("Exists");
						error.setVariable("uuid", shsProduct.getUuid());
						validatable.error(error);
						return;
					}
				}
			}
		});
		form.add(new ControlGroupContainer(xmlField));
		
		form.add(new SubmitLink("showform") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				parameters.remove("view");
				parameters.add("view", "form");
			}
		});
		form.add(new SubmitLink("submit"));
		add(form);
	}

	/**
	 * Get a product from db
	 * 
	 * @param parameters
	 * @return
	 */
	private ShsProduct getProduct(final PageParameters parameters) {
		ShsProduct product = null;
		String uuid = parameters.get("uuid").toString();
		if (uuid != null) {
			product = productAdminService.getProduct(uuid);
		} else {
			product = new ObjectFactory().createShsProduct();
		}
		return product;
	}
	
	private boolean isEditMode(final PageParameters parameters) {
		StringValue editValue = parameters.get(EDIT_MODE.toString()); 
		return editValue != null && editValue.toBoolean();
	}

	public static class XmlForm implements Serializable {
		private static final long serialVersionUID = 1L;

		private String uuid;
		private String xml;

		public XmlForm(String xml) {
			this.xml = xml;
		}

		public XmlForm(String uuid, String xml) {
			this.uuid = uuid;
			this.xml = xml;
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}

		public String getXml() {
			return xml;
		}

		public void setXml(String xml) {
			this.xml = xml;
		}
	}

	private static final long serialVersionUID = 1L;
}
