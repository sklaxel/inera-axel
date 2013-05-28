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
package se.inera.axel.shs.admin.product;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import se.inera.axel.shs.admin.ObjectMother;
import se.inera.axel.shs.admin.base.AbstractPageTest;
import se.inera.axel.shs.product.ProductAdminService;
import se.inera.axel.shs.routing.ShsRouter;
import se.inera.axel.shs.xml.product.ShsProduct;

public class ProductPageTest extends AbstractPageTest {
	private final static Logger log = LoggerFactory.getLogger(ProductPageTest.class);
	
	private ShsProduct shsProduct = null;

    @Override
    protected void beforeMethodSetup() {
        ShsRouter shsRouter = mock(ShsRouter.class);
        when(shsRouter.getOrgId()).thenReturn("0000000000");
        injector.registerBean("shsRouter", shsRouter);

        ProductAdminService productAdminService = mock(ProductAdminService.class);
        shsProduct = ObjectMother.createShsProduct();
        when(productAdminService.findAll()).thenReturn(Arrays.asList(shsProduct));
        injector.registerBean("productService", productAdminService);

    }

	@Test
	public void testRenderProductPage() {
		tester.startPage(ProductPage.class);
		
		tester.assertRenderedPage(ProductPage.class);
		
		tester.assertNoErrorMessage();
	}
	
	@Test
	public void whenAProductIsClickedTheEditPageShouldBeInEditMode() {
		tester.startPage(ProductPage.class);
		
		tester.assertNoErrorMessage();
		
		tester.clickLink("list:list:1:commonName.link");
		
		tester.assertRenderedPage(EditProductPage.class);
		
		tester.assertNoErrorMessage();
		
		tester.assertDisabled("productPanel:productForm:control.uuid:uuid");
	}
}
