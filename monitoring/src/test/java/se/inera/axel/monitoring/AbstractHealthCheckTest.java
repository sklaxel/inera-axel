package se.inera.axel.monitoring;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.testng.collections.Sets.newHashSet;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class AbstractHealthCheckTest {
    protected MBeanServer mBeanServer;

    protected void queryMBeansResult(ObjectInstance... objectInstance) {
        when(mBeanServer.queryMBeans(any(ObjectName.class), any(QueryExp.class)))
                .thenReturn(newHashSet(asList(objectInstance)));
    }

    protected void emptyQueryMBeansResult() {
        when(mBeanServer.queryMBeans(any(ObjectName.class), any(QueryExp.class)))
                .thenReturn(Collections.<ObjectInstance>emptySet());
    }

    protected void returnAttributes(Attribute... attributes) throws InstanceNotFoundException, ReflectionException {
        AttributeList attributeList = new AttributeList();
        for (Attribute attribute : attributes) {
            attributeList.add(attribute);
        }
        when(mBeanServer.getAttributes(any(ObjectName.class), any(String[].class))).thenReturn(attributeList);
    }
}
