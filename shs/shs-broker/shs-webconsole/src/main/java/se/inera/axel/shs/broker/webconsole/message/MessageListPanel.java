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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import se.inera.axel.shs.broker.messagestore.MessageLogAdminService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;

public class MessageListPanel extends Panel {
	private static final long serialVersionUID = 1L;

	@PaxWicketBean(name = "messageLogAdminService")
    @SpringBean(name = "messageLogAdminService")
    MessageLogAdminService messageLogAdminService;

	IDataProvider<ShsMessageEntry> listData;

	public MessageListPanel(String id) {
		super(id);

		listData = new MessageLogDataProvider(messageLogAdminService);
		DataView<ShsMessageEntry> dataView = new DataView<ShsMessageEntry>("list", listData) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(Item<ShsMessageEntry> item) {
				String messageId = item.getModelObject().getId();
				item.add(labelWithLink("label.txId", messageId));
                item.add(labelWithLink("label.from.value", messageId));
                item.add(labelWithLink("label.to.value", messageId));
                item.add(labelWithLink("label.product.value", messageId));
                item.add(labelWithLink("label.datetime", messageId));
                item.add(labelWithLink("state", messageId));
			}

		};
		add(dataView);

		dataView.setItemsPerPage(15);
		PagingNavigator pagingNavigator = new PagingNavigator(
				"messageNavigator", dataView);

		add(pagingNavigator);
	}

	protected Component labelWithLink(String labelId, String messageId) {
		PageParameters params = new PageParameters();
		params.add("messageId", messageId);
		Link<Void> link = new BookmarkablePageLink<Void>(labelId + ".link",
				MessagePage.class, params);
		link.add(new Label(labelId));
		return link;
	}
}
