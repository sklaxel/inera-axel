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
package se.inera.axel.shs.admin.directory;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;
import se.inera.axel.shs.admin.base.BasePage;

/**
 * List LDAP Directory
 */
@PaxWicketMountPoint(mountPoint = "/shs/directory/actor/view")
public class ActorPage extends BasePage {
	private static final long serialVersionUID = 1L;

	public ActorPage(final PageParameters parameters) {
		super(parameters);

		add(new ActorViewPanel("organization", parameters));
		add(new ProductListPanel("productlist", parameters));
		add(new AddressListPanel("addresslist", parameters));
		add(new AgreementListPanel("agreementlist", parameters));
	}
}
