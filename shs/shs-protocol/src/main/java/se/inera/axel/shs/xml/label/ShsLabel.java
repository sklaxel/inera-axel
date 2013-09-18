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
package se.inera.axel.shs.xml.label;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.SerializationUtils;

import se.inera.axel.shs.xml.TimestampAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "originatorOrFrom",
    "to",
    "endRecipient",
    "product",
    "meta",
    "subject",
    "datetime",
    "content",
    "history"
})
@XmlRootElement(name = "shs.label")
public class ShsLabel implements Serializable {

    @XmlAttribute(name = "version")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String version;
    @XmlAttribute(name = "tx.id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String txId;
    @XmlAttribute(name = "corr.id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String corrId;
    @XmlAttribute(name = "shs.agreement")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String shsAgreement;
    @XmlAttribute(name = "transfer-type")
    protected TransferType transferType;
    @XmlAttribute(name = "message-type")
    protected MessageType messageType;
    @XmlAttribute(name = "document-type")
    protected MessageType documentType;
    @XmlAttribute(name = "sequence-type", required = true)
    protected SequenceType sequenceType;
    @XmlAttribute(name = "status")
    protected Status status;
    @XmlElements({
        @XmlElement(name = "originator", required = true, type = Originator.class),
        @XmlElement(name = "from", required = true, type = From.class)
    })
    protected List<Object> originatorOrFrom;
    protected To to;
    @XmlElement(name = "end-recipient")
    protected EndRecipient endRecipient;
    @XmlElement(required = true)
    protected Product product;
    protected List<Meta> meta;
    protected String subject;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(TimestampAdapter.class)
    protected Date datetime;
    @XmlElement(required = true)
    protected Content content;
    protected List<History> history;

    /**
     * Creates deep copy by means of serialization.
     * 
     * NOTE!
     * Possible performance bottleneck!!!
     * 
     * @param shsLabel
     * @return
     */
	public static ShsLabel newInstance(ShsLabel shsLabel) {
		return (ShsLabel) SerializationUtils.clone(shsLabel);
	}

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
     * Gets the value of the txId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTxId() {
        return txId;
    }

    /**
     * Sets the value of the txId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTxId(String value) {
        this.txId = value;
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
     * Gets the value of the shsAgreement property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShsAgreement() {
        return shsAgreement;
    }

    /**
     * Sets the value of the shsAgreement property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShsAgreement(String value) {
        this.shsAgreement = value;
    }

    /**
     * Gets the value of the transferType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public TransferType getTransferType() {
        if (transferType == null) {
            return TransferType.ASYNCH;
        } else {
            return transferType;
        }
    }

    /**
     * Sets the value of the transferType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransferType(TransferType value) {
        this.transferType = value;
    }

    /**
     * Gets the value of the messageType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public MessageType getMessageType() {
        if (messageType == null) {
            return MessageType.SIMPLE;
        } else {
            return messageType;
        }
    }

    /**
     * Sets the value of the messageType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageType(MessageType value) {
        this.messageType = value;
    }

    /**
     * Gets the value of the documentType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public MessageType getDocumentType() {
        if (documentType == null) {
            return MessageType.SIMPLE;
        } else {
            return documentType;
        }
    }

    /**
     * Sets the value of the documentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentType(MessageType value) {
        this.documentType = value;
    }

    /**
     * Gets the value of the sequenceType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public SequenceType getSequenceType() {
        return sequenceType;
    }

    /**
     * Sets the value of the sequenceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSequenceType(SequenceType value) {
        this.sequenceType = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Status getStatus() {
        if (status == null) {
            return Status.PRODUCTION;
        } else {
            return status;
        }
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(Status value) {
        this.status = value;
    }

    /**
     * Gets the value of the originatorOrFrom property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the originatorOrFrom property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOriginatorOrFrom().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Originator }
     * {@link From }
     * 
     * 
     */
    public List<Object> getOriginatorOrFrom() {
        if (originatorOrFrom == null) {
            originatorOrFrom = new ArrayList<Object>();
        }
        return this.originatorOrFrom;
    }
    
    public From getFrom() {
    	for(Object object: getOriginatorOrFrom()) {
    		if (object instanceof From) {
    			return (From)object;
    		}
    	}
    	
    	return null;
    }
    
    public Originator getOriginator() {
    	for(Object object: getOriginatorOrFrom()) {
    		if (object instanceof Originator) {
    			return (Originator)object;
    		}
    	}
    	
    	return null;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link To }
     *     
     */
    public To getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link To }
     *     
     */
    public void setTo(To value) {
        this.to = value;
    }

    /**
     * Gets the value of the endRecipient property.
     * 
     * @return
     *     possible object is
     *     {@link EndRecipient }
     *     
     */
    public EndRecipient getEndRecipient() {
        return endRecipient;
    }

    /**
     * Sets the value of the endRecipient property.
     * 
     * @param value
     *     allowed object is
     *     {@link EndRecipient }
     *     
     */
    public void setEndRecipient(EndRecipient value) {
        this.endRecipient = value;
    }

    /**
     * Gets the value of the product property.
     * 
     * @return
     *     possible object is
     *     {@link Product }
     *     
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Sets the value of the product property.
     * 
     * @param value
     *     allowed object is
     *     {@link Product }
     *     
     */
    public void setProduct(Product value) {
        this.product = value;
    }

    /**
     * Gets the value of the meta property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the meta property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMeta().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Meta }
     * 
     * 
     */
    public List<Meta> getMeta() {
        if (meta == null) {
            meta = new ArrayList<Meta>();
        }
        return this.meta;
    }

    /**
     * Gets the value of the subject property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the value of the subject property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubject(String value) {
        this.subject = value;
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
     * Gets the value of the content property.
     * 
     * @return
     *     possible object is
     *     {@link Content }
     *     
     */
    public Content getContent() {
        return content;
    }

    /**
     * Sets the value of the content property.
     * 
     * @param value
     *     allowed object is
     *     {@link Content }
     *     
     */
    public void setContent(Content value) {
        this.content = value;
    }

    /**
     * Gets the value of the history property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the history property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHistory().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link History }
     * 
     * 
     */
    public List<History> getHistory() {
        if (history == null) {
            history = new ArrayList<History>();
        }
        return this.history;
    }

	@Override
	public String toString() {
		return "ShsLabel [version=" + version + ", txId=" + txId + ", corrId="
				+ corrId + ", shsAgreement=" + shsAgreement + ", transferType="
				+ transferType + ", messageType=" + messageType
				+ ", documentType=" + documentType + ", sequenceType="
				+ sequenceType + ", status=" + status + ", originatorOrFrom="
				+ originatorOrFrom + ", to=" + to + ", endRecipient="
				+ endRecipient + ", product=" + product + ", meta=" + meta
				+ ", subject=" + subject + ", datetime=" + datetime
				+ ", content=" + content + ", history=" + history + "]";
	}

    
    
}
