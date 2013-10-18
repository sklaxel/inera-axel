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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;

import se.inera.axel.shs.broker.webconsole.base.BasePage;

/**
 * List LDAP Directory
 */
@PaxWicketMountPoint(mountPoint = "/shs/directory/actor/edit")
public class ActorEditPage extends BasePage {
	private static final long serialVersionUID = 1L;

	private static final String EDIT_PANEL = "editpanel";

	public ActorEditPage(final PageParameters params) {
		super(params);

		String type = params.get("type").toString();
		if (StringUtils.isNotBlank(type)) {
			if (type.equals("organization")) {
				add(new ActorEditFormPanel(EDIT_PANEL, params));
			} else if (type.equals("product")) {
				add(new ProductTypeEditPanel(EDIT_PANEL, params));
			} else if (type.equals("address")) {
				add(new AddressEditPanel(EDIT_PANEL, params));
			} else if (type.equals("agreement")) {
				add(new AgreementEditPanel(EDIT_PANEL, params));
			}
		}
	}
}
