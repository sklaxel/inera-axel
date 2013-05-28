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

package se.inera.axel.shs.xml.agreement;

import javax.xml.bind.annotation.*;
import java.io.Serializable;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "description",
    "valid",
    "schedule",
    "qoS"
})
@XmlRootElement(name = "general")
public class General implements Serializable {

    protected String description;
    @XmlElement(required = true)
    protected Valid valid;
    @XmlElement(required = true)
    protected Schedule schedule;
    @XmlElement(name = "QoS", required = true)
    protected QoS qoS;

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
     * Gets the value of the valid property.
     * 
     * @return
     *     possible object is
     *     {@link Valid }
     *     
     */
    public Valid getValid() {
        return valid;
    }

    /**
     * Sets the value of the valid property.
     * 
     * @param value
     *     allowed object is
     *     {@link Valid }
     *     
     */
    public void setValid(Valid value) {
        this.valid = value;
    }

    /**
     * Gets the value of the schedule property.
     * 
     * @return
     *     possible object is
     *     {@link Schedule }
     *     
     */
    public Schedule getSchedule() {
        return schedule;
    }

    /**
     * Sets the value of the schedule property.
     * 
     * @param value
     *     allowed object is
     *     {@link Schedule }
     *     
     */
    public void setSchedule(Schedule value) {
        this.schedule = value;
    }

    /**
     * Gets the value of the qoS property.
     * 
     * @return
     *     possible object is
     *     {@link QoS }
     *     
     */
    public QoS getQoS() {
        return qoS;
    }

    /**
     * Sets the value of the qoS property.
     * 
     * @param value
     *     allowed object is
     *     {@link QoS }
     *     
     */
    public void setQoS(QoS value) {
        this.qoS = value;
    }

}
