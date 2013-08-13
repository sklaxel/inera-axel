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
package se.inera.axel.shs.broker.webconsole.message;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import se.inera.axel.shs.broker.messagestore.MessageLogAdminService;

public class CriteriaPanel extends Panel {
	private static final long serialVersionUID = 1L;

	public CriteriaPanel(String id, MessageLogAdminService.Filter filter) {
		super(id);

        Form criteriaForm = new Form("criteriaForm", new CompoundPropertyModel<MessageLogAdminService.Filter>(filter)) {
            @Override
            protected void onSubmit() {
                CriteriaPanel.this.onSubmit();
            }
        };

        criteriaForm.add(new TextField<String>("corrId"));
        criteriaForm.add(new TextField<String>("from"));
        criteriaForm.add(new TextField<String>("to"));
        criteriaForm.add(new TextField<String>("txId"));
        criteriaForm.add(new TextField<String>("filename"));
        criteriaForm.add(new TextField<String>("product"));


        Button searchButton = new Button("searchButton") {
            @Override
            public void onSubmit() {
                CriteriaPanel.this.onSubmit();
            }
        };
        criteriaForm.add(searchButton);
        criteriaForm.setDefaultButton(searchButton);

        add(criteriaForm);
	}

    public void onSubmit() {
    }

}
