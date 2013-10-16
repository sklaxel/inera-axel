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

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.axel.shs.broker.agreement.AgreementService;
import se.inera.axel.shs.broker.directory.DirectoryService;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.messagestore.*;
import se.inera.axel.shs.broker.routing.ShsPluginRegistration;
import se.inera.axel.shs.broker.routing.ShsRouter;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.mime.ShsMessageMaker;
import se.inera.axel.shs.mime.ShsMessageTestObjectMother;
import se.inera.axel.shs.processor.ShsMessageMarshaller;
import se.inera.axel.shs.processor.TimestampConverter;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@Configuration
public class MockConfig {

    public MockConfig() {
        MockitoAnnotations.initMocks(this);
    }

    @Mock
    MessageStoreService messageStoreService;

    public @Bean MessageStoreService messageStoreService() throws Exception {
        when(messageStoreService.findOne(any(ShsMessageEntry.class)))
                       .thenAnswer(new Answer<ShsMessage>() {
                           @Override
                           public ShsMessage answer(InvocationOnMock invocation) throws Throwable {
                               ShsMessage message = make(a(ShsMessageMaker.ShsMessage));
                               ShsMessageEntry entry = (ShsMessageEntry) invocation.getArguments()[0];
                               message.setLabel(entry.getLabel());
                               return message;
                           }
                       });

        when(messageStoreService.save(any(ShsMessageEntry.class), any(InputStream.class)))
                       .thenAnswer(new Answer<ShsMessageEntry>() {
                           @Override
                           public ShsMessageEntry answer(InvocationOnMock invocation) throws Throwable {
                               ShsMessageEntry entry = (ShsMessageEntry) invocation.getArguments()[0];
                               InputStream stream = (InputStream) invocation.getArguments()[1];

                               ShsLabel label = new ShsMessageMarshaller().parseLabel(stream);

                               entry.setLabel(label);
                               return entry;
                           }
                       });

        return messageStoreService;
    }

}
