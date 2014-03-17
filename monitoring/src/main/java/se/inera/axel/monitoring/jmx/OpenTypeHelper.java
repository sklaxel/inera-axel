package se.inera.axel.monitoring.jmx;

import se.inera.axel.monitoring.HealthStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class OpenTypeHelper {
    private static final Map<Class<?>, OpenTypeFactory> OPEN_TYPE_FACTORIES = new HashMap<>();
    private OpenTypeHelper() {
    }

    static {
        OPEN_TYPE_FACTORIES.put(HealthStatus.class, new HealthStatusOpenTypeFactory());
    }

    public static OpenTypeFactory getFactory(Class<?> clazz) {
        return OPEN_TYPE_FACTORIES.get(clazz);
    }
}
