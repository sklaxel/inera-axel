package se.inera.axel.monitoring;

import java.util.Map;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class MongoDbConnectionPoolHealthCheck extends JmxHealthCheck {
    public MongoDbConnectionPoolHealthCheck(String healthCheckId, String objectNamePattern, Map<String, String> expectedAttributes) {
        super(healthCheckId, objectNamePattern, expectedAttributes);
    }


}
