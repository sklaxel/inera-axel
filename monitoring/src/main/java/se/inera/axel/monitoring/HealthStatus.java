package se.inera.axel.monitoring;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class HealthStatus implements Serializable {
    private String healthId;
    private SeverityLevel level;
    private String message;
    private String resource;
    private double healthPercent;

    private HealthStatus() {
    }

    public HealthStatus(String healthId, SeverityLevel level, String message, String resource, double healthPercent) {
        this.healthId = healthId;
        this.level = level;
        this.message = message;
        this.resource = resource;
        this.healthPercent = healthPercent;
    }

    public String getHealthId() {
        return healthId;
    }

    public SeverityLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getResource() {
        return resource;
    }
    public double getHealthPercent() {
        return healthPercent;
    }

    @Override
    public String toString() {
        return "HealthStatus{" +
               "healthId='" + healthId + '\'' +
               ", level=" + level +
               ", message='" + message + '\'' +
               ", resource='" + resource + '\'' +
               ", healthPercent=" + healthPercent +
               '}';
    }
}
