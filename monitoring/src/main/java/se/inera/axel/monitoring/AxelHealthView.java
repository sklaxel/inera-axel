package se.inera.axel.monitoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;
import se.inera.axel.monitoring.jmx.OpenTypeFactory;
import se.inera.axel.monitoring.jmx.OpenTypeHelper;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
@ManagedResource
public class AxelHealthView implements HealthView, SelfNaming {
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
            healthCheck.check(healthStatuses, mBeanServer);
        }

        return healthStatuses;
    }

    @Override
    public ObjectName getObjectName() throws MalformedObjectNameException {
        return ObjectName.getInstance("se.inera.axel:name=axel,service=Health,type=HealthView");
    }
}
