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

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "mime")
public class Mime implements Serializable {

    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "subtype")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String subtype;
    @XmlAttribute(name = "text-charset")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String textCharset;
    @XmlAttribute(name = "transfer-encoding")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String transferEncoding;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        if (type == null) {
            return "text";
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the subtype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubtype() {
        if (subtype == null) {
            return "xml";
        } else {
            return subtype;
        }
    }

    /**
     * Sets the value of the subtype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubtype(String value) {
        this.subtype = value;
    }

    /**
     * Gets the value of the textCharset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextCharset() {
        if (textCharset == null) {
            return "iso-8859-1";
        } else {
            return textCharset;
        }
    }

    /**
     * Sets the value of the textCharset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextCharset(String value) {
        this.textCharset = value;
    }

    /**
     * Gets the value of the transferEncoding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransferEncoding() {
        if (transferEncoding == null) {
            return "binary";
        } else {
            return transferEncoding;
        }
    }

    /**
     * Sets the value of the transferEncoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransferEncoding(String value) {
        this.transferEncoding = value;
    }

}
