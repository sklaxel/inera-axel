package se.inera.axel.monitoring;

import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class HealthCheckFactory {
    public static List<HealthCheck> createHealthChecks(InputStream healthCheckConfigurationStream) {
        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(HealthCheckFactory.class.getClassLoader()));
        yaml.setBeanAccess(BeanAccess.FIELD);
        Object loadedHealthChecks = yaml.load(healthCheckConfigurationStream);
        List<HealthCheck> healthChecks = new ArrayList<>((java.util.Collection<? extends HealthCheck>) loadedHealthChecks);

        IOUtils.closeQuietly(healthCheckConfigurationStream);

        return healthChecks;
    }
}
