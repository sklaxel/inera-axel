package se.inera.axel.cxf;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.w3c.dom.Element;
import se.inera.axel.monitoring.HealthReport;
import se.inera.axel.riv.itintegration.monitoring.impl.HealthProblemException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class TestOutFaultInterceptor extends AbstractSoapInterceptor {
    private static final JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(HealthReport.class);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    public TestOutFaultInterceptor() {
        super(Phase.MARSHAL);
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        System.out.println("--------------------Interceptor was here");
        Fault fault = (Fault)message.getContent(Exception.class);
        if (fault.getCause() instanceof HealthProblemException) {
            HealthProblemException exception = (HealthProblemException) fault.getCause();
            Element detail = fault.getOrCreateDetail();
            try {
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.marshal(exception.getHealthReport(), detail);
            } catch (JAXBException e) {
                // TODO fix
                System.out.println(e.getMessage());
            }
        }
    }
}
