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
package se.inera.axel.shs.broker.directory.internal;

import org.hamcrest.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.directory.*;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class DefaultDirectoryAdminServiceAggregatorTest {
    private DefaultDirectoryAdminServiceAggregator directoryAdminServiceAggregator;

    @Mock
    private DirectoryAdminService directoryAdminService1;

    @Mock
    private DirectoryAdminService directoryAdminService2;

    private Organization organization11;
    private Organization organization12;
    private Organization organization21;
    private Organization organization23;

    private ProductType productType11;
    private ProductType productType12;
    private ProductType productType21;
    private ProductType productType23;

    private Agreement agreement11;
    private Agreement agreement12;
    private Agreement agreement21;
    private Agreement agreement23;

    private Address address11;
    private Address address12;
    private Address address21;
    private Address address23;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        organization11 = new Organization();
        organization11.setOrgNumber("1");

        organization12 = new Organization();
        organization12.setOrgNumber("2");

        organization21 = new Organization();
        organization21.setOrgNumber("1");

        organization23 = new Organization();
        organization23.setOrgNumber("3");

        when(directoryAdminService1.getOrganizations()).thenReturn(Arrays.asList(organization11, organization12));
        when(directoryAdminService2.getOrganizations()).thenReturn(Arrays.asList(organization21, organization23));

        productType11 = new ProductType();
        productType11.setSerialNumber("1");

        productType12 = new ProductType();
        productType12.setSerialNumber("2");

        productType21 = new ProductType();
        productType21.setSerialNumber("1");

        productType23 = new ProductType();
        productType23.setSerialNumber("3");

        when(directoryAdminService1.getProductTypes(organization11)).thenReturn(Arrays.asList(productType11, productType12));
        when(directoryAdminService2.getProductTypes(organization11)).thenReturn(Arrays.asList(productType21, productType23));

        agreement11 = new Agreement();
        agreement11.setSerialNumber("1");

        agreement12 = new Agreement();
        agreement12.setSerialNumber("2");

        agreement21 = new Agreement();
        agreement21.setSerialNumber("1");

        agreement23 = new Agreement();
        agreement23.setSerialNumber("3");

        when(directoryAdminService1.getAgreements(organization11)).thenReturn(Arrays.asList(agreement11, agreement12));
        when(directoryAdminService2.getAgreements(organization11)).thenReturn(Arrays.asList(agreement21, agreement23));

        address11 = new Address();
        address11.setSerialNumber("1");
        address11.setOrganizationNumber("1");

        address12 = new Address();
        address12.setSerialNumber("2");
        address12.setOrganizationNumber("1");

        address21 = new Address();
        address21.setSerialNumber("1");
        address21.setOrganizationNumber("1");

        address23 = new Address();
        address23.setSerialNumber("3");
        address23.setOrganizationNumber("1");

        when(directoryAdminService1.getAddresses(organization11)).thenReturn(Arrays.asList(address11, address12));
        when(directoryAdminService2.getAddresses(organization11)).thenReturn(Arrays.asList(address21, address23));

        directoryAdminServiceAggregator = new DefaultDirectoryAdminServiceAggregator(Arrays.asList(directoryAdminService1, directoryAdminService2));
    }

    @Test
    public void testGetOrganizations() throws Exception {
        List<Organization> organizationList = directoryAdminServiceAggregator.getOrganizations();

        assertThat(organizationList, containsInAnyOrder(organization11, organization12, organization23));
    }

    @Test
    public void testGetProductTypes() throws Exception {
        List<ProductType> productTypes = directoryAdminServiceAggregator.getProductTypes(organization11);

        assertThat(productTypes, containsInAnyOrder(productType11, productType12, productType23));
    }

    @Test
    public void testGetAgreements() throws Exception {
        List<Agreement> agreements = directoryAdminServiceAggregator.getAgreements(organization11);

        assertThat(agreements, containsInAnyOrder(agreement11, agreement12, agreement23));
    }

    @Test
    public void testGetAddresses() throws Exception {
        List<Address> addresses = directoryAdminServiceAggregator.getAddresses(organization11);

        assertThat(addresses, containsInAnyOrder(address11, address12, address23));
    }
}
