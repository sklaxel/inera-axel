package se.inera.axel.monitoring;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.openmbean.CompositeData;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class MongoDbConnectionPoolHealthCheck extends JmxHealthCheck {
    // Used by Yaml
    private MongoDbConnectionPoolHealthCheck() {

    }

    public MongoDbConnectionPoolHealthCheck(String healthCheckId, String objectNamePattern, Map<String, String> expectedAttributes) {
        super(healthCheckId, objectNamePattern, expectedAttributes);
    }

    @Override
    protected void checkAdditional(List<HealthStatus> healthStatuses, Set<ObjectInstance> foundMBeans, MBeanServer mBeanServer) throws Exception {
        for (ObjectInstance foundMBean : foundMBeans) {
            Object statistics = mBeanServer.getAttribute(foundMBean.getObjectName(), "Statistics");
            Integer maxSize = (Integer) mBeanServer.getAttribute(foundMBean.getObjectName(), "MaxSize");
            if (statistics instanceof CompositeData) {
                CompositeData statisticsData = (CompositeData)statistics;
                if (maxSize.equals(statisticsData.get("inUse"))) {
                    healthStatuses.add(
                        new HealthStatus(
                            getHealthCheckId(),
                            SeverityLevel.WARNING,
                            String.format("All %1s available connections to MongoDB in use.", statisticsData.get("total")),
                            foundMBean.getObjectName().getCanonicalName(),
                            1.0));
                }
            }
        }
    }
}
