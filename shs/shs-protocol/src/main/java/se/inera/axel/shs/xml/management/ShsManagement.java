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

package se.inera.axel.shs.xml.management;

import se.inera.axel.shs.xml.DateAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "datetime",
    "confirmationOrError"
})
@XmlRootElement(name = "shs.management")
public class ShsManagement implements Serializable {

    @XmlAttribute(name = "version")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String version;
    @XmlAttribute(name = "corr.id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String corrId;
    @XmlAttribute(name = "content.id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String contentId;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(DateAdapter.class)
    protected Date datetime;
    @XmlElements({
        @XmlElement(name = "confirmation", required = true, type = Confirmation.class),
        @XmlElement(name = "error", required = true, type = Error.class)
    })
    protected List<Object> confirmationOrError;

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        if (version == null) {
            return "1.2";
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the corrId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCorrId() {
        return corrId;
    }

    /**
     * Sets the value of the corrId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCorrId(String value) {
        this.corrId = value;
    }

    /**
     * Gets the value of the contentId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentId() {
        return contentId;
    }

    /**
     * Sets the value of the contentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentId(String value) {
        this.contentId = value;
    }

    /**
     * Gets the value of the datetime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getDatetime() {
        return datetime;
    }

    /**
     * Sets the value of the datetime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatetime(Date value) {
        this.datetime = value;
    }

    /**
     * Gets the value of the confirmationOrError property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the confirmationOrError property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConfirmationOrError().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Confirmation }
     * {@link Error }
     * 
     * 
     */
    public List<Object> getConfirmationOrError() {
        if (confirmationOrError == null) {
            confirmationOrError = new ArrayList<Object>();
        }
        return this.confirmationOrError;
    }
    
    public Confirmation getConfirmation() {
    	Confirmation confirmation = null;
    	
    	Object value = getConfirmationOrErrorValue();
    	
    	if (value instanceof Confirmation) {
    		confirmation = (Confirmation)value;
    	}
    	
    	return confirmation;
    }
    
    public Error getError() {
    	Error error = null;
    	
    	Object value = getConfirmationOrErrorValue();
    	
    	if (value instanceof Error) {
    		error = (Error)value;
    	}
    	
    	return error;
    }
    
    private Object getConfirmationOrErrorValue() {
    	if (getConfirmationOrError().size() > 0) {
    		return getConfirmationOrError().get(0);
    	} else {
    		return null;
    	}
    }

}
