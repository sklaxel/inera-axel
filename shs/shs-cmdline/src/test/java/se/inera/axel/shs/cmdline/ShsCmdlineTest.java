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
package se.inera.axel.shs.cmdline;

import com.beust.jcommander.JCommander;
import org.apache.camel.language.Bean;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class ShsCmdlineTest {
    private ShsCmdline cmdline;
    private JCommander jcommander;
    private ShsSendCommand sendCommand;
    private ShsRequestCommand requestCommand;
    private ShsFetchCommand fetchCommand;

    @BeforeMethod
    public void setUp() {
        cmdline = new ShsCmdline();
        jcommander = new JCommander(cmdline);
        sendCommand = new ShsSendCommand();
        requestCommand = new ShsRequestCommand();
        fetchCommand = new ShsFetchCommand();
        jcommander.addCommand("send", sendCommand);
        jcommander.addCommand("request", requestCommand);
        jcommander.addCommand("fetch", fetchCommand);
    }

    @Test
    public void listMessages() {
         jcommander.parse("fetch", "-l", "-t", "1111111111");

        assertEquals(jcommander.getParsedCommand(), "fetch");
        assertTrue(fetchCommand.listMode);
        assertEquals(fetchCommand.shsTo, "1111111111");
    }

    @Test
    public void fetchAll() {
        jcommander.parse("fetch", "-t", "1111111111");

        assertEquals(jcommander.getParsedCommand(), "fetch");
        assertFalse(fetchCommand.listMode);
        assertEquals(fetchCommand.shsTo, "1111111111");
    }

    @Test
    public void fetchWithRetrieveSingleFileOnly() {
        jcommander.parse("fetch", "-t", "1111111111", "-s", "-E", "endRecipientMail");

        assertEquals(jcommander.getParsedCommand(), "fetch");
        assertFalse(fetchCommand.listMode);
        assertEquals(fetchCommand.shsTo, "1111111111");
        assertEquals(fetchCommand.shsEndRecipient, "endRecipientMail");
    }

    @Test
    public void fetchSingleMessage() {
        jcommander.parse("fetch", "-t", "1111111111", "-i", "40b58f76-eb92-48bc-adf6-f9804b98c342");

        assertEquals(jcommander.getParsedCommand(), "fetch");
        assertFalse(fetchCommand.listMode);
        assertEquals(fetchCommand.shsTo, "1111111111");
        assertEquals(fetchCommand.shsTxId, "40b58f76-eb92-48bc-adf6-f9804b98c342");
    }
}
