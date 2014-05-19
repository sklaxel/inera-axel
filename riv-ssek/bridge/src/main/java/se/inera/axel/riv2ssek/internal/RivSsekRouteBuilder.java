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
package se.inera.axel.riv2ssek.internal;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.component.http.SSLContextParametersSecureProtocolSocketFactory;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.headers.Header;
import se.inera.axel.shs.processor.ShsHeaders;

import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPHeader;
import java.util.List;
import java.util.UUID;

import static org.apache.camel.builder.PredicateBuilder.not;
import static org.apache.camel.builder.PredicateBuilder.or;

/**
 * Defines Camel routes for RIV <---> SSEK
 */
public class RivSsekRouteBuilder extends RouteBuilder {
    private final Namespaces namespaces = new Namespaces("riv", "urn:riv:itintegration:registry:1")
            .add("add", "http://www.w3.org/2005/08/addressing")
            .add("soap", "http://schemas.xmlsoap.org/soap/envelope/")
            .add("ssek", "http://schemas.ssek.org/ssek/2006-05-10/");
    private final String SSEK_MAPPING = "ssekMapping";

    @Override
    public void configure() throws Exception {
        configureSsl();

        from("{{rivEndpoint}}").routeId("riv2ssek")
        .onException(Exception.class)
            .handled(true)
            .logHandled(true)
            .to("direct:soapFaultErrorResponse")
        .end()
        .setHeader("sender", constant("Inera"))
        .setHeader("receiver").xpath("//riv:LogicalAddress", String.class, namespaces)
        .choice().when(header("receiver").isEqualTo(""))
            .setHeader("receiver").xpath("//add:To", String.class, namespaces)
        .end()
        .validate(not(header("receiver").isEqualTo("")))
        .setHeader("txId", header("x-vp-correlation-id"))
        .choice().when(or(header("txId").isNull(), header("txId").isEqualTo("")))
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader("txId", UUID.randomUUID().toString());
                }
            })
        .end()
        .to("direct:camel2ssek");

        from("direct:camel2ssek")
        .onException(Exception.class)
            .handled(true)
            .logHandled(true)
            .to("direct:soapFaultErrorResponse")
        .end()
        .setProperty("ssekService",
                method("rivSsekMappingService", "lookupSsekService(${header.receiver}, ${header.SOAPAction}))"))
        .to("xquery:META-INF/xquery/camel2ssek.xquery")
        .removeHeaders("*")
        .setHeader("SOAPAction", constant(""))
        .setHeader(Exchange.CONTENT_TYPE, constant("application/xml"))
        .setHeader(Exchange.HTTP_URI, simple("${property.ssekService.address}"))
        .to("jetty://http://ssekService?throwExceptionOnFailure=false");

        from("direct:soapFaultErrorResponse")
        .setHeader("faultstring").simple("Error reported: ${exception.message} - could not process request.")
        .to("velocity:/META-INF/velocity/soapfault.vm")
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
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
