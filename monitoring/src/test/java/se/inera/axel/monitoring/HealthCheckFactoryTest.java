package se.inera.axel.monitoring;

import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class HealthCheckFactoryTest {
    @Test
    public void unmarshalDefaultHealthCheckConfiguration() throws Exception {
        InputStream stream = getClass().getResourceAsStream("/etc/healthChecks.yaml");

        List<HealthCheck> healthChecks = HealthCheckFactory.createHealthChecks(stream);

        assertThat(healthChecks, hasSize(greaterThan(0)));
    }
}
