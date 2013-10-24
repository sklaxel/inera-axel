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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import se.inera.axel.shs.broker.directory.Organization;

public class ActorViewPanel extends Panel {

	public ActorViewPanel(String id, IModel<Organization> model) {
		super(id, new CompoundPropertyModel<Organization>(model));

        add(new BookmarkablePageLink<Void>("back", DirectoryPage.class));

        add(new Label("orgName"));
        add(new Label("streetAddress"));
        add(new Label("postalCode"));
        add(new Label("postalAddress"));
        add(new Label("postOfficeBox"));
        add(new Label("orgNumber"));
        add(new Label("description"));
        add(new Label("phoneNumber"));
        add(new Label("faxNumber"));
        add(new Label("labeledUri"));

        PageParameters editParams = new PageParameters();
        editParams.add("type", "organization");
        editParams.add("orgNumber", model.getObject().getOrgNumber());
        add(new BookmarkablePageLink<Void>("edit", ActorEditPage.class,
                editParams));
	}

	private static final long serialVersionUID = 1L;

}
