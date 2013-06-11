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
package se.inera.axel.shs.xml.product;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "digest",
    "encryption",
    "dsig"
})
@XmlRootElement(name = "security")
public class Security implements Serializable {

    protected Digest digest;
    protected Encryption encryption;
    protected Dsig dsig;

    /**
     * Gets the value of the digest property.
     * 
     * @return
     *     possible object is
     *     {@link Digest }
     *     
     */
    public Digest getDigest() {
        return digest;
    }

    /**
     * Sets the value of the digest property.
     * 
     * @param value
     *     allowed object is
     *     {@link Digest }
     *     
     */
    public void setDigest(Digest value) {
        this.digest = value;
    }

    /**
     * Gets the value of the encryption property.
     * 
     * @return
     *     possible object is
     *     {@link Encryption }
     *     
     */
    public Encryption getEncryption() {
        return encryption;
    }

    /**
     * Sets the value of the encryption property.
     * 
     * @param value
     *     allowed object is
     *     {@link Encryption }
     *     
     */
    public void setEncryption(Encryption value) {
        this.encryption = value;
    }

    /**
     * Gets the value of the dsig property.
     * 
     * @return
     *     possible object is
     *     {@link Dsig }
     *     
     */
    public Dsig getDsig() {
        return dsig;
    }

    /**
     * Sets the value of the dsig property.
     * 
     * @param value
     *     allowed object is
     *     {@link Dsig }
     *     
     */
    public void setDsig(Dsig value) {
        this.dsig = value;
    }

}
