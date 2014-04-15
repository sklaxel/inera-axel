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
package se.inera.axel.riv.impl;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.testng.CamelTestSupport;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.DocumentFragment;
import se.inera.axel.shs.camel.DefaultCamelToShsMessageProcessor;
import se.inera.axel.shs.camel.DefaultShsMessageToCamelProcessor;

import javax.naming.Context;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class RivSoapTransformationTest extends CamelTestSupport {

    private URL rivSoapRequest = getClass().getResource("/make-booking-request.xml");

    @Produce(uri = "direct:extractPayload")
    protected ProducerTemplate template;

    @BeforeClass
    public void beforeClass() {
        System.setProperty("java.io.tmpdir", "/tmp/");
    }

    @Test
    public void extractRivPayload() throws IOException {
        template.sendBody(rivSoapRequest.openStream());
    }

    @Test
    public void createSoapResponse() {
        template.sendBody("direct:createRivResponse", "<ns:rivPayloadExample xmlns:ns=\"urn:axel:example\"><ns:hello>World</ns:hello></ns:rivPayloadExample>");
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                Namespaces namespaces = new Namespaces("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
                namespaces.add("booking", "urn:riv:crm:scheduling:MakeBookingResponder:1");
                namespaces.add("example", "urn:axel:example");

                from("direct:extractPayload")
                .transform().xpath("/soapenv:Envelope/soapenv:Body/*", namespaces)
                .to("file://target/outbox?fileName=rivPayload.xml")
                .validate().xpath("/booking:MakeBooking", namespaces);

                from("direct:createRivResponse")
                .transform().xquery("resource:classpath:xquery/rivShsSoapResponse.xquery")
                .to("file://target/outbox?fileName=rivResponse.xml")
                .validate().xpath("/soapenv:Envelope/soapenv:Body[count(*) = 1]/example:rivPayloadExample/example:hello[text() = 'World']", namespaces);
            }
        };
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        registry.bind("properties", new PropertiesComponent("test.properties"));

        return registry;
    }
}
