/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.riv.itintegration.monitoring.impl;

import se.inera.axel.monitoring.HealthStatus;
import se.inera.axel.monitoring.HealthView;
import se.inera.axel.monitoring.SeverityLevel;
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface;
import se.riv.itintegration.monitoring.v1.ConfigurationType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.management.MBeanServer;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@WebService(endpointInterface = "se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface")
public class PingForConfigurationResponder implements PingForConfigurationResponderInterface {
    private final String mavenVersion;
    private String axelHealthViewMBeanName = "se.inera.axel:name=shs-broker,service=Health,type=HealthView";
    private MBeanServer mBeanServer;
    private HealthView healthView;
    private String groupId = "se.inera.axel";
    private String artifactId = "itintegration-monitoring-service";
    private static String POM_PROPERTIES_PATH = "/META-INF/maven/%1$s/%2$s/pom.properties";

    public PingForConfigurationResponder(MBeanServer mBeanServer, HealthView healthView) {
        this.mBeanServer = mBeanServer;
        this.healthView = healthView;
        this.mavenVersion = getMavenVersion();
    }

    @Override
    public PingForConfigurationResponseType pingForConfiguration(
            @WebParam(partName = "LogicalAddress", name = "LogicalAddress", targetNamespace = "urn:riv:itintegration:registry:1", header = true)
            String logicalAddress,
            @WebParam(partName = "parameters", name = "PingForConfiguration", targetNamespace = "urn:riv:itintegration:monitoring:PingForConfigurationResponder:1")
            PingForConfigurationType parameters) {
        if (logicalAddress == null) {
            throw new RuntimeException("Testing exception handling: No ws-addressing 'To'-address found in message");
        }


        if ("1111111111".equalsIgnoreCase(logicalAddress)) {
            throw new RuntimeException("Testing exception handling: illegal 'To'-address: " + logicalAddress);
        }

        if (parameters == null || parameters.getServiceContractNamespace() == null) {
            throw new RuntimeException("Testing soap fault. No service namespace specified.");
        }

        PingForConfigurationResponseType response = new PingForConfigurationResponseType();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        response.setVersion(this.mavenVersion);
        response.setPingDateTime(format.format(new Date()));

        checkAxelHealth();
        appendOperatingSystemData(response);
        appendMemoryUsageInformation(response);
        appendSystemProperties(response);
        appendEnvironmentVariables(response);

        return response;
    }

    private void appendSystemProperties(PingForConfigurationResponseType response) {
        Properties systemProperties = System.getProperties();
        for (Map.Entry<Object, Object> entry : systemProperties.entrySet()) {
            addConfigurationEntry(response, entry.getKey().toString(), entry.getValue().toString());
        }
    }

    private void appendEnvironmentVariables(PingForConfigurationResponseType response) {
        Map<String, String> environmentVariables = System.getenv();
        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
            addConfigurationEntry(response, entry.getKey(), entry.getValue());
        }
    }

    private void appendMemoryUsageInformation(PingForConfigurationResponseType response) {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        addConfigurationEntry(response, "Heap memory usage", String.valueOf(memoryMXBean.getHeapMemoryUsage().getUsed()));
        addConfigurationEntry(response, "Max heap memory", String.valueOf(memoryMXBean.getHeapMemoryUsage().getMax()));
        addConfigurationEntry(response, "Used non heap memory usage", String.valueOf(memoryMXBean.getNonHeapMemoryUsage().getUsed()));
        addConfigurationEntry(response, "Max non heap memory usage", String.valueOf(memoryMXBean.getNonHeapMemoryUsage().getMax()));
    }

    private void appendOperatingSystemData(PingForConfigurationResponseType response) {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        addConfigurationEntry(response, "Operating system", operatingSystemMXBean.getName());
        addConfigurationEntry(response, "Architecture", operatingSystemMXBean.getArch());
        addConfigurationEntry(response, "Available processors", String.valueOf(operatingSystemMXBean.getAvailableProcessors()));
        addConfigurationEntry(response, "System load average", String.format("%.2f", operatingSystemMXBean.getSystemLoadAverage()));
    }

    protected void checkAxelHealth() {
        List<HealthStatus> healthList = Collections.emptyList();
        try {
            healthList = healthView.healthList();
        } catch (Exception e) {
            throw new HealthProblemException("Could not retrieve health list", healthList);
        }
        for (HealthStatus healthStatus : healthList) {
            if (EnumSet.of(SeverityLevel.ERROR, SeverityLevel.CRITICAL).contains(healthStatus.getLevel())) {
                throw new HealthProblemException("Axel is unavailable: "
                        + healthStatus.getHealthId()
                        + ": " + healthStatus.getMessage()
                        + ", resource " + healthStatus.getResource(),
                        healthList);

            }
        }
    }

    private void addConfigurationEntry(PingForConfigurationResponseType response, String name, String value) {
        ConfigurationType configurationEntry = new ConfigurationType();
        configurationEntry.setName(name);
        configurationEntry.setValue(value);
        response.getConfiguration().add(configurationEntry);
    }

    private String getMavenVersion() {
        Properties properties = loadProperties(getClass().getResourceAsStream(String.format(POM_PROPERTIES_PATH, groupId, artifactId)));
        if (properties == null) {
            return "unknown";
        } else {
            return properties.getProperty("version");
        }
    }

    private Properties loadProperties(InputStream inputStream) {
        if (inputStream != null) {
            Properties properties = new Properties();
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                return null;
            }
            return properties;
        }
        return null;
    }
}

