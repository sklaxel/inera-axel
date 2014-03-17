package se.inera.axel.monitoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import se.inera.axel.monitoring.jmx.OpenTypeFactory;
import se.inera.axel.monitoring.jmx.OpenTypeHelper;

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
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import java.lang.management.ManagementFactory;
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
@ManagedResource(objectName = "se.inera.axel:type=ShsNode,service=Health")
public class AxelHealthView implements HealthView {
    private final List<HealthCheck> healthChecks;
    private final MBeanServer mBeanServer;

    @Autowired
    public AxelHealthView(MBeanServer mBeanServer, List<HealthCheck> healthChecks) {
        this.healthChecks = healthChecks;
        this.mBeanServer = mBeanServer;
    }

    @Override
    @ManagedOperation
    public TabularData health() throws Exception {
        String[] itemNames = new String[] {"healthId", "level", "message", "resource", "healthPercent"};

        OpenTypeFactory openTypeFactory = OpenTypeHelper.getFactory(HealthStatus.class);
        CompositeType healthStatusRowType = openTypeFactory.getCompositeType();
        TabularType tabularType = new TabularType(
                "HealthStatus",
                "HealthStatus",
                healthStatusRowType,
                itemNames);
        TabularData result = new TabularDataSupport(tabularType);
        List<HealthStatus> healthList = healthList();
        for (HealthStatus status : healthList) {
            result.put(new CompositeDataSupport(healthStatusRowType, openTypeFactory.getFields(status)));
        }

        return result;
    }

    @Override
    @ManagedOperation
    public List<HealthStatus> healthList() throws Exception {
        List<HealthStatus> healthStatuses = new ArrayList<>();

        for (HealthCheck healthCheck: healthChecks) {
            String mbeanQuery = healthCheck.getObjectNamePattern();
            Set<ObjectInstance> foundMBeans = Collections.EMPTY_SET;
            try {
                foundMBeans = mBeanServer.queryMBeans(new ObjectName(mbeanQuery), null);
            } catch (MalformedObjectNameException e) {
                healthStatuses.add(new HealthStatus(healthCheck.getHealthCheckId(),
                        SeverityLevel.WARNING,
                        "Configuration error invalid MBean objectName " + mbeanQuery,
                        mbeanQuery,
                        0));
            }

            if (foundMBeans.size() == 0) {
                HealthStatus healthStatus = new HealthStatus(
                        healthCheck.getHealthCheckId(),
                        SeverityLevel.ERROR,
                        "No MBeans matching objectName pattern " + mbeanQuery + " found",
                        mbeanQuery,
                        0);
                healthStatuses.add(healthStatus);
            }

            if (healthCheck.hasAttributeChecks()) {
                checkAttributes(healthStatuses, healthCheck, foundMBeans);
            }
        }

        return healthStatuses;
    }

    protected void checkAttributes(List<HealthStatus> healthStatuses, HealthCheck healthCheck, Set<ObjectInstance> foundMBeans) throws InstanceNotFoundException, ReflectionException, MBeanException, AttributeNotFoundException {
        for (ObjectInstance objectInstance : foundMBeans) {
            AttributeList attributes = mBeanServer.getAttributes(objectInstance.getObjectName(), healthCheck.getAttributesToCheck());
            String[] attributesToCheck = healthCheck.getAttributesToCheck();
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
                if (!healthCheck.verifyAttribute(attribute.getName(), attribute.getValue())) {
                    HealthStatus healthStatus = new HealthStatus(
                            healthCheck.getHealthCheckId(),
                            SeverityLevel.ERROR,
                            String.format(
                                    "Attribute '%1s' with value '%2s' did not match expected value '%3s'",
                                    attribute.getName(),
                                    attribute.getValue(),
                                    healthCheck.getExpectedAttributeValue(attribute.getName())),
                            objectInstance.getObjectName().getCanonicalName(),
                            0);
                    healthStatuses.add(healthStatus);
                }
            }
        }
    }
}
