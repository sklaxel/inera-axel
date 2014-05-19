package se.inera.axel.riv2ssek.internal;

import org.apache.camel.Exchange;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.language.NamespacePrefix;
import org.apache.camel.language.XPath;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class RegisterMedicalCertificateMockResponder {
    private final Namespaces namespaces = new Namespaces("riv", "urn:riv:itintegration:registry:1")
            .add("add", "http://www.w3.org/2005/08/addressing")
            .add("soapenv", "http://schemas.xmlsoap.org/soap/envelope/")
            .add("ssek", "http://schemas.ssek.org/ssek/2006-05-10/");

    public Object respond(Source source,
                          @XPath(value = "//ssek:SSEK/ssek:ReceiverId/text()",
                                  namespaces = @NamespacePrefix(
                                          prefix = "ssek",
                                          uri = "http://schemas.ssek.org/ssek/2006-05-10/"
                                  )) String receiverId,
                          Exchange exchange) {
        if ("1111111111".equals(receiverId)) {
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return getClass().getResourceAsStream("/ssek-responses/500_ReceiverIdUnknown.xml");
        } else {
            return getClass().getResourceAsStream("/ssek-responses/registerMedicalCertificateResponse.xml");
        }
    }
}
