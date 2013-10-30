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
package se.inera.axel.shs.processor;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabel;

import java.io.IOException;

import javax.activation.DataHandler;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.management.ShsManagement;

import com.natpryce.makeiteasy.Maker;

/**
 * 
 * @author Ekkehart Lötzsch
 *
 */
public class ResponseMessageBuilderTest {
	Maker<ShsLabel> labelMaker;
	ResponseMessageBuilder responseMessageBuilder;
	ShsManagementMarshaller shsManagementMarshaller;

	@Test
	public void buildErrorMessage() throws IOException {
		ShsLabel requestLabel = make(labelMaker);
		
		ShsMessage shsMessage = responseMessageBuilder.buildErrorMessage(requestLabel, new Exception("TestException"));
		
		// Test label
        assertEquals(shsMessage.getLabel().getCorrId(), requestLabel.getCorrId());
        assertTrue(shsMessage.getLabel().getContent().getContentId().length() <= ShsMessageMarshaller.MAX_LENGTH_CONTENT_ID);

        // Test data part
        DataPart dataPart = shsMessage.getDataParts().get(0);
        DataHandler dataHandler = dataPart.getDataHandler();
        ShsManagement shsManagment = shsManagementMarshaller.unmarshal(dataHandler.getInputStream());
        assertEquals(shsManagment.getCorrId(), requestLabel.getCorrId());
        assertEquals(shsManagment.getContentId(), requestLabel.getContent().getContentId());
	}

	@Test
	public void buildConfirmMessage() throws IOException {
		ShsLabel requestLabel = make(labelMaker);
		
		ShsMessage shsMessage = responseMessageBuilder.buildConfirmMessage(requestLabel);
		
		// Test label
        assertEquals(shsMessage.getLabel().getCorrId(), requestLabel.getCorrId());
        assertTrue(shsMessage.getLabel().getContent().getContentId().length() <= 36);

		// Test data part
        DataPart dataPart = shsMessage.getDataParts().get(0);
        DataHandler dataHandler = dataPart.getDataHandler();
        ShsManagement shsManagment = shsManagementMarshaller.unmarshal(dataHandler.getInputStream());
        assertEquals(shsManagment.getCorrId(), requestLabel.getCorrId());
        assertEquals(shsManagment.getContentId(), requestLabel.getContent().getContentId());
	}

	@BeforeMethod
	public void beforeMethod() {
	}

	@AfterMethod
	public void afterMethod() {
	}

	@BeforeClass
	public void beforeClass() {
		labelMaker = a(ShsLabel);
		responseMessageBuilder = new ResponseMessageBuilder();
		shsManagementMarshaller = new ShsManagementMarshaller();
	}

	@AfterClass
	public void afterClass() {
	}
}
