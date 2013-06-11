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

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.axel.shs.broker.agreement.AgreementService;
import se.inera.axel.shs.broker.directory.DirectoryService;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.broker.routing.ShsPluginRegistration;
import se.inera.axel.shs.broker.routing.ShsRouter;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.mime.ShsMessageMaker;
import se.inera.axel.shs.mime.ShsMessageTestObjectMother;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.util.ArrayList;
import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessageInstantiator.label;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
@Configuration
public class MockConfig {

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


    public MockConfig() {
        MockitoAnnotations.initMocks(this);
    }

    @Bean
    public MessageLogService messageLogService() {
        given(messageLogService.createEntry(any(ShsMessage.class)))
        .willAnswer(new Answer<ShsMessageEntry>() {
            @Override
            public ShsMessageEntry answer(InvocationOnMock invocation) throws Throwable {
                return new ShsMessageEntry(((ShsMessage) invocation.getArguments()[0]).getLabel());
            }
        });

        given(messageLogService.fetchMessage(any(ShsMessageEntry.class)))
        .willAnswer(new Answer<ShsMessage>() {
            @Override
            public ShsMessage answer(InvocationOnMock invocation) throws Throwable {
                return make(a(ShsMessageMaker.ShsMessage,
                                        with(label, ((ShsMessageEntry) invocation.getArguments()[0]).getLabel())));
            }
        });

        return messageLogService;
    }

    @Bean
    public DirectoryService directoryService () {
        given(directoryService.getOrganization(ShsMessageTestObjectMother.DEFAULT_TEST_TO)).willAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Organization org = null;

                org = new Organization();
                org.setOrgName("Good Org");
                org.setOrgNumber(ShsMessageTestObjectMother.DEFAULT_TEST_TO);
                org.setDescription("Don't be evil");

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
                    // direct addressing...
                    result.add(((ShsLabel) invocation.getArguments()[0]).getTo().getvalue());
                    return result;
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

}
