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

import org.hamcrest.Matcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.directory.*;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class DirectoryServiceGatewayTest {
    private DirectoryServiceGateway directoryServiceGateway;

    @Mock
    private DirectoryService directoryService1;

    @Mock
    private DirectoryService directoryService2;

    private Organization org1;
    private Organization org2;

    private Address address1;
    private Address address2;

    private Agreement agreement1;
    private Agreement agreement2;

    private ProductType productType1;
    private ProductType productType2;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        directoryServiceGateway = new DirectoryServiceGateway();
        directoryServiceGateway.setDirectoryServices(Arrays.asList(directoryService1, directoryService2));

        org1 = new Organization();
        org2 = new Organization();

        address1 = new Address();
        address2 = new Address();

        agreement1 = new Agreement();
        agreement2 = new Agreement();

        productType1 = new ProductType();
        productType2 = new ProductType();

        setUpDirectoryService1();
        setUpDirectoryService2();
    }

    private void setUpDirectoryService1() {
        when(directoryService1.getOrganization("org1")).thenReturn(org1);
        when(directoryService1.getAddress("org1", "product1")).thenReturn(address1);
        when(directoryService1.findAgreements("org1", "product1")).thenReturn(Arrays.asList(agreement1));
        when(directoryService1.getProductType("org1", "product1")).thenReturn(productType1);
    }

    private void setUpDirectoryService2() {
        when(directoryService2.getOrganization("org2")).thenReturn(org2);
        when(directoryService2.getAddress("org2", "product1")).thenReturn(address2);
        when(directoryService2.findAgreements("org2", "product1")).thenReturn(Arrays.asList(agreement2));
        when(directoryService2.getProductType("org2", "product2")).thenReturn(productType2);
    }

    @Test
    public void getOrganizationShouldReturnFirstMatch() throws Exception {
        Organization organization = directoryServiceGateway.getOrganization("org1");

        assertEquals(organization, org1);
    }

    @Test
    public void getOrganizationFromSecondServerIfNotFoundInFirst() throws Exception {
        Organization organization = directoryServiceGateway.getOrganization("org2");

        assertEquals(organization, org2);
    }

    @Test
    public void getOrganizationShouldReturnNullForNonExistingOrganization() throws Exception {
        Organization organization = directoryServiceGateway.getOrganization("doNotExist");

        assertNull(organization);
    }

    @Test
    public void getAddressShouldReturnFirstMatch() throws Exception {
        Address address = directoryServiceGateway.getAddress("org1", "product1");

        assertEquals(address, address1);
    }

    @Test
    public void getAddressFromSecondServerIfNotFoundInFirst() throws Exception {
        Address address = directoryServiceGateway.getAddress("org2", "product1");

        assertEquals(address, address2);
    }

    @Test
    public void getAddressShouldReturnNullForNonExistingOrganization() throws Exception {
        Address address = directoryServiceGateway.getAddress("org2", "doNotExist");

        assertNull(address);
    }

    @Test
    public void findAgreementsShouldReturnFirstMatch() throws Exception {
        List<Agreement> agreements = directoryServiceGateway.findAgreements("org1", "product1");

        assertThat(agreements, is(Arrays.asList(agreement1)));
    }

    @Test
    public void findAgreementsFromSecondServerIfNotFoundInFirst() {
        List<Agreement> agreements = directoryServiceGateway.findAgreements("org2", "product1");

        assertThat(agreements, is(Arrays.asList(agreement2)));
    }

    @Test
    public void findAgreementsShouldReturnEmptyListIfNotFound() {
        List<Agreement> agreements = directoryServiceGateway.findAgreements("org2", "doNotExist");

        assertThat(agreements, empty());
    }

    @Test
    public void getProductTypeShouldReturnFirstMatch() throws Exception {
        ProductType productType = directoryServiceGateway.getProductType("org1", "product1");

        assertEquals(productType, productType1);
    }

    @Test
    public void getProductTypeFromSecondServerIfNotFoundInFirst() throws Exception {
        ProductType productType = directoryServiceGateway.getProductType("org2", "product2");

        assertEquals(productType, productType2);
    }

    @Test
    public void getProductTypeShouldReturnNullForNonExistingProductType() throws Exception {
        ProductType productType = directoryServiceGateway.getProductType("org2", "product1");

        assertNull(productType);
    }
}
