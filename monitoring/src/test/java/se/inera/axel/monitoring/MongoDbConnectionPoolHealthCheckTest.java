package se.inera.axel.monitoring;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class MongoDbConnectionPoolHealthCheckTest extends AbstractHealthCheckTest {

    @BeforeMethod
    public void setUp() throws Exception {
        mBeanServer = mock(MBeanServer.class);

    }

    @Test
    public void whenNoConnectionIsInUseAHealthStatusShouldNotBeReported() throws Exception {
        queryMBeansResult(new ObjectInstance("se.inera.axel:type=Test", "java.lang.Object"));
        MongoDbConnectionPoolHealthCheck healthCheck =
                new MongoDbConnectionPoolHealthCheck("se.inera.axel.test", "se.inera.axel:type=Test", null);

        List<HealthStatus> healthStatuses = Collections.emptyList();
        healthCheck.check(healthStatuses, mBeanServer);

        assertThat(healthStatuses, is(empty()));
    }

    @Test
    public void allConnectionsInUseShouldTriggerWarning() throws Exception {
        queryMBeansResult(new ObjectInstance("se.inera.axel:type=Test", "java.lang.Object"));
        MongoDbConnectionPoolHealthCheck healthCheck =
                new MongoDbConnectionPoolHealthCheck("se.inera.axel.test", "se.inera.axel:type=Test", null);

        List<HealthStatus> healthStatuses = Collections.emptyList();
        healthCheck.check(healthStatuses, mBeanServer);

        assertThat(healthStatuses, is(empty()));
    }
}
