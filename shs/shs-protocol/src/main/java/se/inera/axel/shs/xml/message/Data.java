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

package se.inera.axel.shs.xml.message;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "data")
public class Data implements Serializable {

    @XmlAttribute(name = "datapartType", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String datapartType;
    @XmlAttribute(name = "filename")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String filename;
    @XmlAttribute(name = "no-of-bytes")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String noOfBytes;
    @XmlAttribute(name = "no-of-records")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String noOfRecords;

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
     * Gets the value of the filename property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the value of the filename property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilename(String value) {
        this.filename = value;
    }

    /**
     * Gets the value of the noOfBytes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNoOfBytes() {
        return noOfBytes;
    }

    /**
     * Sets the value of the noOfBytes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNoOfBytes(String value) {
        this.noOfBytes = value;
    }

    /**
     * Gets the value of the noOfRecords property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNoOfRecords() {
        return noOfRecords;
    }

    /**
     * Sets the value of the noOfRecords property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNoOfRecords(String value) {
        this.noOfRecords = value;
    }

}
