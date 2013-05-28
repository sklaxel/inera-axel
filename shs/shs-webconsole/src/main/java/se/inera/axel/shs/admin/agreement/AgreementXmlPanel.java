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
package se.inera.axel.shs.admin.agreement;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import se.inera.axel.shs.admin.base.ControlGroupContainer;
import se.inera.axel.shs.agreement.AgreementAdminService;
import se.inera.axel.shs.xml.ShsAgreementMarshaller;
import se.inera.axel.shs.xml.XmlException;
import se.inera.axel.shs.xml.agreement.ObjectFactory;
import se.inera.axel.shs.xml.agreement.ShsAgreement;

import java.io.Serializable;

import static se.inera.axel.shs.admin.base.AdminPageParameters.*;

/**
 * Display xml view of an agreement
 * 
 */
public class AgreementXmlPanel extends Panel {

	@PaxWicketBean(name = "agreementService")
	AgreementAdminService agreementAdminService;
	
	private ShsAgreementMarshaller marshaller;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param parameters
	 */
	public AgreementXmlPanel(final String id, final PageParameters parameters) {
		super(id);

		add(new FeedbackPanel("feedback"));

		ShsAgreement agreement = getAgreement(parameters);

		marshaller = new ShsAgreementMarshaller(
				ObjectFactory.class.getClassLoader());

		String xml = marshaller.marshal(agreement);

		XmlForm xmlForm = new XmlForm(agreement.getUuid(), xml);
		IModel<XmlForm> xmlModel = new CompoundPropertyModel<XmlForm>(xmlForm);

		Form<XmlForm> form = new Form<XmlForm>("agreementForm", xmlModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
				super.onSubmit();
				XmlForm xmlForm = getModelObject();
				try {
					ShsAgreement agreement = unmarshal(xmlForm.getXml());
					
					parameters.set(CURRENT_UUID.toString(), agreement.getUuid());
					agreementAdminService.save(agreement);

					String view = parameters.get(VIEW.toString()).toString();
					if (view != null && view.equals("form")) {
						setResponsePage(EditAgreementPage.class, parameters);
					} else {
						setResponsePage(AgreementPage.class);
					}
				} catch (Exception e) {
					error(e.getMessage());
					e.printStackTrace();
				}
			}

			private ShsAgreement unmarshal(String xml) {
				ShsAgreementMarshaller marshaller = new ShsAgreementMarshaller(
						ObjectFactory.class.getClassLoader());
				return marshaller.unmarshal(xml);
			}

		};
		form.add(new HiddenField<String>("uuid"));
		TextArea<String> xmlField = new TextArea<String>("xml");
		xmlField.add(new IValidator<String>() {

			@Override
			public void validate(IValidatable<String> validatable) {
				ShsAgreement shsAgreement = null;
				
				try {
					shsAgreement = marshaller.unmarshal(validatable.getValue());
				} catch (XmlException e) {
					ValidationError error = new ValidationError();
					error.addMessageKey("xml.InvalidXml");
					validatable.error(error);
					return;
				}
				
				if (isEditMode(parameters)) {
					String currentUUID = parameters.get(CURRENT_UUID.toString()).toOptionalString(); 
					if (currentUUID != null && !currentUUID.equalsIgnoreCase(shsAgreement.getUuid())) {
						ValidationError error = new ValidationError();
						error.addMessageKey("uuid.ReadOnly");
						error.setVariable("originalUUID", currentUUID);
						validatable.error(error);
						return;
					}
				} else {
					if (agreementAdminService.findOne(shsAgreement.getUuid()) != null) {
						ValidationError error = new ValidationError();
						error.addMessageKey("Exists");
						error.setVariable("uuid", shsAgreement.getUuid());
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
				parameters.set(VIEW.toString(), "form");
			}
		});
		form.add(new SubmitLink("submit"));
		add(form);
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
