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
package se.inera.axel.shs.broker.messagestore.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.ShsLabelMaker;
import se.inera.axel.shs.xml.label.Status;
import se.inera.axel.shs.xml.label.TransferType;

import java.util.GregorianCalendar;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessage;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.*;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.*;

public class AbstractMongoMessageLogTest extends AbstractTestNGSpringContextTests {

    @Autowired
    MessageLogService messageLogService;

    @BeforeMethod
    public void setupTestDB() {
        messageLogService.saveMessage(
                make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                        with(transferType, TransferType.ASYNCH))))));

        messageLogService.saveMessage(
                make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                        with(transferType, TransferType.ASYNCH))))));

        messageLogService.messageSent(
                messageLogService.saveMessage(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(transferType, TransferType.ASYNCH)))))));

        messageLogService.messageReceived(
                messageLogService.saveMessage(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(transferType, TransferType.SYNCH)))))));

        messageLogService.messageReceived(
                messageLogService.saveMessage(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(transferType, TransferType.ASYNCH)))))));

        messageLogService.messageReceived(
                messageLogService.saveMessage(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                                with(transferType, TransferType.ASYNCH)))))));

        messageLogService.messageReceived(
                messageLogService.saveMessage(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(product, a(Product, with(Product.value, "error"))),
                                with(sequenceType, SequenceType.ADM),
                                with(transferType, TransferType.ASYNCH)))))));

        messageLogService.messageReceived(
                messageLogService.saveMessage(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(product, a(Product, with(Product.value, "confirm"))),
                                with(sequenceType, SequenceType.ADM),
                                with(transferType, TransferType.ASYNCH)))))));


        messageLogService.messageReceived(
                messageLogService.saveMessage(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(transferType, TransferType.ASYNCH),
                                with(status, Status.TEST)))))));

        messageLogService.messageAcknowledged(messageLogService.messageReceived(
                messageLogService.saveMessage(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(transferType, TransferType.ASYNCH))))))));

        messageLogService.messageReceived(
                messageLogService.saveMessage(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                                with(endRecipient, a(EndRecipient, with(EndRecipient.value,
                                        ShsLabelMaker.DEFAULT_TEST_ENDRECIPIENT))),
                                with(transferType, TransferType.ASYNCH)))))));

        messageLogService.messageReceived(
                messageLogService.saveMessage(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                                with(corrId, "testing-corrid"),
                                with(transferType, TransferType.ASYNCH)))))));

        messageLogService.messageReceived(
                messageLogService.saveMessage(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                                with(transferType, TransferType.ASYNCH),
                                with(content, a(Content, with(Content.contentId, "testing-contentid")))))))));

        messageLogService.messageReceived(
                messageLogService.saveMessage(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                                with(transferType, TransferType.ASYNCH),
                                with(meta, listOf(a(Meta, with(Meta.name, "namn"),
                                        with(Meta.value, "varde"))))))))));

        messageLogService.messageReceived(
                messageLogService.saveMessage(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(originatorOrFrom, listOf(a(Originator, with(Originator.value,
                                        ShsLabelMaker.DEFAULT_TEST_ORIGINATOR)))),
                                with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                                with(transferType, TransferType.ASYNCH)))))));

        ShsMessageEntry entry = messageLogService.messageReceived(
                messageLogService.saveMessage(
                        make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                                with(to, a(To, with(To.value, ShsLabelMaker.DEFAULT_TEST_FROM))),
                                with(subject, "lastWeeksMessage"),
                                with(transferType, TransferType.ASYNCH)))))));
        
        //config for message "entry"

        GregorianCalendar lastWeek = new GregorianCalendar();
        lastWeek.add(GregorianCalendar.DAY_OF_MONTH, -7);
        
        entry.setStateTimeStamp(lastWeek.getTime());
        messageLogService.update(entry);
        
        entry = messageLogService.saveMessage(
                make(a(ShsMessage, with(ShsMessage.label, a(ShsLabel,
                        with(transferType, TransferType.ASYNCH))))));
        
//        entry.setArchived(true);
        messageLogService.update(entry);
        
    }
}
