package se.inera.axel.shs.broker.directory.internal.config;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.ldap.core.simple.SimpleLdapTemplate;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.pool.factory.PoolingContextSource;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.DirectoryService;
import se.inera.axel.shs.broker.directory.internal.DefaultDirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.directory.internal.LdapDirectoryAdminService;
import se.inera.axel.shs.broker.directory.internal.LdapDirectoryService;
import se.inera.axel.shs.broker.directory.internal.config.LdapServerConfiguration;
import se.inera.axel.shs.broker.directory.internal.config.ThreadContextClassLoaderDirectoryAdminServiceWrapper;

import java.util.*;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class DirectoryServiceFactory implements BeanFactoryPostProcessor, ApplicationContextAware {
    /**
     * Utility class should not be instantiated
     */
    private DirectoryServiceFactory() {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        applicationContext.getEnvironment().getProperty("");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
      // beanFactory.get
    }

    public static List<DirectoryService> getDirectoryServices(Properties properties) throws Exception {
        List<LdapServerConfiguration> ldapServerConfigurations = LdapServerConfiguration.extractConfigurations(properties, "shs.ldap");

        return getDirectoryServices(ldapServerConfigurations);
    }

    public static List<DirectoryService> getDirectoryServices(List<LdapServerConfiguration> ldapServerConfigurations) throws Exception {
        List<DirectoryService> directoryServices = new ArrayList<DirectoryService>(ldapServerConfigurations.size());
        for(LdapServerConfiguration ldapServerConfiguration : ldapServerConfigurations) {
            DirectoryService directoryService = createDirectoryService(ldapServerConfiguration);
            if (directoryService != null) {
                directoryServices.add(directoryService);
            }
        }

        return directoryServices;
    }

    public static DefaultDirectoryAdminServiceRegistry createDirectoryAdminServiceRegistry(Properties properties) throws Exception {
        List<LdapServerConfiguration> ldapServerConfigurations = LdapServerConfiguration.extractConfigurations(properties, "shs.ldap.admin");

        return createDirectoryAdminServiceRegistry(ldapServerConfigurations);
    }

    public static DefaultDirectoryAdminServiceRegistry createDirectoryAdminServiceRegistry(List<LdapServerConfiguration> ldapServerConfigurations) throws Exception {
        Map<String, DirectoryAdminService> directoryServices = new LinkedHashMap<String, DirectoryAdminService>();
        for(LdapServerConfiguration ldapServerConfiguration : ldapServerConfigurations) {
            DirectoryAdminService directoryService = createDirectoryAdminService(ldapServerConfiguration);
            if (directoryService != null) {
                // Wrap so that Thread Context Classloader is correct in an osgi environment
                directoryServices.put(ldapServerConfiguration.getUrl(),
                        new ThreadContextClassLoaderDirectoryAdminServiceWrapper(directoryService));
            }
        }

        DefaultDirectoryAdminServiceRegistry directoryAdminServiceRegistry = new DefaultDirectoryAdminServiceRegistry(directoryServices);

        return directoryAdminServiceRegistry;
    }

    private static DirectoryService createDirectoryService(LdapServerConfiguration ldapServerConfiguration) throws Exception {
        SimpleLdapTemplate ldapTemplate = createSimpleLdapTemplate(ldapServerConfiguration);
        if (ldapTemplate == null) {
            return null;
        }
        LdapDirectoryService directoryService = new LdapDirectoryService();
        directoryService.setLdapTemplate(ldapTemplate);

        return directoryService;
    }

    private static SimpleLdapTemplate createSimpleLdapTemplate(LdapServerConfiguration ldapServerConfiguration) throws Exception {

        if (StringUtils.isEmpty(ldapServerConfiguration.getUrl())) {
            return null;
        }

        LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl(ldapServerConfiguration.getUrl());
        ldapContextSource.setUserDn(StringUtils.isBlank(ldapServerConfiguration.getUserDn()) ? "" : ldapServerConfiguration.getUserDn());
        ldapContextSource.setPassword(StringUtils.isBlank(ldapServerConfiguration.getPassword()) ? "" : ldapServerConfiguration.getPassword());
        ldapContextSource.setPooled(false);
        ldapContextSource.setDirObjectFactory(DefaultDirObjectFactory.class);
        ldapContextSource.setBaseEnvironmentProperties(ldapServerConfiguration.getBaseEnvironmentProperties());
        ldapContextSource.afterPropertiesSet();

        PoolingContextSource poolingContextSource = new PoolingContextSource();
        poolingContextSource.setContextSource(ldapContextSource);
        poolingContextSource.setMinIdle(ldapServerConfiguration.getInitSize());
        poolingContextSource.setMaxTotal(ldapServerConfiguration.getMaxSize());
        poolingContextSource.setMaxActive(ldapServerConfiguration.getPreferredSize());
        poolingContextSource.setMinEvictableIdleTimeMillis(ldapServerConfiguration.getTimeout());

        return new SimpleLdapTemplate(poolingContextSource);
    }

    private static DirectoryAdminService createDirectoryAdminService(LdapServerConfiguration ldapServerConfiguration) throws Exception {
        SimpleLdapTemplate ldapTemplate = createSimpleLdapTemplate(ldapServerConfiguration);
        if (ldapTemplate == null) {
            return null;
        }
        LdapDirectoryAdminService directoryService = new LdapDirectoryAdminService();
        directoryService.setLdapTemplate(ldapTemplate);

        return directoryService;
    }
}
