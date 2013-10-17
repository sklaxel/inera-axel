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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.directory.DirectoryService;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.exception.UnknownReceiverException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.From;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.To;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class CommonNameTransformerTest {
	private static final String ACTOR_NAME = "actorName";
	private DirectoryService directoryService;
	private Organization organization;
	private ShsMessage shsMessage;
	
	@Test
	public void processShouldSetCommonNameInTo() throws Exception {
		
		when(directoryService.getOrganization(anyString())).thenReturn(organization);

		CommonNameTransformer commonNameTransformer = new CommonNameTransformer();
		commonNameTransformer.setDirectoryService(directoryService);
		
		commonNameTransformer.addCommonName(shsMessage.getLabel());

		assertEquals(shsMessage.getLabel().getTo().getCommonName(), ACTOR_NAME);
	}
	
	@Test
	public void commonNameShouldNotBeModifiedIfOrgNameInDirectoryIsBlank() throws Exception {
		organization.setOrgName(" ");
		shsMessage.getLabel().getTo().setCommonName("DoNotChange");
		when(directoryService.getOrganization(anyString())).thenReturn(organization);

		CommonNameTransformer commonNameTransformer = new CommonNameTransformer();
		commonNameTransformer.setDirectoryService(directoryService);
		
		ShsMessage returnedMessage = commonNameTransformer.addCommonName(shsMessage);

		assertEquals(returnedMessage.getLabel().getTo().getCommonName(), "DoNotChange");
	}
	
	
	@Test(expectedExceptions=UnknownReceiverException.class)
	public void processShouldThrowUnknownReceiverIfActorIsNotFoundInDirectory() throws Exception {
		
		CommonNameTransformer commonNameTransformer = new CommonNameTransformer();
		commonNameTransformer.setDirectoryService(directoryService);
		
		ShsMessage returnedMessage = commonNameTransformer.addCommonName(shsMessage);

		assertEquals(returnedMessage.getLabel().getTo().getCommonName(), ACTOR_NAME);
	}
	
	@Test(expectedExceptions = UnknownReceiverException.class)
	public void shouldThrowWhenToIsNull() throws Exception {
		shsMessage.getLabel().setTo(null);
		
		CommonNameTransformer commonNameTransformer = new CommonNameTransformer();
		commonNameTransformer.setDirectoryService(directoryService);
		
		ShsMessage returnedMessage = commonNameTransformer.addCommonName(shsMessage);
		
		assertNull(returnedMessage.getLabel().getTo(), "To should not be added if it is null");
		
	}
	
	@Test(expectedExceptions = UnknownReceiverException.class)
	public void shouldThrowWhenToValueIsBlank() throws Exception {
		shsMessage.getLabel().getTo().setValue("");
		
		CommonNameTransformer commonNameTransformer = new CommonNameTransformer();
		commonNameTransformer.setDirectoryService(directoryService);
		
		commonNameTransformer.addCommonName(shsMessage);
	}
	
	@Test(expectedExceptions = UnknownReceiverException.class)
	public void shouldThrowWhenToValueIsNull() throws Exception {
		shsMessage.getLabel().getTo().setValue(null);
		
		CommonNameTransformer commonNameTransformer = new CommonNameTransformer();
		commonNameTransformer.setDirectoryService(directoryService);
		
		commonNameTransformer.addCommonName(shsMessage);
	}

	@BeforeMethod
	public void beforeMethod() {
		directoryService = mock(DirectoryService.class);
		organization = new Organization();
		organization.setOrgName(ACTOR_NAME);
		
		shsMessage = new ShsMessage();
		ShsLabel label = new ShsLabel();
		To to = new To();
		to.setValue("0000000000");
        label.setTo(to);

        From from = new From();
        from.setValue("1111111111");
        label.getOriginatorOrFrom().add(from);

        shsMessage.setLabel(label);

	}

}
