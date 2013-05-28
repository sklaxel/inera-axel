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

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.axel.shs.directory.DirectoryService;
import se.inera.axel.shs.directory.Organization;
import se.inera.axel.shs.messagestore.MessageLogService;
import se.inera.axel.shs.protocol.ShsMessageTestObjectMother;
import se.inera.axel.shs.routing.ShsRouter;

import static org.mockito.BDDMockito.given;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
@Configuration
public class MockConfig {
    @Mock
    MessageLogService messageLogService;

    @Mock
    DirectoryService directoryService;

    @Mock
    ShsRouter shsRouter;

    public MockConfig() {
        MockitoAnnotations.initMocks(this);
    }

    @Bean
    public MessageLogService messageLogService() {
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
        return shsRouter;
    }
}
