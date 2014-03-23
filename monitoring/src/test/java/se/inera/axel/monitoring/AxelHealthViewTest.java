package se.inera.axel.monitoring;

import org.hamcrest.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.collections.Sets.newHashSet;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class AxelHealthViewTest extends AbstractHealthCheckTest {

    @BeforeMethod
    public void setUp() throws Exception {
        mBeanServer = mock(MBeanServer.class);

    }

    @Test
    public void allChecksSuccessfulShouldReturnEmptyList() throws Exception {
        queryMBeansResult(new ObjectInstance("se.inera.axel:type=Test", "java.lang.Object"));
        List<HealthCheck> healthChecks = new ArrayList<>();
        healthChecks.add(new JmxHealthCheck("se.inera.axel.mbean", "se.inera.axel:type=Test", null));
        AxelHealthView axelHealthView = new AxelHealthView(mBeanServer, healthChecks);
        List<HealthStatus> result = axelHealthView.healthList();
        assertThat(result, is(empty()));
    }

    @Test
    public void errorHealthStatusShouldBeReturnedWhenMBeanIsNotFound() throws Exception {
        emptyQueryMBeansResult();
        List<HealthCheck> healthChecks = new ArrayList<>();
        healthChecks.add(new JmxHealthCheck("se.inera.axel.mbean", "se.inera.axel:type=Test", null));
        AxelHealthView axelHealthView = new AxelHealthView(mBeanServer, healthChecks);
        List<HealthStatus> result = axelHealthView.healthList();
        Matcher<HealthStatus> withErrorLevel = withSeverityLevel(SeverityLevel.ERROR);
        assertThat(result, allOf(Matchers.<HealthStatus>iterableWithSize(1), hasItem(withErrorLevel)));
    }

    protected Matcher<HealthStatus> withSeverityLevel(SeverityLevel level) {
        return hasProperty("level", is(level));
    }

    @Test
    public void healthCheckShouldBeSuccessfulWhenTheMBeanAttributeMatches() throws Exception {
        returnAttributes(new Attribute("testAttribute", "expectedValue"));
        queryMBeansResult(new ObjectInstance("se.inera.axel:type=Test", "java.lang.Object"));

        List<HealthCheck> healthChecks = new ArrayList<>();
        Map<String, String> expectedAttributes = new HashMap<>();
        expectedAttributes.put("testAttribute", "expectedValue");
        HealthCheck healthCheck = new JmxHealthCheck("se.inera.axel.mbean", "se.inera.axel:type=Test", expectedAttributes);
        healthChecks.add(healthCheck);

        AxelHealthView axelHealthView = new AxelHealthView(mBeanServer, healthChecks);
        List<HealthStatus> result = axelHealthView.healthList();
        assertThat(result, is(empty()));
    }

    @Test
    public void incorrectAttributeValueShouldCauseError() throws Exception {
        returnAttributes(new Attribute("testAttribute", "incorrectValue"));
        queryMBeansResult(new ObjectInstance("se.inera.axel:type=Test", "java.lang.Object"));
        List<HealthCheck> healthChecks = new ArrayList<>();
        Map<String, String> expectedAttributes = new HashMap<>();
        expectedAttributes.put("testAttribute", "expectedValue");
        HealthCheck healthCheck = new JmxHealthCheck("se.inera.axel.mbean", "se.inera.axel:type=Test", expectedAttributes);
        healthChecks.add(healthCheck);

        AxelHealthView axelHealthView = new AxelHealthView(mBeanServer, healthChecks);
        List<HealthStatus> result = axelHealthView.healthList();

        Matcher<HealthStatus> withErrorLevel = withSeverityLevel(SeverityLevel.ERROR);
        assertThat(result, allOf(Matchers.<HealthStatus>iterableWithSize(1), hasItem(withErrorLevel)));
    }

    @Test(expectedExceptions = AttributeNotFoundException.class)
    public void missingAttributeShouldCauseError() throws Exception {
        returnAttributes();
        when(mBeanServer.getAttribute(any(ObjectName.class), anyString())).thenThrow(AttributeNotFoundException.class);
        queryMBeansResult(new ObjectInstance("se.inera.axel:type=Test", "java.lang.Object"));
        List<HealthCheck> healthChecks = new ArrayList<>();
        Map<String, String> expectedAttributes = new HashMap<>();
        expectedAttributes.put("testAttribute", "expectedValue");
        HealthCheck healthCheck = new JmxHealthCheck("se.inera.axel.mbean", "se.inera.axel:type=Test", expectedAttributes);
        healthChecks.add(healthCheck);

        AxelHealthView axelHealthView = new AxelHealthView(mBeanServer, healthChecks);
        List<HealthStatus> result = axelHealthView.healthList();

        Matcher<HealthStatus> withErrorLevel = withSeverityLevel(SeverityLevel.ERROR);
        assertThat(result, allOf(Matchers.<HealthStatus>iterableWithSize(1), hasItem(withErrorLevel)));
    }
}
