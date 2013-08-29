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
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.webconsole.common.Constant;
import se.inera.axel.shs.broker.agreement.AgreementAdminService;
import se.inera.axel.shs.broker.directory.Agreement;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.routing.ShsRouter;
import se.inera.axel.shs.broker.webconsole.common.DirectoryAdminServiceUtil;
import se.inera.axel.shs.xml.agreement.ShsAgreement;

import java.util.ArrayList;
import java.util.List;

public class AgreementEditPanel extends Panel {

	@PaxWicketBean(name = "directoryAdminServiceRegistry")
    @SpringBean(name = "directoryAdminServiceRegistry")
    DirectoryAdminServiceRegistry directoryAdminServiceRegistry;

	@PaxWicketBean(name = "agreementService")
    @SpringBean(name = "agreementAdminService")
	AgreementAdminService agreementAdminService;

	@PaxWicketBean(name = "shsRouter")
    @SpringBean(name = "shsRouter")
	ShsRouter shsRouter;

	public AgreementEditPanel(String id, PageParameters params) {
		super(id);

		add(new FeedbackPanel("feedback"));

        final DirectoryAdminService directoryAdminService =
                DirectoryAdminServiceUtil.getSelectedDirectoryAdminService(directoryAdminServiceRegistry);

		final String orgNumber = params.get("orgno").toString();
		String productIdParam = params.get("pid").toString();
		String transferType = params.get("transfType").toString();

		Agreement agreement = null;
		if (StringUtils.isNotBlank(productIdParam) && StringUtils.isNotBlank(orgNumber)
				&& StringUtils.isNotBlank(transferType)) {
			agreement = directoryAdminService.getAgreement(orgNumber, productIdParam, transferType);
		} else {
			agreement = new Agreement();
			agreement.setPrincipal(shsRouter.getOrgId());
		}

		final IModel<Agreement> agreementModel = new CompoundPropertyModel<Agreement>(agreement);
		Form<Agreement> form = new Form<Agreement>("agreementForm", agreementModel) {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				Agreement submittedAgreement = getModelObject();
				Organization organization = directoryAdminService.getOrganization(orgNumber);
				try {
					directoryAdminService.saveAgreement(organization, submittedAgreement);
				} catch (Exception e) {
					e.printStackTrace();
				}
				PageParameters params = new PageParameters();
				params.add("orgNumber", orgNumber);
				setResponsePage(ActorPage.class, params);
			}

			private static final long serialVersionUID = 1L;
		};

		final TextField<String> productName = new TextField<String>("productName");
		productName.setOutputMarkupId(true);
		form.add(productName);
		final TextField<String> productId = new TextField<String>("productId");
		productId.setOutputMarkupId(true);
		productId.setRequired(true);
		form.add(productId);

		form.add(new TextField<String>("principal").setRequired(true));
		form.add(getDeliveryConfirmationRadioGroup());

		form.add(new DropDownChoice<String>("transferType", Constant.TRANSFER_TYPE_LIST)
				.setRequired(true));

		form.add(new TextField<String>("description"));
		form.add(new TextField<String>("error"));
		form.add(new TextField<String>("labeledUri").setRequired(true));

		final List<DropdownAgreement> agreements = getAgreements();
		IChoiceRenderer<DropdownAgreement> agreementRenderer = new DropdownAgreementChoiceRenderer<DropdownAgreement>();
		DropDownChoice<DropdownAgreement> ddcAgreements = new DropDownChoice<DropdownAgreement>(
				"serialNumber", Model.ofList(agreements), agreementRenderer);
		ddcAgreements.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				Agreement agreementFormObject = agreementModel.getObject();
				DropdownAgreement selectedAgreement = getAgreement(
						agreementFormObject.getSerialNumber(), agreements);
				agreementFormObject.setProductId(selectedAgreement.getProductId());
				agreementFormObject.setProductName(selectedAgreement.getProductName());
				target.add(productName, productId);
			}

			private static final long serialVersionUID = 1L;
		});
		ddcAgreements.setRequired(true);
		form.add(ddcAgreements);

		form.add(new SubmitLink("submit"));
		PageParameters cancelParams = new PageParameters();
		cancelParams.add("orgNumber", orgNumber);
		form.add(new BookmarkablePageLink<Void>("cancel", ActorPage.class, cancelParams));

		add(form);
	}

	protected RadioGroup<String> getDeliveryConfirmationRadioGroup() {
		RadioGroup<String> deliveryConfirmationRadioGroup = new RadioGroup<String>(
				"deliveryConfirmation");
		deliveryConfirmationRadioGroup.add(new Radio<String>(Constant.YES, Model.of(Constant.YES)));
		deliveryConfirmationRadioGroup.add(new Radio<String>(Constant.NO, Model.of(Constant.NO)));
		return deliveryConfirmationRadioGroup;
	}

	protected DropdownAgreement getAgreement(String serialNumber, List<DropdownAgreement> agreements) {
		DropdownAgreement result = null;
		for (DropdownAgreement agreement : agreements) {
			if (agreement.getSerialNumber().equals(serialNumber))
				result = agreement;
		}
		return result;
	}

	protected List<DropdownAgreement> getAgreements() {
		List<DropdownAgreement> agreements = new ArrayList<DropdownAgreement>();
		List<ShsAgreement> shsAgreements = agreementAdminService.findAll();
		for (ShsAgreement shsAgreement : shsAgreements) {
			agreements.add(new DropdownAgreement(shsAgreement.getUuid(), shsAgreement.getShs()
					.getProduct().get(0).getvalue(), shsAgreement.getShs().getProduct().get(0)
					.getCommonName()));
		}
		return agreements;
	}

	private static final long serialVersionUID = 1L;

}
