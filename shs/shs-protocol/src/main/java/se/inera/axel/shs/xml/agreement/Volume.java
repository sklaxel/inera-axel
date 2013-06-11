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
package se.inera.axel.shs.xml.agreement;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "average",
    "peak",
    "description"
})
@XmlRootElement(name = "volume")
public class Volume implements Serializable {

    @XmlAttribute(name = "per-transfer", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String perTransfer;
    @XmlAttribute(name = "unit")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String unit;
    @XmlElement(required = true)
    protected Average average;
    protected Peak peak;
    protected String description;

    /**
     * Gets the value of the perTransfer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPerTransfer() {
        return perTransfer;
    }

    /**
     * Sets the value of the perTransfer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPerTransfer(String value) {
        this.perTransfer = value;
    }

    /**
     * Gets the value of the unit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnit() {
        if (unit == null) {
            return "KB";
        } else {
            return unit;
        }
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnit(String value) {
        this.unit = value;
    }

    /**
     * Gets the value of the average property.
     * 
     * @return
     *     possible object is
     *     {@link Average }
     *     
     */
    public Average getAverage() {
        return average;
    }

    /**
     * Sets the value of the average property.
     * 
     * @param value
     *     allowed object is
     *     {@link Average }
     *     
     */
    public void setAverage(Average value) {
        this.average = value;
    }

    /**
     * Gets the value of the peak property.
     * 
     * @return
     *     possible object is
     *     {@link Peak }
     *     
     */
    public Peak getPeak() {
        return peak;
    }

    /**
     * Sets the value of the peak property.
     * 
     * @param value
     *     allowed object is
     *     {@link Peak }
     *     
     */
    public void setPeak(Peak value) {
        this.peak = value;
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

}
