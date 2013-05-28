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
import java.util.ArrayList;
import java.util.List;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "principal",
    "data",
    "replyData",
    "description"
})
@XmlRootElement(name = "shs.product")
public class ShsProduct implements Serializable {

    @XmlAttribute(name = "version")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String version;
    @XmlAttribute(name = "uuid", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String uuid;
    @XmlAttribute(name = "commonName")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String commonName;
    @XmlAttribute(name = "resp-required")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String respRequired;
    @XmlAttribute(name = "labeledURI")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String labeledURI;
    @XmlElement(required = true)
    protected Principal principal;
    @XmlElement(required = true)
    protected List<Data> data;
    @XmlElement(name = "reply-data")
    protected List<ReplyData> replyData;
    protected String description;

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
     * Gets the value of the uuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the value of the uuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUuid(String value) {
        this.uuid = value;
    }

    /**
     * Gets the value of the commonName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCommonName() {
        return commonName;
    }

    /**
     * Sets the value of the commonName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCommonName(String value) {
        this.commonName = value;
    }

    /**
     * Gets the value of the respRequired property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRespRequired() {
        if (respRequired == null) {
            return "yes";
        } else {
            return respRequired;
        }
    }

    /**
     * Sets the value of the respRequired property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRespRequired(String value) {
        this.respRequired = value;
    }

    /**
     * Gets the value of the labeledURI property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabeledURI() {
        return labeledURI;
    }

    /**
     * Sets the value of the labeledURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabeledURI(String value) {
        this.labeledURI = value;
    }

    /**
     * Gets the value of the principal property.
     * 
     * @return
     *     possible object is
     *     {@link Principal }
     *     
     */
    public Principal getPrincipal() {
        return principal;
    }

    /**
     * Sets the value of the principal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Principal }
     *     
     */
    public void setPrincipal(Principal value) {
        this.principal = value;
    }

    /**
     * Gets the value of the data property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the data property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Data }
     * 
     * 
     */
    public List<Data> getData() {
        if (data == null) {
            data = new ArrayList<Data>();
        }
        return this.data;
    }

    /**
     * Gets the value of the replyData property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the replyData property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReplyData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReplyData }
     * 
     * 
     */
    public List<ReplyData> getReplyData() {
        if (replyData == null) {
            replyData = new ArrayList<ReplyData>();
        }
        return this.replyData;
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
