package se.inera.axel.monitoring;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ReflectionException;
import java.util.List;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public interface HealthCheck {
    String getHealthCheckId();

    void check(List<HealthStatus> healthStatuses, MBeanServer mBeanServer) throws Exception;
}
