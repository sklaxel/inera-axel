package se.inera.axel.monitoring;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class HealthCheck {
    private String healthCheckId;

    private String objectNamePattern;

    private Map<String, String> expectedAttributes = new HashMap<>();

    public HealthCheck(String healthCheckId, String objectNamePattern, Map<String, String> expectedAttributes) {
        this.healthCheckId = healthCheckId;
        this.objectNamePattern = objectNamePattern;
        if (expectedAttributes != null) {
            this.expectedAttributes.putAll(expectedAttributes);
        }
    }

    public String getObjectNamePattern() {
        return objectNamePattern;
    }

    public String getHealthCheckId() {
        return healthCheckId;
    }

    public boolean hasAttributeChecks() {
        return expectedAttributes.size() > 0;
    }

    public String[] getAttributesToCheck() {
        Set<String> keySet = expectedAttributes.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    public boolean verifyAttribute(String name, Object value) {
        String expectedValue = expectedAttributes.get(name);

        return expectedValue == null ? true : expectedValue.equals(value);
    }

    public String getExpectedAttributeValue(String name) {
        return expectedAttributes.get(name);
    }
}
