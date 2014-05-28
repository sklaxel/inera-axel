package se.inera.axel.ssek.common;

import se.inera.axel.ssek.common.schema.ssek.SSEK;

import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public enum SsekNamespaces {
    SSEK("ssek", se.inera.axel.ssek.common.schema.ssek.SSEK.class.getPackage().getAnnotation(XmlSchema.class).namespace()),
    SSEKP("ssekp", "http://schemas.ssek.org/ ssek/2006-05-10/policy");

    private final String defaultPrefix;
    private final String namespaceURI;

    SsekNamespaces(String defaultPrefix, String namespaceURI) {
        this.defaultPrefix = defaultPrefix;
        this.namespaceURI = namespaceURI;
    }

    /**
     * Return the QName for the given localPart. Uses the default prefix.
     *
     * @param localPart the local part of the element or attribute.
     *
     * @return a QName for the given local part
     */
    public QName getQName(String localPart) {
        return new QName(this.namespaceURI, localPart, defaultPrefix);
    }

    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }
}
