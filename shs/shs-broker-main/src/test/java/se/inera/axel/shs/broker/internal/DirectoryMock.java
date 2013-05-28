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

import se.inera.axel.shs.directory.*;
import se.inera.axel.shs.protocol.ShsMessageTestObjectMother;

import java.util.List;

// TODO easymock or powermock or something?
public class DirectoryMock implements DirectoryService {

	@Override
	public Organization getOrganization(String orgNumber) {
		Organization org = null;

		if (ShsMessageTestObjectMother.DEFAULT_TEST_TO.equals(orgNumber)) {
			org = new Organization();
			org.setOrgName("Good Org");
			org.setOrgNumber(ShsMessageTestObjectMother.DEFAULT_TEST_TO);
			org.setDescription("Don't be evil");
		}

		return org;
	}

	@Override
	public Address getAddress(String orgNumber, String productId) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Agreement getAgreement(String orgNumber, String productId, String transferType) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public List<Agreement> findAgreements(String orgNumber, String productId) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public ProductType getProductType(String orgNumber, String productId) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
