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
package se.inera.axel.shs.broker.agreement.mongo.model;

import se.inera.axel.shs.xml.agreement.NotAfter;
import se.inera.axel.shs.xml.agreement.NotBefore;


/**
 * 
 */
public class Request {

    protected NotBefore notBefore;
    protected NotAfter notAfter;

    /**
     * Gets the value of the notBefore property.
     * 
     * @return
     *     possible object is
     *     {@link NotBefore }
     *     
     */
    public NotBefore getNotBefore() {
        return notBefore;
    }

    /**
     * Sets the value of the notBefore property.
     * 
     * @param value
     *     allowed object is
     *     {@link NotBefore }
     *     
     */
    public void setNotBefore(NotBefore value) {
        this.notBefore = value;
    }

    /**
     * Gets the value of the notAfter property.
     * 
     * @return
     *     possible object is
     *     {@link NotAfter }
     *     
     */
    public NotAfter getNotAfter() {
        return notAfter;
    }

    /**
     * Sets the value of the notAfter property.
     * 
     * @param value
     *     allowed object is
     *     {@link NotAfter }
     *     
     */
    public void setNotAfter(NotAfter value) {
        this.notAfter = value;
    }

}
