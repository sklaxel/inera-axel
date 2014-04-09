package se.inera.axel.shs.broker.directory;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFReader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.mock.env.MockPropertySource;

import java.io.IOException;

/**
 * Initializes an in memory directory service to use for testing the communication with the LDAP server.
 * <p>
 * Register this class as the initializer in the ContextConfiguration annotation.
 *
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class InMemoryDirectoryServerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>,
        ApplicationListener<ContextClosedEvent> {
    private InMemoryDirectoryServer directoryServer;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
        try {
            propertySources.addLast(new ResourcePropertySource("classpath:ldap-test.properties"));
            propertySources.addLast(new ResourcePropertySource("classpath:META-INF/spring/ldap-default.properties"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        try {
            startDirectoryServer(applicationContext.getEnvironment());
        } catch (LDAPException e) {
            throw new IllegalStateException(e);
        }

        int listenPort = directoryServer.getListenPort();
        MockPropertySource mockEnvVars = new MockPropertySource().withProperty("shs.ldap.url",
                String.format("ldap://localhost:%s/L=SHS", listenPort));
        propertySources.replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, mockEnvVars);

        applicationContext.addApplicationListener(this);
    }

    public void startDirectoryServer(Environment environment) throws LDAPException {
        // Create the configuration to use for the server.
        InMemoryDirectoryServerConfig config =
                new InMemoryDirectoryServerConfig("L=SHS");

        config.addAdditionalBindCredentials(
                environment.getProperty("shs.ldap.admin.userDn"),
                environment.getProperty("shs.ldap.admin.password"));
        config.setSchema(null);

        directoryServer = new InMemoryDirectoryServer(config);

        directoryServer.importFromLDIF(true, new LDIFReader(getClass().getResourceAsStream("/ldap/axel-systemtest.ldif")));
        directoryServer.startListening();
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (directoryServer != null) {
            directoryServer.shutDown(false);
        }
    }
}
