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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class Open {

    protected When when;
    protected List<Object> starttimeOrStoptime;
    protected String description;

    /**
     * Gets the value of the when property.
     * 
     * @return
     *     possible object is
     *     {@link When }
     *     
     */
    public When getWhen() {
        return when;
    }

    /**
     * Sets the value of the when property.
     * 
     * @param value
     *     allowed object is
     *     {@link When }
     *     
     */
    public void setWhen(When value) {
        this.when = value;
    }

    /**
     * Gets the value of the starttimeOrStoptime property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the starttimeOrStoptime property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStarttimeOrStoptime().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Starttime }
     * {@link Stoptime }
     * 
     * 
     */
    public List<Object> getStarttimeOrStoptime() {
        if (starttimeOrStoptime == null) {
            starttimeOrStoptime = new ArrayList<Object>();
        }
        return this.starttimeOrStoptime;
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
