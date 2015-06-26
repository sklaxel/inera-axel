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

/**
 * Interface for extracting data from a certificate.
 * 
 */
public interface CertificateExtractor {

	/**
	 * Extracts the sender id from a certificate.
	 * 
	 * @param certificate the certificate from which the sender id should be extracted.
	 * 
	 * @return the extracted sender id.
	 * 
	 * @throws CertificateException 
	 */
	public String extractSender(Object certificate) throws CertificateException;
}
