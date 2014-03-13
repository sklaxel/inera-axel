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

import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.broker.webconsole.common.Constant;

public class StatusPanel extends Panel {

	public StatusPanel(String id, IModel<ShsMessageEntry> messageModel) {
		super(id, new CompoundPropertyModel<ShsMessageEntry>(messageModel));

		add(new Label("id"));
        add(new Label("state"));
        add(DateLabel.forDatePattern("stateTimeStamp", Constant.DATETIME_FORMAT));
        add(DateLabel.forDatePattern("arrivalTimeStamp", Constant.DATETIME_FORMAT));
        add(new Label("statusCode"));
        add(new Label("statusText"));
        add(new Label("acknowledged"));

	}

	private static final long serialVersionUID = 1L;

}
