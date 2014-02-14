package se.inera.axel.shs.broker.messagestore.internal;

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

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.SerializationUtils;

public class ShsMessageMongoOperationTimeStamp implements Serializable {

    private Date removeSuccefullyTranferedMessagesTimeStamp;

    public ShsMessageMongoOperationTimeStamp() {
    	
    }

//	public static ShsMessageEntry newInstance(ShsMessageEntry shsMessageEntry) {
//		return (ShsMessageEntry) SerializationUtils.clone(shsMessageEntry);
//	}


    public void setRemoveSuccefullyTranferedMessagesTimeStamp (Date timeStamp) {
    	this.removeSuccefullyTranferedMessagesTimeStamp = timeStamp;
	}
    
    public Date getRemoveSuccefullyTranferedMessagesTimeStamp() {
    	return removeSuccefullyTranferedMessagesTimeStamp;
    }

	@Override
    public String toString() {
        return "ShsMessageMongoOperationTimeStamp{" +
                "removeSuccefullyTranferedMessagesTimeStamp=" + removeSuccefullyTranferedMessagesTimeStamp + '}';
    }

}
