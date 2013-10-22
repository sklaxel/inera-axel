package se.inera.axel.shs.broker.directory.internal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Holds the configuration information for a LDAP server.
 */
public class LdapServerConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapServerConfiguration.class);

    private String url;
    private String userDn;
    private String password;
    private int initSize;
    private int maxSize;
    private int preferredSize;
    private int timeout;
    private static Set<String> fallbackProperties = new HashSet(Arrays.asList(
            "connect.pool.initsize",
            "connect.pool.maxsize",
            "connect.pool.prefsize",
            "connect.pool.timeout"));
    private Map<String, String> baseEnvironmentProperties = new HashMap<>();


    /**
     * Extracts a list of LDAP server configurations from the properties.
     *
     * <p>The properties should be named as shs.ldap.x.propertyName where x is the number of the
     * LDAP server. For the first server x can be omitted. For example shs.ldap.url. The first server
     * has index 1. All indices have to be present, if an index is not available the configuration properties
     * with higher indices are ignored.
     *
     * @param properties a properties object with the LDAP server properties.
     *
     * @return a list of LDAP server configurations.
     *
     * @throws IllegalArgumentException if no LDAP server configurations are found.
     */
    public static List<LdapServerConfiguration> extractConfigurations(Properties properties, String propertyPrefix) {
        int noOfServers = getNumberOfLdapServers(properties, propertyPrefix);

        List<LdapServerConfiguration> ldapServerConfigurations = new ArrayList<>(noOfServers);

        for (int i = 1; i <= noOfServers; i++) {
            LdapServerConfiguration ldapServerConfiguration = new LdapServerConfiguration();
            ldapServerConfiguration.url = getProperty(properties, propertyPrefix, i, "url");
            ldapServerConfiguration.userDn = getProperty(properties, propertyPrefix, i, "userDn");
            ldapServerConfiguration.password = getProperty(properties, propertyPrefix, i, "password");
            ldapServerConfiguration.initSize = toInt(getProperty(properties, propertyPrefix, i, "connect.pool.initsize"), 1);
            ldapServerConfiguration.maxSize = toInt(getProperty(properties, propertyPrefix, i, "connect.pool.maxsize"), 5);
            ldapServerConfiguration.preferredSize = toInt(getProperty(properties, propertyPrefix, i, "connect.pool.prefsize"), 2);
            ldapServerConfiguration.timeout = toInt(getProperty(properties, propertyPrefix, i, "connect.pool.timeout"), 300000);
            ldapServerConfiguration.getBaseEnvironmentProperties().putAll(getBaseEnvironmentProperties(properties, propertyPrefix, i));

            ldapServerConfigurations.add(ldapServerConfiguration);
        }
        return ldapServerConfigurations;
    }

    private static Map<String, String> getBaseEnvironmentProperties(Properties properties, String propertyPrefix, int serverIndex) {
        Map<String, String> baseEnvironmentProperties = new HashMap<>();

        Set<String> propertyNames = new HashSet<>();

        for (String propertyName : properties.stringPropertyNames()) {
            int index = propertyName.indexOf("baseEnvironmentProperties.");

            if (index > 0) {
                propertyNames.add(propertyName.substring(index));
            }
        }

        for (String baseEnvironmentProperty : propertyNames) {
            String propertyValue = getProperty(properties, propertyPrefix, serverIndex, baseEnvironmentProperty);
            baseEnvironmentProperties.put(baseEnvironmentProperty.substring("baseEnvironmentProperties.".length()), propertyValue);
        }

        return baseEnvironmentProperties;
    }

    private static int toInt(String propertyValue, int defaultValue) {
        int result = defaultValue;
        try {
            result = Integer.parseInt(propertyValue);
        } catch (NumberFormatException e) {
            LOGGER.warn("Failed to convert property value {} to int returning default value {}", propertyValue, defaultValue);
        }

        return result;
    }

    /**
     * Gets the property value.
     *
     * <p>If the property is a fallback property and the property does not have a value for the current server index
     * the default value is returned. For server index 1 the default value is returned for all properties if a value
     * with index 1 is not configured.</p>
     *
     * @param properties
     * @param propertyPrefix
     * @param i
     * @param propertyName
     * @return
     */
    private static String getProperty(Properties properties, String propertyPrefix, int i, String propertyName) {
        String propertyValue = properties.getProperty(getPropertyKey(propertyPrefix, i, propertyName));

        if (propertyValue == null
                && (i == 1
                    || fallbackProperties.contains(propertyName)
                    || propertyName.startsWith("baseEnvironmentProperties")))
        {
            propertyValue = properties.getProperty(getDefaultPropertyKey(propertyPrefix, propertyName));
            if (propertyValue == null
                    && (!"shs.ldap".equals(propertyPrefix)
                        && fallbackProperties.contains(propertyName))
                        || propertyName.startsWith("baseEnvironmentProperties"))
            {
                propertyValue = properties.getProperty(getDefaultPropertyKey("shs.ldap", propertyName));
            }
        }

        return propertyValue;
    }

    private static String getPropertyKey(String propertyPrefix, int serverNumber, String propertyName) {
        String propertyKey = String.format("%s.%d.%s", propertyPrefix, serverNumber, propertyName);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Returning {} from getPropertyKey", propertyKey);
        }
        return propertyKey;
    }

    private static String getDefaultPropertyKey(String propertyPrefix, String propertyName) {
        String propertyKey = String.format("%s.%s", propertyPrefix, propertyName);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Returning {} from getDefaultPropertyKey", propertyKey);
        }
        return propertyKey;
    }

    private static int getNumberOfLdapServers(Properties properties, String propertyPrefix) {
        if (!(properties.containsKey(getDefaultPropertyKey(propertyPrefix, "url"))
                || properties.containsKey(getPropertyKey(propertyPrefix, 1, "url")))) {
            // At least one server has to be configured
            throw new IllegalArgumentException("At least one LDAP server has to be configured. " +
                    "Could not find property shs.ldap.url or shs.ldap.1.url");
        }

        int noOfServers = 1;

        while (properties.containsKey(getPropertyKey(propertyPrefix, noOfServers + 1, "url"))) {
            noOfServers++;
        }

        return noOfServers;
    }

    public String getUrl() {
        return url;
    }

    public String getUserDn() {
        return userDn;
    }

    public String getPassword() {
        return password;
    }

    public int getInitSize() {
        return initSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getPreferredSize() {
        return preferredSize;
    }

    public int getTimeout() {
        return timeout;
    }

    public Map<String, String> getBaseEnvironmentProperties() {
        return baseEnvironmentProperties;
    }
}
