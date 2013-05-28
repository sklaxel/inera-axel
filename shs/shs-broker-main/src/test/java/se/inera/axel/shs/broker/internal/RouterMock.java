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
package se.inera.axel.shs.broker.internal;

import se.inera.axel.shs.exception.MissingDeliveryAddressException;
import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.routing.ShsRouter;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.util.List;


public class RouterMock implements ShsRouter {
	@Override
	public List<String> resolveRecipients(ShsLabel label) throws ShsException {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String resolveEndpoint(ShsLabel label) throws MissingDeliveryAddressException {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Boolean isLocal(ShsLabel label) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String getOrgId() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
