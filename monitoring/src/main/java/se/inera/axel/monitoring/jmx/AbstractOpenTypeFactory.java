package se.inera.axel.monitoring.jmx;

import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public abstract class AbstractOpenTypeFactory implements OpenTypeFactory {
    private final List<String> fieldNames = new ArrayList<>();
    private final List<String> fieldDescriptions = new ArrayList<>();
    private final List<SimpleType> fieldTypes = new ArrayList<>();

    public void addField(String name, String description, SimpleType type) {
        fieldNames.add(name);
        fieldDescriptions.add(description);
        fieldTypes.add(type);
    }

    @Override
    public CompositeType getCompositeType() throws OpenDataException {
        String[] itemNames = fieldNames.toArray(new String[fieldNames.size()]);
        String[] itemDescription = fieldDescriptions.toArray(new String[fieldDescriptions.size()]);
        OpenType[] types = fieldTypes.toArray(new OpenType[fieldTypes.size()]);
        return new CompositeType(getTypeName(), getTypeDescription(), itemNames, itemDescription, types);
    }

    protected abstract String getTypeName();

    protected String getTypeDescription() {
        return getTypeName();
    }

    @Override
    public Map<String, Object> getFields(Object o) {
        return new HashMap<>();
    }
}
