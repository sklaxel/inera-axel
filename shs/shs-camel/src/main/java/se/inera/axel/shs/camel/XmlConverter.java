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
package se.inera.axel.shs.camel;

import org.apache.camel.Converter;
import se.inera.axel.shs.processor.ShsAgreementMarshaller;
import se.inera.axel.shs.processor.ShsLabelMarshaller;
import se.inera.axel.shs.processor.ShsManagementMarshaller;
import se.inera.axel.shs.processor.ShsProductMarshaller;
import se.inera.axel.shs.xml.agreement.ShsAgreement;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.management.ShsManagement;
import se.inera.axel.shs.xml.product.ShsProduct;

import java.io.InputStream;

@Converter
public class XmlConverter {
	private static ShsLabelMarshaller shsLabelMarshaller = new ShsLabelMarshaller();
	private static ShsManagementMarshaller shsManagementMarshaller = new ShsManagementMarshaller();
	private static ShsAgreementMarshaller shsAgreementMarshaller = new ShsAgreementMarshaller();
	private static ShsProductMarshaller shsProductMarshaller = new ShsProductMarshaller();

	@Converter
	public static ShsLabel convertStringToShsLabel(String xml) throws Exception {
		try {
			return shsLabelMarshaller.unmarshal(xml);
		} catch (Exception e) {
			return null;
		}

	}

	@Converter
	public static ShsLabel convertStreamToShsLabel(InputStream xml) throws Exception {
		try {
			return shsLabelMarshaller.unmarshal(xml);
		} catch (Exception e) {
			return null;
		}

	}

	@Converter
	public static String convertShsLabelToString(ShsLabel object) throws Exception {
		try {
			return shsLabelMarshaller.marshal(object);
		} catch (Exception e) {
			return null;
		}
	}

	@Converter
	public static ShsManagement convertStringToShsManagement(String xml) {
		try {
			return shsManagementMarshaller.unmarshal(xml);
		} catch (Exception e) {
			return null;
		}
	}

	@Converter
	public static ShsManagement convertStreamToShsManagement(InputStream xml) {
		try {
			return shsManagementMarshaller.unmarshal(xml);
		} catch (Exception e) {
			return null;
		}
	}

	@Converter
	public static String convertShsManagementToString(ShsManagement object) throws Exception {
		try {
			return shsManagementMarshaller.marshal(object);
		} catch (Exception e) {
			return null;
		}
	}


	@Converter
	public static ShsAgreement convertStringToShsAgreement(String xml) {
		try {
			return shsAgreementMarshaller.unmarshal(xml);
		} catch (Exception e) {
			return null;
		}
	}

	@Converter
	public static ShsAgreement convertStreamToShsAgreement(InputStream xml) {
		try {
			return shsAgreementMarshaller.unmarshal(xml);
		} catch (Exception e) {
			return null;
		}
	}

	@Converter
	public static String convertShsAgreementToString(ShsAgreement object) throws Exception {
		try {
			return shsAgreementMarshaller.marshal(object);
		} catch (Exception e) {
			return null;
		}
	}

	@Converter
	public static ShsProduct convertStringToShsProduct(String xml) {
		try {
			return shsProductMarshaller.unmarshal(xml);
		} catch (Exception e) {
			return null;
		}
	}

	@Converter
	public static ShsProduct convertStreamToShsProduct(InputStream xml) {
		try {
			return shsProductMarshaller.unmarshal(xml);
		} catch (Exception e) {
			return null;
		}
	}

	@Converter
	public static String convertShsProductToString(ShsProduct object) throws Exception {
		try {
			return shsProductMarshaller.marshal(object);
		} catch (Exception e) {
			return null;
		}
	}
}
