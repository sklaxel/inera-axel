package se.inera.axel.monitoring;

import org.hamcrest.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.collections.Sets.newHashSet;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class AxelHealthViewTest {

    private MBeanServer mBeanServer;

    @BeforeMethod
    public void setUp() throws Exception {
        mBeanServer = mock(MBeanServer.class);

    }

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

    @Test
    public void allChecksSuccessfulShouldReturnEmptyList() throws Exception {
        queryMBeansResult(new ObjectInstance("se.inera.axel:type=Test", "java.lang.Object"));
        List<HealthCheck> healthChecks = new ArrayList<>();
        healthChecks.add(new HealthCheck("se.inera.axel.mbean", "se.inera.axel:type=Test", null));
        AxelHealthView axelHealthView = new AxelHealthView(mBeanServer, healthChecks);
        List<HealthStatus> result = axelHealthView.healthList();
        assertThat(result, is(empty()));
    }

    @Test
    public void errorHealthStatusShouldBeReturnedWhenMBeanIsNotFound() throws Exception {
        emptyQueryMBeansResult();
        List<HealthCheck> healthChecks = new ArrayList<>();
        healthChecks.add(new HealthCheck("se.inera.axel.mbean", "se.inera.axel:type=Test", null));
        AxelHealthView axelHealthView = new AxelHealthView(mBeanServer, healthChecks);
        List<HealthStatus> result = axelHealthView.healthList();
        Matcher<HealthStatus> withErrorLevel = hasProperty("level", is(SeverityLevel.ERROR));
        assertThat(result, allOf(Matchers.<HealthStatus>iterableWithSize(1), hasItem(withErrorLevel)));
    }

    @Test
    public void healthCheckShouldBeSuccessfulWhenTheMBeanAttributeMatches() throws Exception {
        returnAttributes(new Attribute("testAttribute", "expectedValue"));
        queryMBeansResult(new ObjectInstance("se.inera.axel:type=Test", "java.lang.Object"));

        List<HealthCheck> healthChecks = new ArrayList<>();
        Map<String, String> expectedAttributes = new HashMap<>();
        expectedAttributes.put("testAttribute", "expectedValue");
        HealthCheck healthCheck = new HealthCheck("se.inera.axel.mbean", "se.inera.axel:type=Test", expectedAttributes);
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
        HealthCheck healthCheck = new HealthCheck("se.inera.axel.mbean", "se.inera.axel:type=Test", expectedAttributes);
        healthChecks.add(healthCheck);

        AxelHealthView axelHealthView = new AxelHealthView(mBeanServer, healthChecks);
        List<HealthStatus> result = axelHealthView.healthList();

        Matcher<HealthStatus> withErrorLevel = hasProperty("level", is(SeverityLevel.ERROR));
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
        HealthCheck healthCheck = new HealthCheck("se.inera.axel.mbean", "se.inera.axel:type=Test", expectedAttributes);
        healthChecks.add(healthCheck);

        AxelHealthView axelHealthView = new AxelHealthView(mBeanServer, healthChecks);
        List<HealthStatus> result = axelHealthView.healthList();

        Matcher<HealthStatus> withErrorLevel = hasProperty("level", is(SeverityLevel.ERROR));
        assertThat(result, allOf(Matchers.<HealthStatus>iterableWithSize(1), hasItem(withErrorLevel)));
    }
}
