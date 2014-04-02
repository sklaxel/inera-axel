package se.inera.axel.monitoring;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class JmxHealthCheck implements HealthCheck {
    private String healthCheckId;

    private String objectNamePattern;

    private Map<String, String> expectedAttributes = new HashMap<>();

    // Used by Yaml
    protected JmxHealthCheck() {

    }

    public JmxHealthCheck(String healthCheckId, String objectNamePattern, Map<String, String> expectedAttributes) {
        this.healthCheckId = healthCheckId;
        this.objectNamePattern = objectNamePattern;
        if (expectedAttributes != null) {
            this.expectedAttributes.putAll(expectedAttributes);
        }
    }

    public String getObjectNamePattern() {
        return objectNamePattern;
    }

    @Override
    public String getHealthCheckId() {
        return healthCheckId;
    }

    private boolean hasAttributeChecks() {
        return expectedAttributes.size() > 0;
    }

    private String[] getAttributesToCheck() {
        Set<String> keySet = expectedAttributes.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    private boolean verifyAttribute(String name, Object value) {
        String expectedValue = expectedAttributes.get(name);

        return expectedValue == null ? true : expectedValue.equals(value);
    }

    private String getExpectedAttributeValue(String name) {
        return expectedAttributes.get(name);
    }

    private void checkAttributes(List<HealthStatus> healthStatuses, Set<ObjectInstance> foundMBeans, MBeanServer mBeanServer) throws Exception {
        for (ObjectInstance objectInstance : foundMBeans) {
            AttributeList attributes = mBeanServer.getAttributes(objectInstance.getObjectName(), getAttributesToCheck());
            String[] attributesToCheck = getAttributesToCheck();
            if (attributesToCheck.length != attributes.size()) {
                List<String> missing = new ArrayList<>(Arrays.asList(attributesToCheck));
                for (Attribute attribute : attributes.asList()) {
                    missing.remove(attribute.getName());
                }

                // Throws exception with the reason for why the attribute is missing
                for (String missingAttribute : missing) {
                    mBeanServer.getAttribute(objectInstance.getObjectName(), missingAttribute);
                }

                throw new IllegalStateException(
                        String.format(
                                "All expected attributes %1s could not be retrieved. These attributes where missing %2s",
                                Arrays.toString(attributesToCheck),
                                missing)
                );
            }
            for (Attribute attribute : attributes.asList()) {
                if (!verifyAttribute(attribute.getName(), attribute.getValue())) {
                    HealthStatus healthStatus = new HealthStatus(
                            getHealthCheckId(),
                            SeverityLevel.ERROR,
                            String.format(
                                    "Attribute '%1s' with value '%2s' did not match expected value '%3s'",
                                    attribute.getName(),
                                    attribute.getValue(),
                                    getExpectedAttributeValue(attribute.getName())),
                            objectInstance.getObjectName().getCanonicalName(),
                            0);
                    healthStatuses.add(healthStatus);
                }
            }
        }
    }

    @Override
    public void check(List<HealthStatus> healthStatuses, MBeanServer mBeanServer) throws Exception {
        String mbeanQuery = getObjectNamePattern();
        Set<ObjectInstance> foundMBeans = Collections.EMPTY_SET;
        try {
            foundMBeans = mBeanServer.queryMBeans(new ObjectName(mbeanQuery), null);
        } catch (MalformedObjectNameException e) {
            healthStatuses.add(new HealthStatus(getHealthCheckId(),
                    SeverityLevel.WARNING,
                    "Configuration error invalid MBean objectName " + mbeanQuery,
                    mbeanQuery,
                    0));
        }

        if (foundMBeans.size() == 0) {
            HealthStatus healthStatus = new HealthStatus(
                    getHealthCheckId(),
                    SeverityLevel.ERROR,
                    "No MBeans matching objectName pattern " + mbeanQuery + " found",
                    mbeanQuery,
                    0);
            healthStatuses.add(healthStatus);
        }

        if (hasAttributeChecks()) {
            checkAttributes(healthStatuses, foundMBeans, mBeanServer);
        }

        checkAdditional(healthStatuses, foundMBeans, mBeanServer);
    }

    protected void checkAdditional(List<HealthStatus> healthStatuses, Set<ObjectInstance> foundMBeans, MBeanServer mBeanServer) throws Exception {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JmxHealthCheck that = (JmxHealthCheck) o;

        if (!expectedAttributes.equals(that.expectedAttributes)) return false;
        if (!healthCheckId.equals(that.healthCheckId)) return false;
        if (!objectNamePattern.equals(that.objectNamePattern)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = healthCheckId.hashCode();
        result = 31 * result + objectNamePattern.hashCode();
        result = 31 * result + expectedAttributes.hashCode();
        return result;
    }
}
