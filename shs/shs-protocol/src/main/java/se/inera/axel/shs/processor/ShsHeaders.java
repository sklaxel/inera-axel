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
package se.inera.axel.shs.processor;

/**
 * Contains SHS Headers, HTTP headers and status codes specific to the SHS protocol.
 *
 */
public class ShsHeaders {
	public static final String LABEL = "ShsLabel";
	
	public static final String FROM = "ShsLabelFrom";
	public static final String TO = "ShsLabelTo";
	public static final String PRODUCT_ID = "ShsLabelProductId";
	public static final String PRODUCT = "ShsLabelProduct";
	public static final String AGREEMENT = "ShsLabelAgreement";
	public static final String AGREEMENT_ID = "ShsLabelAgreementId";
	public static final String SUBJECT = "ShsLabelSubject";
	public static final String ORIGINATOR = "ShsLabelOriginator";
	public static final String ENDRECIPIENT = "ShsLabelEndRecipient";
	public static final String TXID = "ShsLabelTxId";
	public static final String CORRID = "ShsLabelCorrId";
	public static final String SEQUENCETYPE = "ShsLabelSequenceType";
	public static final String STATUS = "ShsLabelStatus";
	public static final String DATETIME = "ShsLabelDateTime";
	public static final String TRANSFERTYPE = "ShsLabelTransferType";
	public static final String MESSAGETYPE = "ShsLabelMessageType";
	public static final String CONTENT_ID = "ShsLabelContentId";
	public static final String CONTENT_COMMENT = "ShsLabelContentComment";
	public static final String META = "ShsLabelMeta";

	public static final String DATAPART_TYPE = "ShsDataPartType";
	public static final String DATAPART_CONTENTTYPE = "ShsDataPartContentType";
	public static final String DATAPART_FILENAME = "ShsDataPartFileName";
	public static final String DATAPART_CONTENTLENGTH = "ShsDataPartContentLength";
	public static final String DATAPART_TRANSFERENCODING = "ShsDataPartTransferEncoding";
	
	public static final String DESTINATION_URI = "ShsDestinationUri";

    public static final String X_SHS_TXID = "X-shs-txid";
    public static final String X_SHS_CORRID = "X-shs-corrid";
    public static final String X_SHS_CONTENTID = "X-shs-contentid";
    public static final String X_SHS_LOCALID = "X-shs-localid";
    public static final String X_SHS_NODEID = "X-shs-nodeid";
    public static final String X_SHS_ARRIVALDATE = "X-shs-arrivaldate";
    public static final String X_SHS_DUPLICATEMSG = "X-shs-duplicatemsg";
    public static final String X_SHS_ERRORCODE = "X-shs-errorcode";
}
