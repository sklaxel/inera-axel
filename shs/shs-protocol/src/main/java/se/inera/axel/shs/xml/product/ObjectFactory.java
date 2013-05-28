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

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the se.inera.axel.shs.xml.product package.
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: se.inera.axel.shs.xml.product
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ReplyData }
     * 
     */
    public ReplyData createReplyData() {
        return new ReplyData();
    }

    /**
     * Create an instance of {@link Mime }
     * 
     */
    public Mime createMime() {
        return new Mime();
    }

    /**
     * Create an instance of {@link Security }
     * 
     */
    public Security createSecurity() {
        return new Security();
    }

    /**
     * Create an instance of {@link Principal }
     * 
     */
    public Principal createPrincipal() {
        return new Principal();
    }

    /**
     * Create an instance of {@link Encryption }
     * 
     */
    public Encryption createEncryption() {
        return new Encryption();
    }

    /**
     * Create an instance of {@link Data }
     * 
     */
    public Data createData() {
        return new Data();
    }

    /**
     * Create an instance of {@link ShsProduct }
     * 
     */
    public ShsProduct createShsProduct() {
        return new ShsProduct();
    }

    /**
     * Create an instance of {@link Digest }
     * 
     */
    public Digest createDigest() {
        return new Digest();
    }

    /**
     * Create an instance of {@link Dsig }
     * 
     */
    public Dsig createDsig() {
        return new Dsig();
    }

}
