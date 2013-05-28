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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "intervaltime",
    "starttime",
    "stoptime"
})
@XmlRootElement(name = "schedule")
public class Schedule implements Serializable {

    @XmlAttribute(name = "startdate")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String startdate;
    @XmlAttribute(name = "stopdate")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String stopdate;
    @XmlAttribute(name = "timezone")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String timezone;
    @XmlElement(required = true)
    protected Intervaltime intervaltime;
    protected Starttime starttime;
    protected Stoptime stoptime;

    /**
     * Gets the value of the startdate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStartdate() {
        return startdate;
    }

    /**
     * Sets the value of the startdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartdate(String value) {
        this.startdate = value;
    }

    /**
     * Gets the value of the stopdate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStopdate() {
        return stopdate;
    }

    /**
     * Sets the value of the stopdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStopdate(String value) {
        this.stopdate = value;
    }

    /**
     * Gets the value of the timezone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Sets the value of the timezone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimezone(String value) {
        this.timezone = value;
    }

    /**
     * Gets the value of the intervaltime property.
     * 
     * @return
     *     possible object is
     *     {@link Intervaltime }
     *     
     */
    public Intervaltime getIntervaltime() {
        return intervaltime;
    }

    /**
     * Sets the value of the intervaltime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Intervaltime }
     *     
     */
    public void setIntervaltime(Intervaltime value) {
        this.intervaltime = value;
    }

    /**
     * Gets the value of the starttime property.
     * 
     * @return
     *     possible object is
     *     {@link Starttime }
     *     
     */
    public Starttime getStarttime() {
        return starttime;
    }

    /**
     * Sets the value of the starttime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Starttime }
     *     
     */
    public void setStarttime(Starttime value) {
        this.starttime = value;
    }

    /**
     * Gets the value of the stoptime property.
     * 
     * @return
     *     possible object is
     *     {@link Stoptime }
     *     
     */
    public Stoptime getStoptime() {
        return stoptime;
    }

    /**
     * Sets the value of the stoptime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Stoptime }
     *     
     */
    public void setStoptime(Stoptime value) {
        this.stoptime = value;
    }

}
