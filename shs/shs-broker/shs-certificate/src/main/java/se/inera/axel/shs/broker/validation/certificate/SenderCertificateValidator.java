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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import se.inera.axel.shs.broker.validation.SenderValidationService;
import se.inera.axel.shs.exception.IllegalSenderException;

@Service("senderValidationService")
public class SenderCertificateValidator implements SenderValidationService {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SenderCertificateValidator.class);

    @Value("#{new Boolean('${senderValidationEnabled:false}')}")
    private boolean enabled;

    @Value("${whiteList:null}")
    private String whiteList;

    @Value("${orgId}")
    private String orgId;

    @Autowired
    CertificateExtractor certificateExtractor;
    
    @Override
    public void validateSender(String callerIp, Object certificate, String sender)
            throws IllegalSenderException {
        
        if (!isEnabled()) {
            LOGGER.debug("sender validation is disabled");
            return;
        }
        
        if (StringUtils.isEmpty(sender)) {
            throw new IllegalSenderException("sender is empty");
        }

        if (sender.equals(orgId)) {
            LOGGER.debug("sender {} matches orgId of this SHS server", sender);
            // sender matches the orgId of this SHS server.
            // This makes local connections from riv-shs-bridge and shs-cmdline-client pass validation. 
            return;
        }

        if (certificate == null) {
            LOGGER.debug("certificate information is empty");
            throw new IllegalSenderException("certificate is empty");
        }

        if (!isOnWhiteList(callerIp)) {
            LOGGER.debug("callerIp {} is not on white list of accepted IP addresses", callerIp);
            throw new IllegalSenderException("Caller IP address[" + callerIp
                    + "] is not on white list");
        }

        String senderInCertificate;
        try {
            senderInCertificate = extractSender(certificate);
            if (!sender.equals(senderInCertificate)) {
                LOGGER.debug("sender {} does not match sender in certificate {}", sender, senderInCertificate);
                throw new IllegalSenderException(
                        "sender does not match the sender in the certificate");
            }
        } catch (CertificateException e) {
            throw new IllegalSenderException(e);
        }
    }

    private String extractSender(Object certificate) throws CertificateException {
        return certificateExtractor.extractSender(certificate);
    }

    private boolean isOnWhiteList(String callerIp) {
        LOGGER.debug("Check if caller {} is on white list", callerIp);
        
        if (StringUtils.isEmpty(callerIp)) {
            LOGGER.warn("IP address of the caller is empty.");
            return false;
        }

        if (StringUtils.isEmpty(whiteList)) {
            LOGGER.warn("A check against the ip address whitelist was requested, but the whitelist is configured empty.");
            return false;
        }

        for (String ipAddress : whiteList.split(",")) {
            if(callerIp.startsWith(ipAddress.trim())){
                LOGGER.debug("callerIp {} matches ip address/subdomain in white list [{}]", callerIp, whiteList);
                return true;
            }
        }

        LOGGER.warn("Caller was not on the white list of accepted IP-addresses. IP-address: {}, accepted IP-addresses in IP_WHITE_LIST: {}", callerIp, whiteList);
        return false;
    }

    public String getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(String whiteList) {
        this.whiteList = whiteList;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
