package se.inera.axel.monitoring;

import org.testng.annotations.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.util.Arrays;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class HealthReportMarshallingTest {
    @Test
    public void marshalHealtList() throws Exception {
        HealthStatus healthStatus = new HealthStatus("healthId", SeverityLevel.ERROR, "message", "resourcer", 0.0);
        HealthReport healthReport = new HealthReport(Arrays.asList(healthStatus));

        JAXBContext jaxbContext = JAXBContext.newInstance(HealthReport.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(healthReport, System.out);
    }
}
