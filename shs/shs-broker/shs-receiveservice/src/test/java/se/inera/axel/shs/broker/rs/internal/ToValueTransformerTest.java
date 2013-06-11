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
package se.inera.axel.shs.broker.rs.internal;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.directory.DirectoryService;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.rs.internal.ToValueTransformer;
import se.inera.axel.shs.exception.UnknownReceiverException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.To;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class ToValueTransformerTest {
	private static final String ACTOR_NAME = "actorName";
	private DirectoryService directoryService;
	private Organization organization;
	private ShsMessage shsMessage;
	
	@Test
	public void processShouldSetCommonNameInTo() throws Exception {
		
		when(directoryService.getOrganization(anyString())).thenReturn(organization);

		ToValueTransformer toValueTransformer = new ToValueTransformer();
		toValueTransformer.setDirectoryService(directoryService);
		
		ShsMessage returnedMessage = toValueTransformer.process(shsMessage);

		assertEquals(returnedMessage.getLabel().getTo().getCommonName(), ACTOR_NAME);
	}
	
	@Test
	public void commonNameShouldNotBeModifiedIfOrgNameInDirectoryIsBlank() throws Exception {
		organization.setOrgName(" ");
		shsMessage.getLabel().getTo().setCommonName("DoNotChange");
		when(directoryService.getOrganization(anyString())).thenReturn(organization);

		ToValueTransformer toValueTransformer = new ToValueTransformer();
		toValueTransformer.setDirectoryService(directoryService);
		
		ShsMessage returnedMessage = toValueTransformer.process(shsMessage);

		assertEquals(returnedMessage.getLabel().getTo().getCommonName(), "DoNotChange");
	}
	
	
	@Test(expectedExceptions=UnknownReceiverException.class)
	public void processShouldThrowUnknownReceiverIfActorIsNotFoundInDirectory() throws Exception {
		
		ToValueTransformer toValueTransformer = new ToValueTransformer();
		toValueTransformer.setDirectoryService(directoryService);
		
		ShsMessage returnedMessage = toValueTransformer.process(shsMessage);

		assertEquals(returnedMessage.getLabel().getTo().getCommonName(), ACTOR_NAME);
	}
	
	@Test
	public void shouldNotThrowWhenToIsNull() throws Exception {
		shsMessage.getLabel().setTo(null);
		
		ToValueTransformer toValueTransformer = new ToValueTransformer();
		toValueTransformer.setDirectoryService(directoryService);
		
		ShsMessage returnedMessage = toValueTransformer.process(shsMessage);
		
		assertNull(returnedMessage.getLabel().getTo(), "To should not be added if it is null");
		
	}
	
	@Test
	public void shouldNotThrowWhenToValueIsBlank() throws Exception {
		shsMessage.getLabel().getTo().setvalue("");
		
		ToValueTransformer toValueTransformer = new ToValueTransformer();
		toValueTransformer.setDirectoryService(directoryService);
		
		toValueTransformer.process(shsMessage);
	}
	
	@Test
	public void shouldNotThrowWhenToValueIsNull() throws Exception {
		shsMessage.getLabel().getTo().setvalue(null);
		
		ToValueTransformer toValueTransformer = new ToValueTransformer();
		toValueTransformer.setDirectoryService(directoryService);
		
		toValueTransformer.process(shsMessage);
	}

	@BeforeMethod
	public void beforeMethod() {
		directoryService = mock(DirectoryService.class);
		organization = new Organization();
		organization.setOrgName(ACTOR_NAME);
		
		shsMessage = new ShsMessage();
		ShsLabel label = new ShsLabel();
		To to = new To();
		to.setvalue("0000000000");
		label.setTo(to);
		shsMessage.setLabel(label);
	}

}
