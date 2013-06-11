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
package se.inera.axel.shs.xml.management;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "appinfo"
})
@XmlRootElement(name = "error")
public class Error implements Serializable {

    @XmlAttribute(name = "errorcode", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String errorcode;
    @XmlAttribute(name = "errorinfo")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String errorinfo;
    protected List<Appinfo> appinfo;

    /**
     * Gets the value of the errorcode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorcode() {
        return errorcode;
    }

    /**
     * Sets the value of the errorcode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorcode(String value) {
        this.errorcode = value;
    }

    /**
     * Gets the value of the errorinfo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorinfo() {
        return errorinfo;
    }

    /**
     * Sets the value of the errorinfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorinfo(String value) {
        this.errorinfo = value;
    }

    /**
     * Gets the value of the appinfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the appinfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAppinfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Appinfo }
     * 
     * 
     */
    public List<Appinfo> getAppinfo() {
        if (appinfo == null) {
            appinfo = new ArrayList<Appinfo>();
        }
        return this.appinfo;
    }

}
