package se.inera.axel.monitoring.jmx;

import se.inera.axel.monitoring.HealthStatus;

import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class HealthStatusOpenTypeFactory extends AbstractOpenTypeFactory {
    public HealthStatusOpenTypeFactory() {
        addField("healthId", "health check id", SimpleType.STRING);
        addField("level", "severity level", SimpleType.STRING);
        addField("message", "A textual description of the health check status", SimpleType.STRING);
        addField("resource", "resource", SimpleType.STRING);
        addField("healthPercent", "healthPercent", SimpleType.DOUBLE);
    }

    @Override
    protected String getTypeName() {
        return HealthStatus.class.getSimpleName();
    }

    @Override
    public Map<String, Object> getFields(Object o) {
        HealthStatus healthStatus = (HealthStatus)o;
        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("healthId", healthStatus.getHealthId());
        fieldsMap.put("level", healthStatus.getLevel().name());
        fieldsMap.put("message", healthStatus.getMessage());
        fieldsMap.put("resource", healthStatus.getResource());
        fieldsMap.put("healthPercent", healthStatus.getHealthPercent());
        return fieldsMap;
    }
}
