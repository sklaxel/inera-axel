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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import se.inera.axel.shs.broker.directory.DirectoryService;
import se.inera.axel.shs.broker.messagestore.MessageLogAdminService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.xml.label.Data;
import se.inera.axel.shs.xml.label.Meta;

public class MessageViewPanel extends Panel {

    @PaxWicketBean(name = "messageLogAdminService")
    @SpringBean(name = "messageLogAdminService")
    MessageLogAdminService messageLogAdminService;

	@PaxWicketBean(name = "ldapDirectoryService")
    @SpringBean(name = "directoryAdminService")
	DirectoryService ldapDirectoryService;

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
        add(new Label("label.datetime"));

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

        // TODO list related messages
//			PageParameters editParams = new PageParameters();
//			editParams.add("type", "organization");
//			editParams.add("orgNumber", orgNumber);
//			add(new BookmarkablePageLink<Void>("edit", ActorEditPage.class,
//					editParams));

	}

	private static final long serialVersionUID = 1L;

}
