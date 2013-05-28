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

package se.inera.axel.shs.agreement.mongo;

import se.inera.axel.shs.xml.agreement.Average;
import se.inera.axel.shs.xml.agreement.Peak;


/**
 * 
 */
public class Frequency {

    protected Average average;
    protected Peak peak;
    protected String description;

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
