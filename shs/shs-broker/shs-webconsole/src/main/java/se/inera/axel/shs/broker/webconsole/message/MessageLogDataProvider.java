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
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.messagestore.MessageLogAdminService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;

import java.util.Iterator;
import java.util.List;

public class MessageLogDataProvider implements IDataProvider<ShsMessageEntry> {

	private static final long serialVersionUID = 1L;
    MessageLogAdminService messageLogAdminService;
	private List<ShsMessageEntry> messageEntries;

	public MessageLogDataProvider(
            MessageLogAdminService messageLogAdminService) {
		super();
		this.messageLogAdminService = messageLogAdminService;
	}

	@Override
	public void detach() {
        messageEntries = null;
	}

	@Override
	public Iterator<ShsMessageEntry> iterator(int first, int count) {
		if (messageEntries == null) {
            messageEntries = Lists.newArrayList(messageLogAdminService.listMessages(""));
		}
		return messageEntries.subList(first, first + count).iterator();
	}

	@Override
	public int size() {
		if (messageEntries == null) {
            messageEntries = Lists.newArrayList(messageLogAdminService.listMessages(""));
		}
		return messageEntries.size();
	}

	@Override
	public IModel<ShsMessageEntry> model(ShsMessageEntry messageEntry) {
		return new CompoundPropertyModel<ShsMessageEntry>(messageEntry);
	}

}
