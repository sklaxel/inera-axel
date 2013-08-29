package se.inera.axel.shs.broker.directory.internal;

import org.hamcrest.MatcherAssert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.hasSize;
import static org.testng.Assert.*;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class LdapServerConfigurationTest {
    private Properties properties;

    @BeforeMethod
    public void before() {
        properties = new Properties();
    }

    @Test
    public void ldapServer1CanBeConfiguredFromDefaultProperties() {
        addDefaultServerProperties();

        List<LdapServerConfiguration> ldapServerConfigurations
                = LdapServerConfiguration.extractConfigurations(properties, "shs.ldap");

        assertThat(ldapServerConfigurations, hasSize(1));
        assertEquals(ldapServerConfigurations.get(0).getUrl(), properties.getProperty("shs.ldap.url"));
        assertEquals(ldapServerConfigurations.get(0).getUserDn(), properties.getProperty("shs.ldap.userDn"));
        assertEquals(ldapServerConfigurations.get(0).getPassword(), properties.getProperty("shs.ldap.password"));
        assertEquals(String.valueOf(ldapServerConfigurations.get(0).getInitSize()), properties.getProperty("shs.ldap.connect.pool.initsize"));
        assertEquals(String.valueOf(ldapServerConfigurations.get(0).getMaxSize()), properties.getProperty("shs.ldap.connect.pool.maxsize"));
        assertEquals(String.valueOf(ldapServerConfigurations.get(0).getPreferredSize()), properties.getProperty("shs.ldap.connect.pool.prefsize"));
        assertEquals(String.valueOf(ldapServerConfigurations.get(0).getTimeout()), properties.getProperty("shs.ldap.connect.pool.timeout"));
    }

    @Test
    public void ldapServer1CanAlsoUseIndex1() {
        addServerPropertiesWithIndex1();

        List<LdapServerConfiguration> ldapServerConfigurations
                = LdapServerConfiguration.extractConfigurations(properties, "shs.ldap");

        assertThat(ldapServerConfigurations, hasSize(1));

        int serverNumber = 1;
        assertLdapServerConfiguration(ldapServerConfigurations.get(0), serverNumber);
    }

    @Test
    public void testTwoServerConfigurations() {
        addServerPropertiesWithIndex1();
        addServerPropertiesWithIndex2();

        List<LdapServerConfiguration> ldapServerConfigurations
                = LdapServerConfiguration.extractConfigurations(properties, "shs.ldap");

        assertThat(ldapServerConfigurations, hasSize(2));
        assertLdapServerConfiguration(ldapServerConfigurations.get(0), 1);
        assertLdapServerConfiguration(ldapServerConfigurations.get(1), 2);
    }

    @Test
    public void defaultValueShouldBeUsedIfThePropertiesDoNotHaveValuesForTheServer() {
        addDefaultServerProperties();
        // Only configure url for server 2 the rest should be taken from default values
        properties.setProperty("shs.ldap.2.url", "url2");

        List<LdapServerConfiguration> ldapServerConfigurations
                = LdapServerConfiguration.extractConfigurations(properties, "shs.ldap");

        assertThat(ldapServerConfigurations, hasSize(2));
        assertEquals(String.valueOf(ldapServerConfigurations.get(1).getInitSize()), properties.getProperty("shs.ldap.connect.pool.initsize"));
        assertEquals(String.valueOf(ldapServerConfigurations.get(1).getMaxSize()), properties.getProperty("shs.ldap.connect.pool.maxsize"));
        assertEquals(String.valueOf(ldapServerConfigurations.get(1).getPreferredSize()), properties.getProperty("shs.ldap.connect.pool.prefsize"));
        assertEquals(String.valueOf(ldapServerConfigurations.get(1).getTimeout()), properties.getProperty("shs.ldap.connect.pool.timeout"));
    }

    @Test
    public void userDnAndPasswordDoesNotFallbackToDefault() {
        addDefaultServerProperties();
        // Only configure url for server 2 the rest should be taken from default values
        properties.setProperty("shs.ldap.2.url", "url2");

        List<LdapServerConfiguration> ldapServerConfigurations
                = LdapServerConfiguration.extractConfigurations(properties, "shs.ldap");

        assertThat(ldapServerConfigurations, hasSize(2));
        assertNull(ldapServerConfigurations.get(1).getUserDn(), "userDn should not have default value");
        assertNull(ldapServerConfigurations.get(1).getPassword(), "password should not have default value");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void atLeastOneServerHasToBeConfigured() {
        List<LdapServerConfiguration> ldapServerConfigurations
                = LdapServerConfiguration.extractConfigurations(properties, "shs.ldap");
    }

    @Test
    public void serverIndexHaveToBeConsecutive() {
        addServerPropertiesWithIndex1();
        properties.setProperty("shs.ldap.3.url", "url3");

        List<LdapServerConfiguration> ldapServerConfigurations
                = LdapServerConfiguration.extractConfigurations(properties, "shs.ldap");

        assertThat("Only server 1 should be configured since the server index 2 was skipped", ldapServerConfigurations, hasSize(1));
    }

    @Test
    public void adminServerPoolPropertiesShouldFallbackToDefault() {
        addDefaultServerProperties();
        addAdminServerWithIndex1();

        List<LdapServerConfiguration> ldapServerConfigurations
                = LdapServerConfiguration.extractConfigurations(properties, "shs.ldap.admin");

        assertThat(ldapServerConfigurations, hasSize(1));
        assertEquals(String.valueOf(ldapServerConfigurations.get(0).getInitSize()), properties.getProperty("shs.ldap.connect.pool.initsize"));
        assertEquals(String.valueOf(ldapServerConfigurations.get(0).getMaxSize()), properties.getProperty("shs.ldap.connect.pool.maxsize"));
        assertEquals(String.valueOf(ldapServerConfigurations.get(0).getPreferredSize()), properties.getProperty("shs.ldap.connect.pool.prefsize"));
        assertEquals(String.valueOf(ldapServerConfigurations.get(0).getTimeout()), properties.getProperty("shs.ldap.connect.pool.timeout"));


    }

    private void assertLdapServerConfiguration(LdapServerConfiguration ldapServerConfiguration, int serverNumber) {
        assertEquals(ldapServerConfiguration.getUrl(), properties.getProperty("shs.ldap." + serverNumber + ".url"));
        assertEquals(ldapServerConfiguration.getUserDn(), properties.getProperty("shs.ldap." + serverNumber + ".userDn"));
        assertEquals(ldapServerConfiguration.getPassword(), properties.getProperty("shs.ldap." + serverNumber + ".password"));
        assertEquals(String.valueOf(ldapServerConfiguration.getInitSize()), properties.getProperty("shs.ldap." + serverNumber + ".connect.pool.initsize"));
        assertEquals(String.valueOf(ldapServerConfiguration.getMaxSize()), properties.getProperty("shs.ldap." + serverNumber + ".connect.pool.maxsize"));
        assertEquals(String.valueOf(ldapServerConfiguration.getPreferredSize()), properties.getProperty("shs.ldap." + serverNumber + ".connect.pool.prefsize"));
        assertEquals(String.valueOf(ldapServerConfiguration.getTimeout()), properties.getProperty("shs.ldap." + serverNumber + ".connect.pool.timeout"));
    }

    private void addDefaultServerProperties() {
        properties.setProperty("shs.ldap.url", "url1");
        properties.setProperty("shs.ldap.userDn", "userDn1");
        properties.setProperty("shs.ldap.password", "password1");
        properties.setProperty("shs.ldap.connect.pool.initsize", "1");
        properties.setProperty("shs.ldap.connect.pool.prefsize", "2");
        properties.setProperty("shs.ldap.connect.pool.maxsize", "3");
        properties.setProperty("shs.ldap.connect.pool.timeout", "4");
    }

    private void addServerPropertiesWithIndex1() {
        properties.setProperty("shs.ldap.1.url", "url1");
        properties.setProperty("shs.ldap.1.userDn", "userDn1");
        properties.setProperty("shs.ldap.1.password", "password1");
        properties.setProperty("shs.ldap.1.connect.pool.initsize", "11");
        properties.setProperty("shs.ldap.1.connect.pool.prefsize", "12");
        properties.setProperty("shs.ldap.1.connect.pool.maxsize", "13");
        properties.setProperty("shs.ldap.1.connect.pool.timeout", "14");
    }

    private void addServerPropertiesWithIndex2() {
        properties.setProperty("shs.ldap.2.url", "url2");
        properties.setProperty("shs.ldap.2.userDn", "userDn2");
        properties.setProperty("shs.ldap.2.password", "password2");
        properties.setProperty("shs.ldap.2.connect.pool.initsize", "21");
        properties.setProperty("shs.ldap.2.connect.pool.prefsize", "22");
        properties.setProperty("shs.ldap.2.connect.pool.maxsize", "23");
        properties.setProperty("shs.ldap.2.connect.pool.timeout", "24");
    }

    private void addAdminServerWithIndex1() {
        properties.setProperty("shs.ldap.admin.1.url", "adminurl1");
        properties.setProperty("shs.ldap.admin.1.userDn", "adminuserDn1");
        properties.setProperty("shs.ldap.admin.1.password", "adminpassword1");
    }
}
