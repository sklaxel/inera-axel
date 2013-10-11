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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import static org.testng.Assert.*;

public class ShsMessageMarshallerTest {
    Logger log = LoggerFactory.getLogger(ShsMessageMarshallerTest.class);

    URL shsErrorMessageMime = getClass().getResource("/shsErrorMessage.mime");
    URL shsTextMessageMime = getClass().getResource("/shsTextMessage.mime");
    URL shsTextMessageNoFileNameMime = getClass().getResource("/shsTextMessageNoFileName.mime");
    URL shsTextMessageSwedishFileNameMime = getClass().getResource("/shsTextMessageSwedishFileName.mime");

    ShsMessageMarshaller shsMessageMarshaller = new ShsMessageMarshaller();

    @Test
    public void unmarshalErrorMessageFromMimeStream() throws Exception {

        ShsMessage shsMessage = shsMessageMarshaller.unmarshal(shsErrorMessageMime.openStream());

        assertNotNull(shsMessage);
        assertNotNull(shsMessage.getLabel());
        assertEquals(shsMessage.getLabel().getTxId(), "4c9fd3e8-b4c4-49aa-926a-52a68864a7b8");

        assertNotNull(shsMessage.getDataParts());
        assertEquals(shsMessage.getDataParts().size(), 1);
        DataPart errorPart = shsMessage.getDataParts().get(0);

        assertEquals(errorPart.getContentType(), "text/xml; charset=us-ascii; name=error.xml");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        IOUtils.copy(errorPart.getDataHandler().getInputStream(), bos);
        String messageMime = bos.toString();

        log.debug("shs error message: " + messageMime);
        assertTrue(messageMime.contains("<!DOCTYPE shs.management SYSTEM \"shs-management-1.2.dtd\">"));
    }

    @Test
    public void unmarshalThenMarshallErrorMessageFromMimeStream() throws Exception {

        ShsMessage shsMessage = shsMessageMarshaller.unmarshal(shsErrorMessageMime.openStream());

        assertNotNull(shsMessage);
        assertNotNull(shsMessage.getLabel());
        assertEquals(shsMessage.getLabel().getTxId(), "4c9fd3e8-b4c4-49aa-926a-52a68864a7b8");

        assertNotNull(shsMessage.getDataParts());
        assertEquals(shsMessage.getDataParts().size(), 1);
        DataPart errorPart = shsMessage.getDataParts().get(0);

        assertEquals(errorPart.getContentType(), "text/xml; charset=us-ascii; name=error.xml");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        IOUtils.copy(errorPart.getDataHandler().getInputStream(), bos);
        String messageMime = bos.toString();

        assertTrue(messageMime.contains("<!DOCTYPE shs.management SYSTEM \"shs-management-1.2.dtd\">"));

        bos = new ByteArrayOutputStream();

        shsMessageMarshaller.marshal(shsMessage, bos);
        messageMime = bos.toString();

        assertTrue(messageMime.contains("<!DOCTYPE shs.management SYSTEM \"shs-management-1.2.dtd\">"));
    }

    @Test
    public void unmarshalThenMarshallTextMessageFromMimeStream() throws Exception {

        ShsMessage shsMessage = shsMessageMarshaller.unmarshal(shsTextMessageMime.openStream());

        assertNotNull(shsMessage);
        assertNotNull(shsMessage.getLabel());
        assertEquals(shsMessage.getLabel().getTxId(), "4c9fd3e8-b4c4-49aa-926a-52a68864a7b8");

        assertNotNull(shsMessage.getDataParts());
        assertEquals(shsMessage.getDataParts().size(), 1);
        DataPart dataPart = shsMessage.getDataParts().get(0);

        assertEquals(dataPart.getContentType(), "text/plain; charset=us-ascii; name=text.txt");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        IOUtils.copy(dataPart.getDataHandler().getInputStream(), bos);
        String messageMime = bos.toString();

        assertTrue(messageMime.contains("Lorem ipsum"), "Datapart doesnt contain expected text: " + messageMime);

        bos = new ByteArrayOutputStream();

        shsMessageMarshaller.marshal(shsMessage, bos);
        messageMime = bos.toString();

        assertTrue(messageMime.contains("text/plain; charset=us-ascii; name=text.txt"), "Marshalled mime doesnt contain expected content type");
        assertTrue(messageMime.contains("Lorem ipsum"), "Marshalled mime doesnt contain expected text");
    }

    @Test
    public void unmarshalThenMarshallTextMessageNoFileNameFromMimeStream() throws Exception {

        ShsMessage shsMessage = shsMessageMarshaller.unmarshal(shsTextMessageNoFileNameMime.openStream());

        assertNotNull(shsMessage.getDataParts());
        assertEquals(shsMessage.getDataParts().size(), 1);
        DataPart dataPart = shsMessage.getDataParts().get(0);

        assertNull(dataPart.getFileName(), "Data part should not have a file name");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        shsMessageMarshaller.marshal(shsMessage, bos);
        String messageMime = bos.toString();

        assertFalse(messageMime.contains("filename"), "Message should not contain filename");
    }

    @Test
    public void unmarshalThenMarshallTextMessageSwedishFileNameFromMimeStream() throws Exception {

        ShsMessage shsMessage = shsMessageMarshaller.unmarshal(shsTextMessageSwedishFileNameMime.openStream());

        assertNotNull(shsMessage.getDataParts());
        assertEquals(shsMessage.getDataParts().size(), 1);
        DataPart dataPart = shsMessage.getDataParts().get(0);

        assertNotNull(dataPart.getFileName(), "Data part should have a file name");
        assertEquals(dataPart.getFileName(), "filnamn-med-åäö.xml");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        shsMessageMarshaller.marshal(shsMessage, bos);
        String messageMime = bos.toString();

        assertTrue(messageMime.contains("filename"), "Message should not contain filename");
    }

}
