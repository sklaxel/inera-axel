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
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceAggregator;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.webconsole.ObjectMother;
import se.inera.axel.shs.broker.webconsole.base.AbstractPageTest;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.product.ProductAdminService;
import se.inera.axel.shs.broker.routing.ShsRouter;
import se.inera.axel.shs.processor.ShsProductMarshaller;
import se.inera.axel.shs.xml.product.ShsProduct;

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;
import static se.inera.axel.shs.broker.webconsole.base.AdminPageParameters.*;

public class EditProductPageTest extends AbstractPageTest {
	private final static Logger log = LoggerFactory.getLogger(EditProductPage.class);
	private ShsProduct shsProduct = null;
	private ShsProductMarshaller productMarshaller = null;

    @Override
    protected void beforeMethodSetup() {
        ShsRouter shsRouter = mock(ShsRouter.class);
        when(shsRouter.getOrgId()).thenReturn("0000000000");
        injector.registerBean("shsRouter", shsRouter);

        ProductAdminService productAdminService = mock(ProductAdminService.class);
        shsProduct = ObjectMother.createShsProduct();
        when(productAdminService.getProduct(shsProduct.getUuid())).thenReturn(shsProduct);
        when(productAdminService.findAll()).thenReturn(Arrays.asList(shsProduct));
        injector.registerBean("productService", productAdminService);

        DirectoryAdminServiceAggregator directoryAdminServiceAggregator = mock(DirectoryAdminServiceAggregator.class);
        Organization organization = new Organization();
        organization.setOrgName("Test organization");
        organization.setOrgNumber("0000000000");
        when(directoryAdminServiceAggregator.getOrganizations()).thenReturn(Arrays.asList(organization));

        DirectoryAdminServiceRegistry directoryAdminServiceRegistry = mock(DirectoryAdminServiceRegistry.class);

        when(directoryAdminServiceRegistry.getDirectoryAdminServiceAggregator()).thenReturn(directoryAdminServiceAggregator);

        injector.registerBean("directoryAdminServiceRegistry", directoryAdminServiceRegistry);

        productMarshaller = new ShsProductMarshaller();
    }

	@Test
	@SuppressWarnings("unchecked")
	public void testRenderEdit() {
		PageParameters pageParameters = new PageParameters();
		pageParameters.set(CURRENT_UUID.toString(), shsProduct.getUuid());
		pageParameters.set(EDIT_MODE.toString(), true);
		tester.startPage(EditProductPage.class, pageParameters);
		
		tester.assertRenderedPage(EditProductPage.class);
		
		tester.assertNoErrorMessage();
		tester.assertComponent("productPanel:feedback", FeedbackPanel.class);
		tester.assertComponent("productPanel:productForm", Form.class);
		
		tester.assertComponent("productPanel:productForm:control.uuid:uuid", TextField.class);
        
		final TextField<String> uuidField = (TextField<String>) tester.getComponentFromLastRenderedPage("productPanel:productForm:control.uuid:uuid");
		assertEquals(uuidField.getModelObject(), shsProduct.getUuid());
		assertFalse(uuidField.isEnabled(), "uuid was not disabled when editing an existing product");
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testRenderNew() {
		PageParameters pageParameters = new PageParameters();
		tester.startPage(EditProductPage.class, pageParameters);
		
		tester.assertRenderedPage(EditProductPage.class);
		
		tester.assertNoErrorMessage();
		tester.assertComponent("productPanel:feedback", FeedbackPanel.class);
		tester.assertComponent("productPanel:productForm", Form.class);
		
		tester.assertComponent("productPanel:productForm:control.uuid:uuid", TextField.class);
        
		final TextField<String> uuidField = (TextField<String>) tester.getComponentFromLastRenderedPage("productPanel:productForm:control.uuid:uuid");
		assertTrue(uuidField.isEnabled(), "uuid should be enabled when creating a new product");
	}
	
	@Test(enabled=true)
	public void testCreateNew() {
		tester.startPage(EditProductPage.class, new PageParameters());
		FormTester formTester = tester.newFormTester("productPanel:productForm");
		setMandatoryFormParameters(formTester);
		formTester.submitLink("submit", false);
		
		tester.assertRenderedPage(ProductPage.class);
		
		tester.assertNoErrorMessage();
	}

	private void setMandatoryFormParameters(FormTester formTester) {
		formTester.setValue("control.uuid:uuid", java.util.UUID.randomUUID().toString());
		formTester.setValue("control.principal:principal", "0000000000");
	}
	
	@Test
	public void testCreateNewWithExistingUuid() {
		tester.startPage(EditProductPage.class, new PageParameters());
		FormTester formTester = tester.newFormTester("productPanel:productForm");
		setMandatoryFormParameters(formTester);
		formTester.setValue("control.uuid:uuid", shsProduct.getUuid());
		formTester.submitLink("submit", false);
		
		tester.assertRenderedPage(EditProductPage.class);
		tester.assertComponent("productPanel:feedback", FeedbackPanel.class);
		
		tester.assertErrorMessages("A product with the UUID " + shsProduct.getUuid() + " already exists");
		
	}
	
	@Test
	public void testRenderNewXmlView() {
		PageParameters pageParameters = new PageParameters();
		pageParameters.set(VIEW.toString(), "xml");
		
		tester.startPage(EditProductPage.class, pageParameters);
		
		tester.assertNoErrorMessage();
		tester.assertComponent("productPanel:feedback", FeedbackPanel.class);
		tester.assertComponent("productPanel:productForm", Form.class);
		tester.assertComponent("productPanel:productForm:uuid", HiddenField.class);
		tester.assertComponent("productPanel:productForm:control.xml:xml", TextArea.class);
		
	}
	
	@Test
	public void testRenderEditXmlView() {
		PageParameters pageParameters = new PageParameters();
		pageParameters.set(VIEW.toString(), "xml");
		pageParameters.set(EDIT_MODE.toString(), true);
		pageParameters.set(CURRENT_UUID.toString(), shsProduct.getUuid());
		
		tester.startPage(EditProductPage.class, pageParameters);
		
		tester.assertNoErrorMessage();
		tester.assertComponent("productPanel:feedback", FeedbackPanel.class);
		tester.assertComponent("productPanel:productForm", Form.class);
		tester.assertComponent("productPanel:productForm:uuid", HiddenField.class);
		tester.assertComponent("productPanel:productForm:control.xml:xml", TextArea.class);
		
		final TextArea<String> xmlField = (TextArea<String>) tester.getComponentFromLastRenderedPage("productPanel:productForm:control.xml:xml");
		assertThat(xmlField.getValue(), containsString(shsProduct.getUuid()));
	}
	
	@Test
	public void testCreateNewXmlView() {
		PageParameters pageParameters = new PageParameters();
		pageParameters.set(VIEW.toString(), "xml");
		tester.startPage(EditProductPage.class, pageParameters);
		FormTester formTester = tester.newFormTester("productPanel:productForm");
		shsProduct.setUuid(UUID.randomUUID().toString());
		formTester.setValue("control.xml:xml", productMarshaller.marshal(shsProduct));
		formTester.submitLink("submit", false);
		
		tester.assertRenderedPage(ProductPage.class);
		
		tester.assertNoErrorMessage();
	}
	
	@Test
	public void testCreateNewXmlViewWithExistingProduct() {
		PageParameters pageParameters = new PageParameters();
		pageParameters.set(VIEW.toString(), "xml");
		tester.startPage(EditProductPage.class, pageParameters);
		FormTester formTester = tester.newFormTester("productPanel:productForm");
		formTester.setValue("control.xml:xml", productMarshaller.marshal(shsProduct));
		formTester.submitLink("submit", false);
		
		tester.assertRenderedPage(EditProductPage.class);
		
		tester.assertErrorMessages("A product with the UUID " + shsProduct.getUuid() + " already exists");
	}

	@Test
	public void testEditProductXmlView() {
		PageParameters pageParameters = new PageParameters();
		pageParameters.set(VIEW.toString(), "xml");
		pageParameters.set(EDIT_MODE.toString(), true);
		pageParameters.set(CURRENT_UUID.toString(), shsProduct.getUuid());
		
		tester.startPage(EditProductPage.class, pageParameters);
		FormTester formTester = tester.newFormTester("productPanel:productForm");
		shsProduct.setCommonName("New common name");
		formTester.setValue("control.xml:xml", productMarshaller.marshal(shsProduct));
		formTester.submitLink("submit", false);
		
		tester.assertRenderedPage(ProductPage.class);
		
		tester.assertNoErrorMessage();
	}
	
	@Test
	public void testChangeUUIDXmlView() {
		PageParameters pageParameters = new PageParameters();
		pageParameters.set(VIEW.toString(), "xml");
		pageParameters.set(EDIT_MODE.toString(), true);
		pageParameters.set(CURRENT_UUID.toString(), shsProduct.getUuid());
		
		tester.startPage(EditProductPage.class, pageParameters);
		FormTester formTester = tester.newFormTester("productPanel:productForm");
		shsProduct.setUuid(UUID.randomUUID().toString());
		formTester.setValue("control.xml:xml", productMarshaller.marshal(shsProduct));
		formTester.submitLink("submit", false);
		
		tester.assertRenderedPage(EditProductPage.class);
		
		tester.assertErrorMessages("UUID must not be updated when editing an existing product. Original uuid " + ObjectMother.DEFAULT_PRODUCT_ID);
	}
	
	@Test
	public void testSubmitInvalidXml() {
		PageParameters pageParameters = new PageParameters();
		pageParameters.set(VIEW.toString(), "xml");
		pageParameters.set(EDIT_MODE.toString(), true);
		pageParameters.set(CURRENT_UUID.toString(), shsProduct.getUuid());
		
		tester.startPage(EditProductPage.class, pageParameters);
		FormTester formTester = tester.newFormTester("productPanel:productForm");
		formTester.setValue("control.xml:xml", "Not xml");
		formTester.submitLink("submit", false);
		
		tester.assertRenderedPage(EditProductPage.class);
		
		tester.assertErrorMessages("The supplied product xml is invalid");
	}
	
}
