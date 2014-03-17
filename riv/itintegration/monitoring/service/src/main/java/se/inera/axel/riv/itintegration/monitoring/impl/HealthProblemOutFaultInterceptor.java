package se.inera.axel.riv.itintegration.monitoring.impl;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.w3c.dom.Element;
import se.inera.axel.monitoring.HealthReport;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class HealthProblemOutFaultInterceptor extends AbstractSoapInterceptor {
    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HealthProblemOutFaultInterceptor.class);

    private static final JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(HealthReport.class);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    public HealthProblemOutFaultInterceptor() {
        super(Phase.MARSHAL);
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        Fault fault = (Fault)message.getContent(Exception.class);
        if (fault.getCause() instanceof HealthProblemException) {
            HealthProblemException exception = (HealthProblemException) fault.getCause();
            Element detail = fault.getOrCreateDetail();
            try {
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.marshal(exception.getHealthReport(), detail);
            } catch (JAXBException e) {
                log.warn("Failed to marshal health report");
            }
        }
    }
}
