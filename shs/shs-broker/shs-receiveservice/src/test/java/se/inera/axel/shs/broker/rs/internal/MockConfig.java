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
import se.inera.axel.shs.broker.validation.SenderValidationService;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.mime.ShsMessageMaker;
import se.inera.axel.shs.mime.ShsMessageTestObjectMother;
import se.inera.axel.shs.processor.TimestampConverter;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
@Configuration
public class MockConfig {
	public static final String DUPLICATE_TX_ID = "a3c1e4f2-2c6b-11e3-8936-f71d91ea5468";
    public static final String DUPLICATE_TIMESTAMP = "2013-10-03T15:39:06";

    @Mock
    AgreementService agreementService;

    @Mock
    MessageLogService messageLogService;

    @Mock
    DirectoryService directoryService;

    @Mock
    ShsRouter shsRouter;

    @Mock
    ShsPluginRegistration shsPluginRegistration;

    @Mock
    SenderValidationService senderValidationService;


    public MockConfig() {
        final String METHOD_NAME = "MockConfig";
        MockitoAnnotations.initMocks(this);
    }

    @Bean
    public MessageLogService messageLogService() {
        given(messageLogService.saveMessage(any(ShsMessage.class)))
        .willAnswer(new Answer<ShsMessageEntry>() {
            @Override
            public ShsMessageEntry answer(InvocationOnMock invocation) throws Throwable {

                ShsLabel label = ((ShsMessage) invocation.getArguments()[0]).getLabel();
                if (label.getTxId().equals(MockConfig.DUPLICATE_TX_ID)) {
                    throw new MessageAlreadyExistsException(label,
                            TimestampConverter.stringToDate(DUPLICATE_TIMESTAMP));
                }

                ShsMessageEntry shsMessageEntry =
                        make(a(ShsMessageEntryMaker.ShsMessageEntry,
                                with(ShsMessageEntryMaker.ShsMessageEntryInstantiator.label, label)));
                return shsMessageEntry;
            }
        });

        given(messageLogService.loadMessage(any(ShsMessageEntry.class)))
                .willAnswer(new Answer<ShsMessage>() {
                    @Override
                    public ShsMessage answer(InvocationOnMock invocation) throws Throwable {
                        return make(a(ShsMessageMaker.ShsMessage,
                                with(ShsMessageMaker.ShsMessage.label, ((ShsMessageEntry) invocation.getArguments()[0]).getLabel())));
                    }
                });

        given(messageLogService.messageQuarantined(any(ShsMessageEntry.class), any(Exception.class)))
                .willAnswer(new Answer<ShsMessageEntry>() {
                    @Override
                    public ShsMessageEntry answer(InvocationOnMock invocation) throws Throwable {
                        ShsMessageEntry entry = (ShsMessageEntry) invocation.getArguments()[0];
                        entry.setState(MessageState.QUARANTINED);
                        entry.setStateTimeStamp(new Date());

                        return entry;
                    }
                });

        given(messageLogService.messageReceived(any(ShsMessageEntry.class)))
                .willAnswer(new Answer<ShsMessageEntry>() {
                    @Override
                    public ShsMessageEntry answer(InvocationOnMock invocation) throws Throwable {
                        ShsMessageEntry entry = (ShsMessageEntry) invocation.getArguments()[0];
                        entry.setState(MessageState.RECEIVED);
                        entry.setStateTimeStamp(new Date());

                        return entry;
                    }
                });

        given(messageLogService.messageSent(any(ShsMessageEntry.class)))
                .willAnswer(new Answer<ShsMessageEntry>() {
                    @Override
                    public ShsMessageEntry answer(InvocationOnMock invocation) throws Throwable {
                        ShsMessageEntry entry = (ShsMessageEntry) invocation.getArguments()[0];
                        entry.setState(MessageState.SENT);
                        entry.setStateTimeStamp(new Date());

                        return entry;
                    }
                });

        given(messageLogService.quarantineCorrelatedMessages(any(ShsMessage.class)))
                .willAnswer(new Answer<ShsMessage>() {
                    @Override
                    public ShsMessage answer(InvocationOnMock invocation) throws Throwable {
                        return (ShsMessage) invocation.getArguments()[0];
                    }
                });

        return messageLogService;
    }

    @Bean
    public DirectoryService directoryService() {
        given(directoryService.getOrganization(ShsMessageTestObjectMother.DEFAULT_TEST_TO)).willAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Organization org = new Organization();
                org.setOrgName("Good Org");
                org.setOrgNumber(ShsMessageTestObjectMother.DEFAULT_TEST_TO);
                org.setDescription("Don't be evil");

                return org;
            }
        });

        given(directoryService.getOrganization("1111111111")).willAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Organization org = new Organization();
                org.setOrgName("The One Org");
                org.setOrgNumber("1111111111");
                org.setDescription("Only ones in this corp.");

                return org;
            }
        });

        given(directoryService.getOrganization("0000000000")).willAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Organization org = new Organization();
                org.setOrgName("The Zero Org");
                org.setOrgNumber("0000000000");
                org.setDescription("Only zeros in this corp.");

                return org;
            }
        });

        return directoryService;
    }

    @Bean
    public ShsRouter shsRouter() {
        when(shsRouter.resolveRecipients(any(ShsLabel.class)))
                .thenAnswer(new Answer<List<String>>() {
                    @Override
                    public List<String> answer(InvocationOnMock invocation) throws Throwable {
                        List<String> result = new ArrayList<String>();
                        ShsLabel shsLabel = (ShsLabel) invocation.getArguments()[0];
                        if (shsLabel.getTo() == null) {
                            // Simulate product addressing with one-to-many scenario
                            result.add("02020202");
                            result.add("03030303");
                        } else {
                            // direct addressing...
                            result.add(shsLabel.getTo().getValue());
                        }

                        return result;
                    }
                });

        // asynch always resolves remote http endpoints.
        when(shsRouter.resolveEndpoint(any(ShsLabel.class)))
                .thenAnswer(new Answer<String>() {
                    @Override
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        String url = "https://localhost:" + System.getProperty("shsRsHttpEndpoint.port", "7070") + "/rs";
                        return url;
                    }
                });

        return shsRouter;
    }

    @Bean
    public ShsPluginRegistration shsPluginRegistration() {
        given(shsPluginRegistration.getEndpointUri(Matchers.<ShsLabel>any())).willReturn("direct:noopPlugin");

        return shsPluginRegistration;
    }

    @Bean
    public AgreementService agreementService() {
        return agreementService;
    }

    @Bean
    public SenderValidationService senderValidationService() {
        return senderValidationService;
    }
}
