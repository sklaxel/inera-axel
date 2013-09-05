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
package se.inera.axel.shs.broker.rs.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.broker.routing.ShsRouter;
import se.inera.axel.shs.xml.label.From;
import se.inera.axel.shs.xml.label.ShsLabel;

public class FromValueTransformer {
	private static final Logger log = LoggerFactory.getLogger(FromValueTransformer.class);
	
	private ShsRouter router = null; 
	
	public ShsRouter getRouter() {
		return router;
	}

	public void setRouter(ShsRouter router) {
		this.router = router;
	}

	public ShsMessage process(ShsMessage shsMessage) throws Exception {
		log.debug("Got ShsMessage body {}", shsMessage);

        ShsLabel label = shsMessage.getLabel();

        addFromIfEmpty(label);

		return shsMessage;
	}

    public ShsMessageEntry process(ShsMessageEntry entry) throws Exception {
        log.debug("Got ShsMessageEntry body {}", entry);

        ShsLabel label = entry.getLabel();

        addFromIfEmpty(label);

        return entry;
    }

    private void addFromIfEmpty(ShsLabel label) {
        From from = label.getFrom();

        if (from == null) {
            log.debug("from == null creating From");
            from = new From();
            from.setValue(router.getOrgId());
            from.setCommonName("");
            label.getOriginatorOrFrom().add(from);
        }
    }
}