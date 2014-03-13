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
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import se.inera.axel.shs.broker.messagestore.MessageLogAdminService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.broker.webconsole.common.Constant;

import javax.inject.Inject;
import javax.inject.Named;

public class  MessageListPanel extends Panel {
	private static final long serialVersionUID = 1L;

    @Inject
    @Named("messageLogAdminService")
    @SpringBean(name = "messageLogAdminService")
    MessageLogAdminService messageLogAdminService;

    DataView<ShsMessageEntry> dataView;
    MessageLogDataProvider listData;
    Label messageCountLabel;

	public MessageListPanel(String id, MessageLogAdminService.Filter filter) {
		super(id);

		listData = new MessageLogDataProvider(filter);
		dataView = new DataView<ShsMessageEntry>("list", listData) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(Item<ShsMessageEntry> item) {
				String messageId = item.getModelObject().getId();
				item.add(labelWithLink(new Label("label.txId"), messageId));
                item.add(labelWithLink(new Label("label.from.value"), messageId));
                item.add(labelWithLink(new Label("label.to.value"), messageId));
                item.add(labelWithLink(new Label("label.product.value"), messageId));
                item.add(labelWithLink(DateLabel.forDatePattern("arrivalTimeStamp", Constant.DATETIME_FORMAT), messageId));
                item.add(labelWithLink(new Label("state"), messageId));
			}

		};
		add(dataView);

		dataView.setItemsPerPage(12);
		PagingNavigator pagingNavigator = new PagingNavigator(
				"messageNavigator", dataView);

		add(pagingNavigator);
        messageCountLabel = new Label("messageCount", new AbstractReadOnlyModel<Integer>() {
            @Override
            public Integer getObject() {
                return (int)listData.size();
            }
        });
        add(messageCountLabel);
	}

	protected Component labelWithLink(Label label, String messageId) {
		PageParameters params = new PageParameters();
		params.add("messageId", messageId);
		Link<Void> link = new BookmarkablePageLink<Void>(label.getId() + ".link",
				MessagePage.class, params);
		link.add(label);
		return link;
	}

    public void update() {
        listData.reload();
        messageCountLabel.modelChanged();
        dataView.modelChanged();
    }
}
