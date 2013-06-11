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
package se.inera.axel.riv.internal;


import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.component.http.HttpOperationFailedException;
import org.apache.camel.component.http.SSLContextParametersSecureProtocolSocketFactory;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import se.inera.axel.riv.RivShsMappingService;
import se.inera.axel.shs.camel.ThrowExceptionOnShsErrorProcessor;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.mime.TransferEncoding;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.TransferType;

public class RivShsRouteBuilder extends RouteBuilder {
		
	@Override
	public void configure() throws Exception {
	
		Namespaces riv = new Namespaces("riv", "urn:riv:itintegration:registry:1");
		Namespaces addressing = new Namespaces("add", "http://www.w3.org/2005/08/addressing");

		configureSsl();
		
		// body: soap-envelope with riv service call
		from("jetty:{{rivInBridgeEndpoint}}?sslContextParametersRef=mySslContext").routeId("riv2shs")
		.onException(Exception.class)
			.handled(true)
			.bean(HttpResponseStatusExceptionResolver.class)
			.removeHeaders("Shs*")
			.end()
		.setHeader(ShsHeaders.TO).xpath("//riv:LogicalAddress", String.class, riv)
		.choice().when(header(ShsHeaders.TO).isEqualTo(""))
				.setHeader(ShsHeaders.TO).xpath("//add:To", String.class, addressing)
		.end()
		.validate(header(ShsHeaders.TO).isNotEqualTo(""))
		.setHeader(ShsHeaders.PRODUCT_ID, method("rivShsMapper", "mapRivServiceToShsProduct"))
		.setHeader(ShsHeaders.CORRID, header(RivShsMappingService.HEADER_RIV_CORRID))
		.setHeader(ShsHeaders.SEQUENCETYPE, constant(SequenceType.REQUEST))
		.setHeader(ShsHeaders.TRANSFERTYPE, constant(TransferType.SYNCH))
		.setHeader(ShsHeaders.DATAPART_TYPE, constant("xml"))
		.setHeader(ShsHeaders.DATAPART_FILENAME, simple("req-${in.header.ShsLabelCorrId}.xml"))
		.setHeader(ShsHeaders.DATAPART_CONTENTTYPE, constant("application/soap+xml"))
		.setHeader(ShsHeaders.DATAPART_TRANSFERENCODING, constant(TransferEncoding.BASE64))
		.beanRef("camelToShsConverter")
		.to("shs://{{rsEndpoint}}")
		.bean(new ThrowExceptionOnShsErrorProcessor())
		.beanRef("shsToCamelConverter");



		from("direct:soapFaultHandler").routeId("direct:soapFaultHandler")
		.transform(simple("${exception.responseBody}"))
		// TODO create response message label
		.beanRef("camelToShsConverter");


		// body: ShsMessage
		from("direct-vm:shs2riv").routeId("shs2riv")
		.onException(HttpOperationFailedException.class)
			.onWhen(new IsSoapFaultExceptionPredicate())
			.handled(true)
			.to("direct:soapFaultHandler")
			.end()
		.beanRef("shsToCamelConverter")
		.removeHeaders("CamelHttp*")
		.setHeader(RivShsMappingService.HEADER_SOAP_ACTION, method("rivShsMapper", "mapShsProductToRivService"))
		.setHeader(Exchange.HTTP_URI, method("rivShsMapper", "mapRivServiceToRivEndpoint"))
        .setHeader(Exchange.CONTENT_TYPE, header(ShsHeaders.DATAPART_CONTENTTYPE))
		.setHeader(RivShsMappingService.HEADER_RIV_CORRID, header(ShsHeaders.CORRID))
		.to("http://rivService?throwExceptionOnFailure=true")
		// TODO create response message label
		.beanRef("camelToShsConverter");

	}

	private void configureSsl() {
		SSLContextParameters sslContextParameters = getContext().getRegistry().lookup("mySslContext", SSLContextParameters.class);
		
		ProtocolSocketFactory factory =
			    new SSLContextParametersSecureProtocolSocketFactory(sslContextParameters);

		Protocol.registerProtocol("https",
				new Protocol(
						"https",
						factory,
						443));
	}
}
