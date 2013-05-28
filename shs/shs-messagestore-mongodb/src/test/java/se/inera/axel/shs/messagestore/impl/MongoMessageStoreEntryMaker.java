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


import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;
import se.inera.axel.shs.messagestore.MessageState;

import java.util.Date;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.Property.newProperty;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabel;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class MongoMessageStoreEntryMaker {
    public static class MongoMessageStoreEntryInstantiator implements Instantiator<MongoMessageLogEntry> {
        public static final Property<MongoMessageLogEntry, se.inera.axel.shs.xml.label.ShsLabel> label = newProperty();
        public static final Property<MongoMessageLogEntry, MessageState> state = newProperty();
        public static final Property<MongoMessageLogEntry, Date> stateTimeStamp = newProperty();

        @Override
        public MongoMessageLogEntry instantiate(
                PropertyLookup<MongoMessageLogEntry> lookup) {
            MongoMessageLogEntry mongoMessageLogEntry =
                    MongoMessageLogEntry.createNewEntry(lookup.valueOf(label, a(ShsLabel)));

            mongoMessageLogEntry.setState(lookup.valueOf(state, MessageState.NEW));
            mongoMessageLogEntry.setStateTimeStamp(lookup.valueOf(stateTimeStamp, new Date()));

            return mongoMessageLogEntry;
        }
    }

    public static final MongoMessageStoreEntryInstantiator MongoMessageStoreEntry = new MongoMessageStoreEntryInstantiator();
}
