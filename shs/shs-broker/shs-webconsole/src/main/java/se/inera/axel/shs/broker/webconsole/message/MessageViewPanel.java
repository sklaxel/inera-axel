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

import com.google.common.collect.Lists;
import org.apache.wicket.Component;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import se.inera.axel.shs.broker.messagestore.MessageLogAdminService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.broker.webconsole.common.Constant;
import se.inera.axel.shs.xml.label.Data;
import se.inera.axel.shs.xml.label.History;
import se.inera.axel.shs.xml.label.Meta;

import javax.inject.Inject;
import javax.inject.Named;

public class MessageViewPanel extends Panel {

    @Inject
    @Named("messageLogAdminService")
    @SpringBean(name = "messageLogAdminService")
    MessageLogAdminService messageLogAdminService;

	public MessageViewPanel(String id, IModel<ShsMessageEntry> messageModel) {
		super(id, new CompoundPropertyModel<ShsMessageEntry>(messageModel));


        add(new Label("label.txId"));
        add(new Label("label.to.value"));
        add(new Label("label.from.value"));
        add(new Label("label.endRecipient.value"));
        add(new Label("label.originator.value"));

        add(new Label("label.corrId"));
        add(new Label("label.transferType"));
        add(new Label("label.messageType"));
        add(new Label("label.sequenceType"));

        add(new Label("label.status"));
        add(new Label("label.product.value"));
        add(new Label("label.subject"));
        add(DateLabel.forDatePattern("label.datetime", Constant.DATETIME_FORMAT));

        add(new Label("label.content.contentId"));
        add(new Label("label.content.comment"));

        add(new ListView<Object>("label.content.dataOrCompound") {
            @Override
            protected void populateItem(ListItem<Object> item) {
                if (item.getModelObject() instanceof Data) {
                    item.setModel(new CompoundPropertyModel<Object>(item.getModelObject()));
                    item.add(new Label("index", "" + (item.getIndex() + 1)));
                    item.add(new Label("datapartType"));
                    item.add(new Label("filename"));
                } else {
                    item.add(new Label("index", "" + (item.getIndex() + 1)));
                    item.add(new Label("datapartType", "N/A"));
                    item.add(new Label("filename", "N/A"));
                }
            }
        });

        add(new ListView<Meta>("label.meta") {
            @Override
            protected void populateItem(ListItem<Meta> item) {
                item.setModel(new CompoundPropertyModel<Meta>(item.getModelObject()));
                item.add(new Label("name"));
                item.add(new Label("value"));
            }
        });

        add(new ListView<History>("label.history") {
            @Override
            protected void populateItem(ListItem<History> item) {
                item.setModel(new CompoundPropertyModel<History>(item.getModelObject()));
                item.add(DateLabel.forDatePattern("datetime", Constant.DATETIME_FORMAT));
                item.add(new Label("localId"));
                item.add(new Label("comment"));
                item.add(new Label("nodeId"));
            }
        });

        final int maxRelatedEntries = 40;
		add(new ListView<ShsMessageEntry>("related",
                Lists.newArrayList(messageLogAdminService.findRelatedEntries(messageModel.getObject(), maxRelatedEntries )))
        {
            @Override
            protected void populateItem(ListItem<ShsMessageEntry> item) {
                item.setModel(new CompoundPropertyModel<ShsMessageEntry>(item.getModelObject()));
                String messageId = item.getModelObject().getId();
                item.add(labelWithLink(new Label("label.txId"), messageId));
                item.add(labelWithLink(new Label("label.product.value"), messageId));
                item.add(labelWithLink(DateLabel.forDatePattern("stateTimeStamp", Constant.DATETIME_FORMAT), messageId));
                item.add(labelWithLink(new Label("state"), messageId));
                item.add(labelWithLink(new Label("acknowledged"), messageId));
            }
        });

	}

	private static final long serialVersionUID = 1L;
    protected Component labelWithLink(Label label, String messageId) {
    		PageParameters params = new PageParameters();
    		params.add("messageId", messageId);
    		Link<Void> link = new BookmarkablePageLink<Void>(label.getId() + ".link",
    				MessagePage.class, params);
    		link.add(label);
    		return link;
    	}
}
