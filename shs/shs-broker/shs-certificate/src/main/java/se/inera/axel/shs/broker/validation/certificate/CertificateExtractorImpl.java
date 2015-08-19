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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Implements interface CertificateExtractor.
 *
 */
@Component
public class CertificateExtractorImpl 
        implements CertificateExtractor {

    private static Logger log = LoggerFactory
            .getLogger(CertificateExtractorImpl.class);

    private Pattern pattern;

    @Value("${shs.senderCertificateValidator.senderPatternInCertificate:}")
    private String patternString;

    @PostConstruct
    public void init() throws Exception {
        if (StringUtils.isNotEmpty(patternString)) {
            this.pattern = Pattern.compile(patternString);
        }
    }

    @Override
    public String extractSender(Object certificate) throws CertificateException {

        if (certificate instanceof X509Certificate) {
            return extractSenderFromX509Certificate(certificate);
        } else if (certificate instanceof String && PemConverter.containsCorrectPemHeaders((String) certificate)) {
            return extractSenderFromPemCertificate(certificate);
        } else {
            log.error("Unknown certificate type found");
            throw new CertificateException("Unknown certificate type found");
        }
    }

    private String extractSenderFromPemCertificate(Object certificate)
            throws CertificateException  {
        String pemCertificateString = (String) certificate;
        X509Certificate x509Certificate = PemConverter
                .convertPemToX509Certificate(pemCertificateString);
        return extractSenderFromCertificate(x509Certificate);
    }

    private String extractSenderFromCertificate(
            X509Certificate x509Certificate) throws CertificateException {
        log.debug("Extracting sender id from certificate.");

        if (x509Certificate == null) {
            log.error("Cannot extract any sender because the certificate was null");
            throw new IllegalArgumentException("Cannot extract any sender because the certificate was null");
        }

        final String principalName = x509Certificate.getSubjectX500Principal().getName();
        return extractSenderFromPrincipal(principalName);
    }

    private String extractSenderFromPrincipal(String principalName) throws CertificateException {
        log.debug("principalName: {}", principalName);
        
        if (pattern == null) {
            log.error("Property shs.senderCertificateValidator.senderPatternInCertificate must not be empty because sender validation is enabled (shs.senderCertificateValidator.enabled=true).");
            return null;
        }
        
        final Matcher matcher = pattern.matcher(principalName);

        if (matcher.find()) {
            String sender = matcher.group(1);
            log.debug("Found sender id {}", sender);

            if (sender.startsWith("#")) {
                try {
                    sender = convertFromHexToString(sender.substring(5));
                } catch (Exception e) {
                    String msg = "convertFromHexToString failed for sender[" + sender + "]";
                    log.error(msg, e);
                    throw new CertificateException(msg);
                }
            }
            return sender;
        } else {
            log.error("No sender found in certificate. \npattern ... " + pattern.toString() + "\nprincipalName ... " + principalName);
            throw new CertificateException("No sender found in certificate: " + principalName);
        }
    }

    private String convertFromHexToString(String hexString) {
        byte[] txtInByte = new byte[hexString.length() / 2];
        int j = 0;
        for (int i = 0; i < hexString.length(); i += 2) {
            txtInByte[j++] = Byte.parseByte(hexString.substring(i, i + 2), 16);
        }
        return new String(txtInByte);
    }

    private String extractSenderFromX509Certificate(Object certificate) throws CertificateException {
        X509Certificate x509Certificate = (X509Certificate) certificate;
        return extractSenderFromCertificate(x509Certificate);
    }


    public void setPatternString(String patternString) {
        this.patternString = patternString;
    }
}
