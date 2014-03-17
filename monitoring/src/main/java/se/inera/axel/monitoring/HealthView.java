package se.inera.axel.monitoring;

import org.springframework.jmx.export.annotation.ManagedOperation;

import javax.management.openmbean.TabularData;
import java.util.List;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public interface HealthView {
    @ManagedOperation
    TabularData health() throws Exception;

    @ManagedOperation
    List<HealthStatus> healthList() throws Exception;
}
