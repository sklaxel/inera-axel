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
package se.inera.axel.shs.xml.product;

import com.natpryce.makeiteasy.*;

import java.util.Collections;
import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static com.natpryce.makeiteasy.Property.newProperty;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class ShsProductMaker {
    public static final String DEFAULT_COMMON_NAME = "testProduct1";
    public static final String DEFAULT_PRODUCT_ID = "00000000-0000-0000-0000-000000000001";
    public static final String DEFAULT_VERSION = "1.2";
    public static final String DEFAULT_RESP_REQUIRED = "yes";

    public static final String DEFAULT_PRINCIPAL_COMMON_NAME = "Axel test";
    public static final String DEFAULT_PRINCIPAL_ORG_NUMBER = "0000000000";

    public static final String DEFAULT_MIME_SUBTYPE = "xml";
    public static final String DEFAULT_MIME_TEXT_CHARSET = "iso-8859-1";
    public static final String DEFAULT_MIME_TRANSFER_ENCODING = "binary";
    public static final String DEFAULT_MIME_TYPE = "text";

    public static final String DEFAULT_DSIG_ALGORITHM = "RSA";
    public static final String DEFAULT_DSIG_KEY_LENGTH = "1024";

    public static final String DEFAULT_ENCRYPTION_ALGORITHM = "RC4";
    public static final String DEFAULT_ENCRYPTION_KEY_LENGTH = "128";

    public static final String DEFAULT_DIGEST_ALGORITHM = "SHA1";

    public static final String DEFAULT_DATA_DATAPART_TYPE = "text";
    public static final String DEFAULT_DATA_MAX_OCCURS = "1";
    public static final String DEFAULT_DATA_MIN_OCCURS = "1";

    public static class ShsProductInstantiator implements Instantiator<ShsProduct> {
        public static final Property<ShsProduct, String> commonName = newProperty();
        public static final Property<ShsProduct, String> description = newProperty();
        public static final Property<ShsProduct, String> labeledURI = newProperty();
        public static final Property<ShsProduct, Principal> principal = newProperty();
        public static final Property<ShsProduct, String> respRequired = newProperty();
        public static final Property<ShsProduct, String> uuid = newProperty();
        public static final Property<ShsProduct, String> version = newProperty();
        public static final Property<ShsProduct, List<Data>> data = newProperty();
        public static final Property<ShsProduct, List<ReplyData>> replyData = newProperty();

        @Override
        public ShsProduct instantiate(
                PropertyLookup<ShsProduct> lookup) {
            ShsProduct product = new ShsProduct();
            product.setCommonName(lookup.valueOf(commonName, DEFAULT_COMMON_NAME));
            product.setDescription(lookup.valueOf(description, new SameValueDonor<String>(null)));
            product.setLabeledURI(lookup.valueOf(labeledURI, new SameValueDonor<String>(null)));
            product.setPrincipal(lookup.valueOf(principal, a(Principal)));
            product.setRespRequired(lookup.valueOf(respRequired, DEFAULT_RESP_REQUIRED));
            product.setUuid(lookup.valueOf(uuid, DEFAULT_PRODUCT_ID));
            product.setVersion(lookup.valueOf(version, DEFAULT_VERSION));
            product.getData().addAll(lookup.valueOf(data, listOf(a(Data))));
            product.getReplyData().addAll(lookup.valueOf(replyData, Collections.<ReplyData>emptyList()));

            return product;
        }
    }

    public static final ShsProductInstantiator ShsProduct = new ShsProductInstantiator();

    public static class PrincipalInstantiator implements Instantiator<Principal> {
        public static final Property<Principal, String> commonName = newProperty();
        public static final Property<Principal, String> labeledURI = newProperty();
        public static final Property<Principal, String> value = newProperty();

        @Override
        public Principal instantiate(
                PropertyLookup<Principal> lookup) {
            Principal principal = new Principal();
            principal.setCommonName(lookup.valueOf(commonName, DEFAULT_PRINCIPAL_COMMON_NAME));
            principal.setLabeledURI(lookup.valueOf(labeledURI, new SameValueDonor<String>(null)));
            principal.setValue(lookup.valueOf(value, DEFAULT_PRINCIPAL_ORG_NUMBER));

            return principal;
        }
    }

    public static final PrincipalInstantiator Principal = new PrincipalInstantiator();

    public static class DataInstantiator implements Instantiator<Data> {
        public static final Property<Data, String> dataType = newProperty();
        public static final Property<Data, String> datapartType = newProperty();
        public static final Property<Data, String> description = newProperty();
        public static final Property<Data, String> maxOccurs = newProperty();
        public static final Property<Data, Mime> mime = newProperty();
        public static final Property<Data, String> minOccurs = newProperty();
        public static final Property<Data, Security> security = newProperty();

        @Override
        public Data instantiate(
                PropertyLookup<Data> lookup) {
            Data data = new Data();
            data.setDataType(lookup.valueOf(dataType, new SameValueDonor<String>(null)));
            data.setDatapartType(lookup.valueOf(datapartType, DEFAULT_DATA_DATAPART_TYPE));
            data.setDescription(lookup.valueOf(description, new SameValueDonor<String>(null)));
            data.setMaxOccurs(lookup.valueOf(maxOccurs, DEFAULT_DATA_MAX_OCCURS));
            data.setMime(lookup.valueOf(mime, a(Mime)));
            data.setMinOccurs(lookup.valueOf(minOccurs, DEFAULT_DATA_MIN_OCCURS));
            data.setSecurity(lookup.valueOf(security, new SameValueDonor<se.inera.axel.shs.xml.product.Security>(null)));

            return data;
        }
    }

    public static final DataInstantiator Data = new DataInstantiator();

    public static class ReplyDataInstantiator implements Instantiator<ReplyData> {
        public static final Property<ReplyData, String> dataType = newProperty();
        public static final Property<ReplyData, String> datapartType = newProperty();
        public static final Property<ReplyData, String> description = newProperty();
        public static final Property<ReplyData, String> maxOccurs = newProperty();
        public static final Property<ReplyData, Mime> mime = newProperty();
        public static final Property<ReplyData, String> minOccurs = newProperty();
        public static final Property<ReplyData, Security> security = newProperty();

        @Override
        public ReplyData instantiate(
                PropertyLookup<ReplyData> lookup) {
            ReplyData replyData = new ReplyData();
            replyData.setDataType(lookup.valueOf(dataType, new SameValueDonor<String>(null)));
            replyData.setDatapartType(lookup.valueOf(datapartType, DEFAULT_DATA_DATAPART_TYPE));
            replyData.setDescription(lookup.valueOf(description, new SameValueDonor<String>(null)));
            replyData.setMaxOccurs(lookup.valueOf(maxOccurs, DEFAULT_DATA_MAX_OCCURS));
            replyData.setMime(lookup.valueOf(mime, a(Mime)));
            replyData.setMinOccurs(lookup.valueOf(minOccurs, DEFAULT_DATA_MIN_OCCURS));
            replyData.setSecurity(lookup.valueOf(security, new SameValueDonor<se.inera.axel.shs.xml.product.Security>(null)));

            return replyData;
        }
    }

    public static final ReplyDataInstantiator ReplyData = new ReplyDataInstantiator();

    public static class MimeInstantiator implements Instantiator<Mime> {
        public static final Property<Mime, String> subtype = newProperty();
        public static final Property<Mime, String> textCharset = newProperty();
        public static final Property<Mime, String> transferEncoding = newProperty();
        public static final Property<Mime, String> type = newProperty();

        @Override
        public Mime instantiate(
                PropertyLookup<Mime> lookup) {
            Mime mime = new Mime();
            mime.setSubtype(lookup.valueOf(subtype, DEFAULT_MIME_SUBTYPE));
            mime.setTextCharset(lookup.valueOf(textCharset, DEFAULT_MIME_TEXT_CHARSET));
            mime.setTransferEncoding(lookup.valueOf(transferEncoding, DEFAULT_MIME_TRANSFER_ENCODING));
            mime.setType(lookup.valueOf(type, DEFAULT_MIME_TYPE));

            return mime;
        }
    }

    public static final MimeInstantiator Mime = new MimeInstantiator();

    public static class SecurityInstantiator implements Instantiator<Security> {
        public static final Property<Security, Digest> digest = newProperty();
        public static final Property<Security, Dsig> dsig = newProperty();
        public static final Property<Security, Encryption> encryption = newProperty();

        @Override
        public Security instantiate(
                PropertyLookup<Security> lookup) {
            Security security = new Security();
            security.setDigest(lookup.valueOf(digest, new SameValueDonor<se.inera.axel.shs.xml.product.Digest>(null)));
            security.setDsig(lookup.valueOf(dsig, new SameValueDonor<se.inera.axel.shs.xml.product.Dsig>(null)));
            security.setEncryption(lookup.valueOf(encryption, new SameValueDonor<se.inera.axel.shs.xml.product.Encryption>(null)));

            return security;
        }
    }

    public static final SecurityInstantiator Security = new SecurityInstantiator();

    public static class DigestInstantiator implements Instantiator<Digest> {
        public static final Property<Digest, String> algorithm = newProperty();

        @Override
        public Digest instantiate(
                PropertyLookup<Digest> lookup) {
            Digest digest = new Digest();
            digest.setAlgorithm(lookup.valueOf(algorithm, DEFAULT_DIGEST_ALGORITHM));

            return digest;
        }
    }

    public static final DigestInstantiator Digest = new DigestInstantiator();

    public static class DsigInstantiator implements Instantiator<Dsig> {
        public static final Property<Dsig, String> keyLength = newProperty();

        @Override
        public Dsig instantiate(
                PropertyLookup<Dsig> lookup) {
            Dsig dsig = new Dsig();
            dsig.setAlgorithm(DEFAULT_DSIG_ALGORITHM);
            dsig.setKeyLength(lookup.valueOf(keyLength, DEFAULT_DSIG_KEY_LENGTH));

            return dsig;
        }
    }

    public static final DsigInstantiator Dsig = new DsigInstantiator();

    public static class EncryptionInstantiator implements Instantiator<Encryption> {
        public static final Property<Encryption, String> algorithm = newProperty();
        public static final Property<Encryption, String> keyLength = newProperty();

        @Override
        public Encryption instantiate(
                PropertyLookup<Encryption> lookup) {
            Encryption encryption = new Encryption();
            encryption.setAlgorithm(lookup.valueOf(algorithm, DEFAULT_ENCRYPTION_ALGORITHM));
            encryption.setKeyLength(lookup.valueOf(keyLength, DEFAULT_ENCRYPTION_KEY_LENGTH));

            return encryption;
        }
    }

    public static final EncryptionInstantiator Encryption = new EncryptionInstantiator();

    public static final Maker<ShsProduct> testProduct1 = a(ShsProduct);

    public static final Maker<ShsProduct> testProduct2 = a(ShsProduct,
            with(ShsProductInstantiator.respRequired, "no"),
            with(ShsProductInstantiator.commonName, "testProdukt2"),
            with(ShsProductInstantiator.principal, a(Principal,
                    with(PrincipalInstantiator.value, "1111111111"))));

    public static final Maker<ShsProduct> testProduct3 = a(ShsProduct);

    public static final Maker<ShsProduct> testProduct4 = a(ShsProduct);

}
