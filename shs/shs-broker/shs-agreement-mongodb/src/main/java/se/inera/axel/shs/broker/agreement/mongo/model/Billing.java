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

import java.util.ArrayList;
import java.util.List;

import se.inera.axel.shs.xml.agreement.PerExchange;
import se.inera.axel.shs.xml.agreement.PerPeriod;
import se.inera.axel.shs.xml.agreement.PerVolume;


/**
 * 
 */
public class Billing {

    protected String required;
    protected String currency;
    protected List<Object> perExchangeOrPerVolumeOrPerPeriod;
    protected String description;

    /**
     * Gets the value of the required property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequired() {
        if (required == null) {
            return "yes";
        } else {
            return required;
        }
    }

    /**
     * Sets the value of the required property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequired(String value) {
        this.required = value;
    }

    /**
     * Gets the value of the currency property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrency() {
        if (currency == null) {
            return "SEK";
        } else {
            return currency;
        }
    }

    /**
     * Sets the value of the currency property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrency(String value) {
        this.currency = value;
    }

    /**
     * Gets the value of the perExchangeOrPerVolumeOrPerPeriod property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the perExchangeOrPerVolumeOrPerPeriod property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPerExchangeOrPerVolumeOrPerPeriod().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PerExchange }
     * {@link PerVolume }
     * {@link PerPeriod }
     * 
     * 
     */
    public List<Object> getPerExchangeOrPerVolumeOrPerPeriod() {
        if (perExchangeOrPerVolumeOrPerPeriod == null) {
            perExchangeOrPerVolumeOrPerPeriod = new ArrayList<Object>();
        }
        return this.perExchangeOrPerVolumeOrPerPeriod;
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
