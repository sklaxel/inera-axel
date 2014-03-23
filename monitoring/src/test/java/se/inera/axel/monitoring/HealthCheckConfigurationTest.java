package se.inera.axel.monitoring;

import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class HealthCheckConfigurationTest {
    @Test
    public void marshalHealthChecks() throws Exception {
        Map<String, String> expectedAttributes = new HashMap<>();
        expectedAttributes.put("attribute1", "attributeValue");
        JmxHealthCheck healthCheck = new JmxHealthCheck("test1", "test,type=Test", expectedAttributes);
        JmxHealthCheck healthCheck2 = new JmxHealthCheck("test2", "test,type=Test", null);

        List<HealthCheck> healthChecks = Arrays.<HealthCheck>asList(healthCheck, healthCheck2);
        Yaml yaml = new Yaml();
        yaml.setBeanAccess(BeanAccess.FIELD);
        String dump = yaml.dump(healthChecks);
        System.out.println(dump);
        List loadedHealthChecks = (List)yaml.load(dump);
        assertThat((JmxHealthCheck)loadedHealthChecks.get(0), is(equalTo(healthCheck)));
        assertThat((JmxHealthCheck)loadedHealthChecks.get(1), is(equalTo(healthCheck2)));
    }

    @Test
    public void unmarshalJmxHealthCheck() throws Exception {
        String healthCheckConfiguration =
                "- !!se.inera.axel.monitoring.JmxHealthCheck" + System.lineSeparator()
                + "  healthCheckId: testid" + System.lineSeparator()
                + "  objectNamePattern: testObjectPattern" + System.lineSeparator()
                + "  expectedAttributes: { testAttribute: attributeValue }";

        Yaml yaml = new Yaml();
        yaml.setBeanAccess(BeanAccess.FIELD);
        Object loadedHealthChecks = yaml.load(healthCheckConfiguration);

        assertThat(loadedHealthChecks, instanceOf(List.class));

        List<HealthCheck> healthChecks = (List) loadedHealthChecks;

        Map<String, String> expectedAttributes = new HashMap<>();
        expectedAttributes.put("testAttribute", "attributeValue");
        HealthCheck expectedHealthCheck = new JmxHealthCheck("testid", "testObjectPattern", expectedAttributes);

        assertThat(healthChecks, contains(expectedHealthCheck));
    }
}
