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

package se.inera.axel.shs.xml.product;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "description",
    "mime",
    "security"
})
@XmlRootElement(name = "reply-data")
public class ReplyData implements Serializable {

    @XmlAttribute(name = "datapartType", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String datapartType;
    @XmlAttribute(name = "minOccurs")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String minOccurs;
    @XmlAttribute(name = "maxOccurs")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String maxOccurs;
    @XmlAttribute(name = "data-type")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String dataType;
    protected String description;
    @XmlElement(required = true)
    protected Mime mime;
    protected Security security;

    /**
     * Gets the value of the datapartType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDatapartType() {
        return datapartType;
    }

    /**
     * Sets the value of the datapartType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatapartType(String value) {
        this.datapartType = value;
    }

    /**
     * Gets the value of the minOccurs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinOccurs() {
        if (minOccurs == null) {
            return "1";
        } else {
            return minOccurs;
        }
    }

    /**
     * Sets the value of the minOccurs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinOccurs(String value) {
        this.minOccurs = value;
    }

    /**
     * Gets the value of the maxOccurs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxOccurs() {
        if (maxOccurs == null) {
            return "1";
        } else {
            return maxOccurs;
        }
    }

    /**
     * Sets the value of the maxOccurs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxOccurs(String value) {
        this.maxOccurs = value;
    }

    /**
     * Gets the value of the dataType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the value of the dataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataType(String value) {
        this.dataType = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the mime property.
     * 
     * @return
     *     possible object is
     *     {@link Mime }
     *     
     */
    public Mime getMime() {
        return mime;
    }

    /**
     * Sets the value of the mime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Mime }
     *     
     */
    public void setMime(Mime value) {
        this.mime = value;
    }

    /**
     * Gets the value of the security property.
     * 
     * @return
     *     possible object is
     *     {@link Security }
     *     
     */
    public Security getSecurity() {
        return security;
    }

    /**
     * Sets the value of the security property.
     * 
     * @param value
     *     allowed object is
     *     {@link Security }
     *     
     */
    public void setSecurity(Security value) {
        this.security = value;
    }

}
