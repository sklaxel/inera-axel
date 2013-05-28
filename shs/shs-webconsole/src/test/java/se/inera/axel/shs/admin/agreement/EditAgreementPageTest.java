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
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import se.inera.axel.shs.admin.base.AbstractPageTest;
import se.inera.axel.shs.agreement.AgreementAdminService;
import se.inera.axel.shs.directory.DirectoryAdminService;
import se.inera.axel.shs.directory.Organization;
import se.inera.axel.shs.product.ProductAdminService;
import se.inera.axel.shs.routing.ShsRouter;
import se.inera.axel.shs.xml.ShsAgreementMarshaller;
import se.inera.axel.shs.xml.agreement.ShsAgreement;
import se.inera.axel.shs.xml.agreement.ShsAgreementMaker;
import se.inera.axel.shs.xml.product.ShsProduct;

import java.util.Arrays;
import java.util.UUID;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;
import static se.inera.axel.shs.admin.base.AdminPageParameters.*;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsAgreement;

public class EditAgreementPageTest extends AbstractPageTest {
	private final static Logger log = LoggerFactory.getLogger(EditAgreementPageTest.class);
    private ShsAgreement shsAgreement = null;
	private ShsAgreementMarshaller agreementMarshaller = null;

    @Override
    protected void beforeMethodSetup() {
        ShsRouter shsRouter = mock(ShsRouter.class);
        when(shsRouter.getOrgId()).thenReturn("0000000000");
        injector.registerBean("shsRouter", shsRouter);

        AgreementAdminService agreementAdminService = mock(AgreementAdminService.class);
        shsAgreement = make(a(ShsAgreement));
        when(agreementAdminService.findOne(shsAgreement.getUuid())).thenReturn(shsAgreement);
        injector.registerBean("agreementService", agreementAdminService);

        DirectoryAdminService ldapDirectoryService = mock(DirectoryAdminService.class);
        Organization organization = new Organization();
        organization.setOrgName("Test organization");
        organization.setOrgNumber("0000000000");
        when(ldapDirectoryService.getOrganizations()).thenReturn(Arrays.asList(organization));

        injector.registerBean("ldapDirectoryService", ldapDirectoryService);

        ProductAdminService productAdminService = mock(ProductAdminService.class);
        ShsProduct shsProduct = new ShsProduct();
        shsProduct.setUuid("00000000-0000-0000-0000-000000000001");
        when(productAdminService.findAll()).thenReturn(Arrays.asList(shsProduct));
        injector.registerBean("productService", productAdminService);

        agreementMarshaller = new ShsAgreementMarshaller();
    }

	@Test
	@SuppressWarnings("unchecked")
	public void testRenderEdit() {
		PageParameters pageParameters = new PageParameters();
		pageParameters.set(CURRENT_UUID.toString(), shsAgreement.getUuid());
		pageParameters.set(EDIT_MODE.toString(), true);
		tester.startPage(EditAgreementPage.class, pageParameters);
		
		tester.assertRenderedPage(EditAgreementPage.class);
		
		tester.assertNoErrorMessage();
		tester.assertComponent("agreementPanel:feedback", FeedbackPanel.class);
		tester.assertComponent("agreementPanel:agreementForm", Form.class);
		
		tester.assertComponent("agreementPanel:agreementForm:control.uuid:uuid", TextField.class);
        
		final TextField<String> uuidField = (TextField<String>) tester.getComponentFromLastRenderedPage("agreementPanel:agreementForm:control.uuid:uuid");
		assertEquals(uuidField.getModelObject(), shsAgreement.getUuid());
		assertFalse(uuidField.isEnabled(), "uuid was not disabled when editing an existing agreement");
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testRenderNew() {
		PageParameters pageParameters = new PageParameters();
		tester.startPage(EditAgreementPage.class, pageParameters);
		
		tester.assertRenderedPage(EditAgreementPage.class);
		
		tester.assertNoErrorMessage();
		tester.assertComponent("agreementPanel:feedback", FeedbackPanel.class);
		tester.assertComponent("agreementPanel:agreementForm", Form.class);
		
		tester.assertComponent("agreementPanel:agreementForm:control.uuid:uuid", TextField.class);
        
		final TextField<String> uuidField = (TextField<String>) tester.getComponentFromLastRenderedPage("agreementPanel:agreementForm:control.uuid:uuid");
		assertTrue(uuidField.isEnabled(), "uuid should be enabled when creating a new agreement");
	}
	
	@Test
	public void testCreateNew() {
		tester.startPage(EditAgreementPage.class, new PageParameters());
		FormTester formTester = tester.newFormTester("agreementPanel:agreementForm");
		setMandatoryFormParameters(formTester);
		formTester.submit();
		
		tester.assertRenderedPage(AgreementPage.class);
		
		tester.assertNoErrorMessage();
	}

	private void setMandatoryFormParameters(FormTester formTester) {
		formTester.setValue("control.uuid:uuid", java.util.UUID.randomUUID().toString());
		formTester.setValue("control.shs.principal:shs.principal", "0000000000");
		formTester.setValue("control.shs.customer:shs.customer", "0000000000");
		formTester.select("control.transferType:transferType", 1);
		formTester.select("control.shs.direction.flow:shs.direction.flow", 1);
		formTester.setValue("control.shs.product.0:shs.product.0", "00000000-0000-0000-0000-000000000001");
		formTester.setValue("control.general.valid.validFrom.date:general.valid.validFrom.date", "2012-10-01");
	}
	
	@Test
	public void testCreateNewWithExistingUuid() {
		tester.startPage(EditAgreementPage.class, new PageParameters());
		FormTester formTester = tester.newFormTester("agreementPanel:agreementForm");
		setMandatoryFormParameters(formTester);
		formTester.setValue("control.uuid:uuid", shsAgreement.getUuid());
		formTester.submit();
		
		tester.assertRenderedPage(EditAgreementPage.class);
		tester.assertComponent("agreementPanel:feedback", FeedbackPanel.class);
		
		tester.assertErrorMessages("An agreement with the UUID " + shsAgreement.getUuid() + " already exists");
		
	}
	
	@Test
	public void testRenderNewXmlView() {
		PageParameters pageParameters = new PageParameters();
		pageParameters.set(VIEW.toString(), "xml");
		
		tester.startPage(EditAgreementPage.class, pageParameters);
		
		tester.assertNoErrorMessage();
		tester.assertComponent("agreementPanel:feedback", FeedbackPanel.class);
		tester.assertComponent("agreementPanel:agreementForm", Form.class);
		tester.assertComponent("agreementPanel:agreementForm:uuid", HiddenField.class);
		tester.assertComponent("agreementPanel:agreementForm:control.xml:xml", TextArea.class);
		
	}
	
	@Test
	public void testRenderEditXmlView() {
		PageParameters pageParameters = new PageParameters();
		pageParameters.set(VIEW.toString(), "xml");
		pageParameters.set(EDIT_MODE.toString(), true);
		pageParameters.set(CURRENT_UUID.toString(), shsAgreement.getUuid());
		
		tester.startPage(EditAgreementPage.class, pageParameters);
		
		tester.assertNoErrorMessage();
		tester.assertComponent("agreementPanel:feedback", FeedbackPanel.class);
		tester.assertComponent("agreementPanel:agreementForm", Form.class);
		tester.assertComponent("agreementPanel:agreementForm:uuid", HiddenField.class);
		tester.assertComponent("agreementPanel:agreementForm:control.xml:xml", TextArea.class);
		
		final TextArea<String> xmlField = (TextArea<String>) tester.getComponentFromLastRenderedPage("agreementPanel:agreementForm:control.xml:xml");
		assertThat(xmlField.getValue(), containsString(shsAgreement.getUuid()));
	}
	
	@Test
	public void testCreateNewXmlView() {
		PageParameters pageParameters = new PageParameters();
		pageParameters.set(VIEW.toString(), "xml");
		tester.startPage(EditAgreementPage.class, pageParameters);
		FormTester formTester = tester.newFormTester("agreementPanel:agreementForm");
		shsAgreement.setUuid(UUID.randomUUID().toString());
		formTester.setValue("control.xml:xml", agreementMarshaller.marshal(shsAgreement));
		formTester.submit();
		
		tester.assertRenderedPage(AgreementPage.class);
		
		tester.assertNoErrorMessage();
	}
	
	@Test
	public void testCreateNewXmlViewWithExistingAgreement() {
		PageParameters pageParameters = new PageParameters();
		pageParameters.set(VIEW.toString(), "xml");
		tester.startPage(EditAgreementPage.class, pageParameters);
		FormTester formTester = tester.newFormTester("agreementPanel:agreementForm");
		formTester.setValue("control.xml:xml", agreementMarshaller.marshal(shsAgreement));
		formTester.submit();
		
		tester.assertRenderedPage(EditAgreementPage.class);
		
		tester.assertErrorMessages("An agreement with the UUID " + shsAgreement.getUuid() + " already exists");
	}

	@Test
	public void testEditAgreementXmlView() {
		PageParameters pageParameters = new PageParameters();
		pageParameters.set(VIEW.toString(), "xml");
		pageParameters.set(EDIT_MODE.toString(), true);
		pageParameters.set(CURRENT_UUID.toString(), shsAgreement.getUuid());
		
		tester.startPage(EditAgreementPage.class, pageParameters);
		FormTester formTester = tester.newFormTester("agreementPanel:agreementForm");
		shsAgreement.getShs().getDirection().setFlow("from-customer");
		formTester.setValue("control.xml:xml", agreementMarshaller.marshal(shsAgreement));
		formTester.submit();
		
		tester.assertRenderedPage(AgreementPage.class);
		
		tester.assertNoErrorMessage();
	}
	
	@Test
	public void testChangeUUIDXmlView() {
		PageParameters pageParameters = new PageParameters();
		pageParameters.set(VIEW.toString(), "xml");
		pageParameters.set(EDIT_MODE.toString(), true);
		pageParameters.set(CURRENT_UUID.toString(), shsAgreement.getUuid());
		
		tester.startPage(EditAgreementPage.class, pageParameters);
		FormTester formTester = tester.newFormTester("agreementPanel:agreementForm");
		shsAgreement.setUuid(UUID.randomUUID().toString());
		formTester.setValue("control.xml:xml", agreementMarshaller.marshal(shsAgreement));
		formTester.submit();
		
		tester.assertRenderedPage(EditAgreementPage.class);
		
		tester.assertErrorMessages("UUID must not be updated when editing an existing agreement. Original uuid " + ShsAgreementMaker.DEFAULT_UUID);
	}
	
	@Test
	public void testSubmitInvalidXml() {
		PageParameters pageParameters = new PageParameters();
		pageParameters.set(VIEW.toString(), "xml");
		pageParameters.set(EDIT_MODE.toString(), true);
		pageParameters.set(CURRENT_UUID.toString(), shsAgreement.getUuid());
		
		tester.startPage(EditAgreementPage.class, pageParameters);
		FormTester formTester = tester.newFormTester("agreementPanel:agreementForm");
		formTester.setValue("control.xml:xml", "Not xml");
		formTester.submit();
		
		tester.assertRenderedPage(EditAgreementPage.class);
		
		tester.assertErrorMessages("The supplied agreement xml is invalid");
	}
}
