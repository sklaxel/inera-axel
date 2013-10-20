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
package se.inera.axel.webconsole;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import se.inera.axel.shs.broker.webconsole.agreement.AgreementPage;
import se.inera.axel.shs.broker.webconsole.agreement.EditAgreementPage;
import se.inera.axel.shs.broker.webconsole.directory.ActorEditPage;
import se.inera.axel.shs.broker.webconsole.directory.ActorPage;
import se.inera.axel.shs.broker.webconsole.directory.DirectoryPage;
import se.inera.axel.shs.broker.webconsole.message.MessageListPage;
import se.inera.axel.shs.broker.webconsole.message.MessagePage;
import se.inera.axel.shs.broker.webconsole.product.EditProductPage;
import se.inera.axel.shs.broker.webconsole.product.ProductPage;


public class WicketApplication extends WebApplication {

	@Override
	public Class<HomePage> getHomePage() {
		return HomePage.class;
	}

	@Override
	public void init() {
        super.init();
        SpringComponentInjector injector = new SpringComponentInjector(this);
        getComponentInstantiationListeners().add(injector);


        mountPage("/agreements", AgreementPage.class);
        mountPage("/agreement/edit", EditAgreementPage.class);

        mountPage("/directory/actor/edit", ActorEditPage.class);
        mountPage("/directory/actor/view", ActorPage.class);
        mountPage("/directory", DirectoryPage.class);

        mountPage("/products", ProductPage.class);
        mountPage("/product/edit", EditProductPage.class);

        mountPage("/message/view", MessagePage.class);
        mountPage("/message/list", MessageListPage.class);

	}

}
