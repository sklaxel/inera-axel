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
package se.inera.axel.shs.broker.validation.certificate;

import static org.mockito.Mockito.when;

import java.security.cert.CertificateException;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CertificateExtractorMock {

    public static final String CALLER_IP_EXISTING_IN_WHITELIST = "192.168.0.1";
    public static final String CALLER_IP_MISSING_IN_WHITELIST = "192.168.0.3";
    
    public static final String SENDER_VALID = "1111111111";
    public static final String SENDER_INVALID = "2222222222";
    public static final String SENDER_THAT_MATCHES_LOCAL_ORGID = "3333333333";

    @Mock
    CertificateExtractor certificateExtractor;

    public CertificateExtractorMock() {
        MockitoAnnotations.initMocks(this);
    }

    @Bean
    public CertificateExtractor certificateExtractor() throws CertificateException {

        when(certificateExtractor.extractSender(org.mockito.Mockito.anyString())).thenReturn(SENDER_VALID);
        
        return certificateExtractor;
    }
}
