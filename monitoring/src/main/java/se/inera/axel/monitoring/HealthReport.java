package se.inera.axel.monitoring;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class HealthReport {
    @XmlElement(name = "healthStatus")
    private List<HealthStatus> healthStatuses;

    // Default constructor required by jaxb
    public HealthReport() {
    }

    public HealthReport(List<HealthStatus> healthStatuses) {
        this.healthStatuses = healthStatuses;
    }

    public List<HealthStatus> getHealthStatuses() {
        return healthStatuses;
    }
}
