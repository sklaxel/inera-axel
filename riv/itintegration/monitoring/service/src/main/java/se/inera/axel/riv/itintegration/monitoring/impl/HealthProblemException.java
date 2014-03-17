package se.inera.axel.riv.itintegration.monitoring.impl;

import se.inera.axel.monitoring.HealthReport;
import se.inera.axel.monitoring.HealthStatus;

import java.util.List;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class HealthProblemException extends RuntimeException {
    private final HealthReport healthReport;
    public HealthProblemException(String message, List<HealthStatus> healthStatuses) {
        super(message);
        this.healthReport = new HealthReport(healthStatuses);
    }

    public HealthReport getHealthReport() {
        return healthReport;
    }
}
