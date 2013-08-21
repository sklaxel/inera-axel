package se.inera.axel.shs.broker.directory.internal;

import org.apache.commons.lang.StringUtils;
import org.springframework.ldap.core.simple.SimpleLdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.pool.factory.PoolingContextSource;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.DirectoryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class DirectoryServiceFactory {
    /**
     * Utility class should not be instantiated
     */
    private DirectoryServiceFactory() {

    }

    public static List<DirectoryService> getDirectoryServices(Properties properties) throws Exception {
        List<LdapServerConfiguration> ldapServerConfigurations = LdapServerConfiguration.extractConfigurations(properties, "shs.ldap");

        return getDirectoryServices(ldapServerConfigurations);
    }

    public static List<DirectoryService> getDirectoryServices(List<LdapServerConfiguration> ldapServerConfigurations) throws Exception {
        List<DirectoryService> directoryServices = new ArrayList<DirectoryService>(ldapServerConfigurations.size());
        for(LdapServerConfiguration ldapServerConfiguration : ldapServerConfigurations) {
            DirectoryService directoryService = createDirectoryService(ldapServerConfiguration);
            directoryServices.add(directoryService);
        }

        return directoryServices;
    }

    public static List<DirectoryAdminService> getDirectoryAdminServices(Properties properties) throws Exception {
        List<LdapServerConfiguration> ldapServerConfigurations = LdapServerConfiguration.extractConfigurations(properties, "shs.ldap.admin");

        return getDirectoryAdminServices(ldapServerConfigurations);
    }

    public static List<DirectoryAdminService> getDirectoryAdminServices(List<LdapServerConfiguration> ldapServerConfigurations) throws Exception {
        List<DirectoryAdminService> directoryServices = new ArrayList<DirectoryAdminService>(ldapServerConfigurations.size());
        for(LdapServerConfiguration ldapServerConfiguration : ldapServerConfigurations) {
            DirectoryAdminService directoryService = createDirectoryAdminService(ldapServerConfiguration);
            directoryServices.add(directoryService);
        }

        return directoryServices;
    }

    private static DirectoryService createDirectoryService(LdapServerConfiguration ldapServerConfiguration) throws Exception {
        SimpleLdapTemplate ldapTemplate = createSimpleLdapTemplate(ldapServerConfiguration);
        LdapDirectoryService directoryService = new LdapDirectoryService();
        directoryService.setLdapTemplate(ldapTemplate);

        return directoryService;
    }

    private static SimpleLdapTemplate createSimpleLdapTemplate(LdapServerConfiguration ldapServerConfiguration) throws Exception {
        LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl(ldapServerConfiguration.getUrl());
        ldapContextSource.setUserDn(StringUtils.isBlank(ldapServerConfiguration.getUserDn()) ? "" : ldapServerConfiguration.getUserDn());
        ldapContextSource.setPassword(StringUtils.isBlank(ldapServerConfiguration.getPassword()) ? "" : ldapServerConfiguration.getPassword());
        ldapContextSource.setPooled(false);
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
        LdapDirectoryAdminService directoryService = new LdapDirectoryAdminService();
        directoryService.setLdapTemplate(ldapTemplate);

        return directoryService;
    }
}
