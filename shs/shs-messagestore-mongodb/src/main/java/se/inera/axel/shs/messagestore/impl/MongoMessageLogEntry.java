/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.messagestore.impl;

import java.util.Date;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;

import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.messagestore.MessageState;
import se.inera.axel.shs.messagestore.ShsMessageEntry;
import se.inera.axel.shs.xml.label.ShsLabel;

public class MongoMessageLogEntry implements ShsMessageEntry {
	@Id
	private String id;
	
	private ShsLabel label;

	private MessageState state;
	
	private Date stateTimeStamp;
	
	private ShsException shsException;
	
	private int retries;
	
	@PersistenceConstructor
	protected MongoMessageLogEntry(String id, ShsLabel label) {
		this.id = id;
        this.label = label;
	}
	
	// TODO add more fields. What do we need?

	@Override
	public ShsLabel getLabel() {
		return label;
	}

	@Override
	public void setLabel(ShsLabel label) {
		this.label = label;
	}

	@Override
	public MessageState getState() {
		return state;
	}

	@Override
	public void setState(MessageState state) {
		this.state = state;
	}

	@Override
	public Date getStateTimeStamp() {
		return stateTimeStamp;
	}

	@Override
	public void setStateTimeStamp(Date stateTimeStamp) {
		this.stateTimeStamp = stateTimeStamp;
	}

	public String getId() {
		return id;
	}

	@Override
	public void setShsException(ShsException e) {
		this.shsException = e;
	}

	@Override
	public ShsException getShsException() {
		return this.shsException;
	}

	@Override
	public boolean isError() {
		return this.shsException != null;
	}

	@Override
	public int getRetries() {
		return retries;
	}

	@Override
	public void setRetries(int retries) {
		this.retries = retries;
	}
	
	public static MongoMessageLogEntry createNewEntry(ShsLabel label) {
		return new MongoMessageLogEntry(UUID.randomUUID().toString(), label);
	}

    @Override
    public String toString() {
        return "MongoMessageLogEntry{" +
                "id='" + id + '\'' +
                ", label=" + label +
                ", state=" + state +
                ", stateTimeStamp=" + stateTimeStamp +
                ", shsException=" + shsException +
                ", retries=" + retries +
                '}';
    }
}
