package se.inera.axel.monitoring.jmx;

import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import java.util.Map;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public interface OpenTypeFactory {
    CompositeType getCompositeType() throws OpenDataException;
    Map<String, Object> getFields(Object o);
}
