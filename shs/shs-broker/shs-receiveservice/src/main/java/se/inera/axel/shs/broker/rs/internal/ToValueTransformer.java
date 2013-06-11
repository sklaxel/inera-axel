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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.broker.directory.DirectoryService;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.exception.UnknownReceiverException;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.To;

public class ToValueTransformer {
	private static final Logger log = LoggerFactory.getLogger(ToValueTransformer.class);
	
	private DirectoryService directoryService = null; 
	
	public DirectoryService getDirectoryService() {
		return directoryService;
	}

	public void setDirectoryService(DirectoryService directoryService) {
		this.directoryService = directoryService;
	}

	public ShsMessage process(ShsMessage shsMessage) throws Exception {
		log.debug("Got ShsMessage body {}", shsMessage);

        ShsLabel label = shsMessage.getLabel();

        validateTo(label);

		return shsMessage;
	}

    public ShsMessageEntry process(ShsMessageEntry entry) throws Exception {
        log.debug("Got ShsMessageEntry body {}", entry);

        ShsLabel label = entry.getLabel();

        validateTo(label);

        return entry;
    }

    // TODO this method has multiple responsibilities it both validates and updates the to value
    private void validateTo(ShsLabel label) {
        To to = label.getTo();

        if (to != null && !StringUtils.isBlank(to.getvalue())) {
            log.debug("to != null and not blank checking that the receiver exists");

            Organization organization = getDirectoryService().getOrganization(to.getvalue());

            if (organization == null)
                throw new UnknownReceiverException("No organization with organization number [" + to.getvalue() + "] found in directory");

            String commonName = organization.getOrgName();

            if (!StringUtils.isBlank(commonName))
                to.setCommonName(commonName);

        }
    }

}