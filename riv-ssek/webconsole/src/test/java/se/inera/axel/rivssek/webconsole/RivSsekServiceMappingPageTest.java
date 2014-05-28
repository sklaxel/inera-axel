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
package se.inera.axel.rivssek.webconsole;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.testng.annotations.Test;
import se.inera.axel.riv2ssek.RivSsekServiceMapping;
import se.inera.axel.riv2ssek.RivSsekServiceMappingRepository;
import se.inera.axel.ssek.common.schema.ssek.IdType;

import java.util.Arrays;

import static org.mockito.Mockito.*;

public class RivSsekServiceMappingPageTest extends RivSsekWebconsolePageTest {
    private final static Logger log = LoggerFactory.getLogger(RivSsekServiceMappingPageTest.class);

    @Override
    protected void beforeMethodSetup() {
        super.beforeMethodSetup();

        RivSsekServiceMappingRepository rivSsekServiceMappingRepository = mock(RivSsekServiceMappingRepository.class);
        when(rivSsekServiceMappingRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(new RivSsekServiceMapping.Builder()
                        .address("http://test")
                        .ssekReceiverType(IdType.CN)
                        .ssekReceiver("ssekReceiver")
                        .rivServiceNamespace("exampleRivNamespace")
                        .rivLogicalAddress("12345678-0000")
                        .build())));

        injector.registerBean("rivSsekServiceMappingRepository", rivSsekServiceMappingRepository);
    }

    @Test
    public void renderRivSsekServiceMappingPage() {
        tester.startPage(RivSsekServiceMappingPage.class);
        
        tester.assertRenderedPage(RivSsekServiceMappingPage.class);
        tester.assertVisible("add");
        tester.assertNoErrorMessage();
    }

    @Test
    public void createNew() {
        tester.startPage(RivSsekServiceMappingPage.class);

        tester.clickLink("add");

        tester.assertRenderedPage(RivSsekServiceMappingEditPage.class);
        tester.assertNoErrorMessage();
    }

}
