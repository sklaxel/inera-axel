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


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Add javascript and footer html
 */
public class FooterPanel extends Panel {
	private static final long serialVersionUID = 1L;

    @SpringBean(name = "nodeInfo")
    @Inject
    @Named("nodeInfo")
    NodeInfo nodeInfo;

    public FooterPanel(final String id) {
    	super(id);

        add(new Label("mavenVersion", nodeInfo.getMavenVersion()));
    }
}
