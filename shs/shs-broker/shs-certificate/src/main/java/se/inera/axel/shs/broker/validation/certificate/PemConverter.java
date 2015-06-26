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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.apache.commons.lang.StringUtils;

/**
 * Provides conversion routines for PEM formatted certificates.
 * 
 */
public final class PemConverter {

	private static final String BEGIN_HEADER = "-----BEGIN CERTIFICATE-----";
	private static final String END_HEADER = "-----END CERTIFICATE-----";

	/**
	 * Converts a PEM formatted certificate to a X509Certificate.
	 * 
	 * @param pemCertificate the PEM certificat that should get converted.
	 * 
	 * @return the X509Certificate.
	 * 
	 * @throws CertificateException
	 */
	public static X509Certificate convertPemToX509Certificate(String pemCertificate) throws CertificateException {
		if (containsCorrectPemHeaders(pemCertificate)) {
			String certificateInfo = extractCertificate(pemCertificate);
            X509Certificate certificate = generateX509Certificate(certificateInfo);
            return (X509Certificate) certificate;
		} else {
			throw new CertificateException("PEM headers missing in certificate.");
		}
	}

	private static X509Certificate generateX509Certificate(String pemCertificate) throws CertificateException {
        InputStream is = new ByteArrayInputStream((pemCertificate).getBytes());
        BufferedInputStream bis = new BufferedInputStream(is);

        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) factory.generateCertificate(bis);
		return certificate;
	}

	private static String extractCertificate(String pemCertificate) {
		StringBuffer cert = new StringBuffer();
		cert.append(BEGIN_HEADER);
		cert.append("\n");

	    int beginHeader = pemCertificate.indexOf(BEGIN_HEADER) + BEGIN_HEADER.length();
	    int endHeader = pemCertificate.indexOf(END_HEADER);
		cert.append(pemCertificate.substring(beginHeader, endHeader).replaceAll("\\s+", ""));

		cert.append("\n");
		cert.append(END_HEADER);

		return cert.toString();
	}

	/**
	 * Checks whether the certificate string contains PEM headers.
	 * 
	 * @param pemCertString the certificate string which is to be checked.
	 * 
     * @return true if it contains PEM headers, false otherwise.
	 */
	public static boolean containsCorrectPemHeaders(String pemCertString) {
	    if (StringUtils.isEmpty(pemCertString)) {
	        return false;
	    }
		int beginHeader = pemCertString.indexOf(BEGIN_HEADER);
		int endHeader = pemCertString.indexOf(END_HEADER);
		return beginHeader != -1 && endHeader != -1;
	}
}
