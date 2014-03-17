package se.inera.axel.cxf;

import org.apache.camel.Exchange;
import org.apache.cxf.binding.soap.SoapFault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.ws.WebFault;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class TestSoapFault {
    @WebFault(targetNamespace = "urn:se.inera.axel")
    private static class TestWebFaultException extends RuntimeException {

    }
    public void causeSoapFault(Exchange exchange) {
        SoapFault fault = new SoapFault("Hello", SoapFault.FAULT_CODE_SERVER);
//        Element detail = fault.getOrCreateDetail();
//        detail.appendChild(detail.getOwnerDocument().createTextNode("tha detail"));

        Element detail = fault.getOrCreateDetail();
        Document ownerDocument = detail.getOwnerDocument();
        Text textNode = ownerDocument.createTextNode("se.inera.axel.healthId: ");
        Element faultDetails = ownerDocument.createElement("healthstatus");
        faultDetails.appendChild(textNode);
        detail.appendChild(faultDetails);
        exchange.getOut().setFault(true);
        exchange.getOut().setBody(fault);
    }
}
