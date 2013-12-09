package se.inera.axel.riv.impl;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.testng.CamelTestSupport;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.DocumentFragment;

import java.io.IOException;
import java.net.URL;

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
        template.sendBody("direct:createRivResponse", "<ns:rivPayloadExample xmlns:ns=\"urn:axel:example\">Hello World</ns:rivPayloadExample>");
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
                .validate().xpath("/soapenv:Envelope/soapenv:Body/example:rivPayloadExample[text() = 'Hello World']", namespaces);
            }
        };
    }
}
