package se.inera.axel.test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class DynamicProperties {
    private static ThreadLocal<Map<String, Object>> dynamicProperties = new ThreadLocal<Map<String, Object>>() {
        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<>();
        }
    };

    public static Map<String, Object> get() {
        return dynamicProperties.get();
    }
}
