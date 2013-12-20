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
import org.apache.camel.component.http.SSLContextParametersSecureProtocolSocketFactory;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import se.inera.axel.riv.RivShsMappingService;
import se.inera.axel.shs.camel.ThrowExceptionOnShsErrorProcessor;
import se.inera.axel.shs.mime.TransferEncoding;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.TransferType;

import javax.xml.transform.OutputKeys;

public class RivShsRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        Namespaces riv = new Namespaces("riv", "urn:riv:itintegration:registry:1");
        Namespaces addressing = new Namespaces("add", "http://www.w3.org/2005/08/addressing");
        Namespaces soapenv = new Namespaces("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");

        configureSsl();

        // body: soap-envelope with riv service call
        from("{{rivInBridgeEndpoint}}").routeId("riv2shs")
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
                .transform().xpath("/soapenv:Envelope/soapenv:Body/*", soapenv)
                .setHeader(ShsHeaders.PRODUCT_ID, method("rivShsMapper", "mapRivServiceToShsProduct"))
                .setHeader(ShsHeaders.CORRID, header(RivShsMappingService.HEADER_RIV_CORRID))
                .setHeader(ShsHeaders.SEQUENCETYPE, constant(SequenceType.REQUEST))
                .setHeader(ShsHeaders.TRANSFERTYPE, constant(TransferType.SYNCH))
                .setHeader(ShsHeaders.DATAPART_TYPE, constant("xml"))
                .setHeader(ShsHeaders.DATAPART_FILENAME, simple("req-${in.header.ShsLabelCorrId}.xml"))
                .setHeader(ShsHeaders.DATAPART_CONTENTTYPE, constant("application/soap+xml"))
                .setHeader(ShsHeaders.DATAPART_TRANSFERENCODING, constant(TransferEncoding.BASE64))
                .setHeader(org.apache.camel.converter.jaxp.XmlConverter.OUTPUT_PROPERTIES_PREFIX + OutputKeys.OMIT_XML_DECLARATION, constant("no"))
                .beanRef("camelToShsConverter")
                .to("shs://{{rsEndpoint}}")
                .bean(new ThrowExceptionOnShsErrorProcessor())
                .beanRef("shsToCamelConverter")
                .to("xquery:xquery/rivShsSoapResponse.xquery");

        // body: ShsMessage
        from("{{shsInBridgeEndpoint}}").routeId("shs2riv")
                .beanRef("shsToCamelConverter")
                .bean(ShsToRivHeaderMapper.class, "addRivHeaders")
                .removeHeaders("CamelHttp*")
                .setHeader(RivShsMappingService.HEADER_SOAP_ACTION, method("rivShsMapper", "mapShsProductToRivService"))
                .setHeader(Exchange.DESTINATION_OVERRIDE_URL, method("rivShsMapper", "mapRivServiceToRivEndpoint"))
                .setHeader(Exchange.CONTENT_TYPE, header(ShsHeaders.DATAPART_CONTENTTYPE))
                .setHeader(RivShsMappingService.HEADER_RIV_CORRID, header(ShsHeaders.CORRID))
                .to("cxf:http://rivService?dataFormat=PAYLOAD")
                // TODO create response message label
                .beanRef("camelToShsConverter");

    }

    private void configureSsl() {
        SSLContextParameters sslContextParameters = getContext().getRegistry().lookupByNameAndType("mySslContext", SSLContextParameters.class);

        ProtocolSocketFactory factory =
                new SSLContextParametersSecureProtocolSocketFactory(sslContextParameters);

        Protocol.registerProtocol("https",
                new Protocol(
                        "https",
                        factory,
                        443));
    }
}
