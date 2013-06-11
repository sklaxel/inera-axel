/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.broker.directory;

import org.springframework.ldap.core.LdapOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.directory.internal.LdapDirectoryAdminService;

import javax.annotation.Resource;
import java.util.List;

import static org.testng.Assert.*;

@ContextConfiguration("classpath:LdapServiceTest-context.xml")
public class LdapAdminServiceTestIT extends AbstractTestNGSpringContextTests {
	private org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LdapAdminServiceTestIT.class);
	
	@Resource(name="directoryAdminService")
	private DirectoryAdminService directoryAdminService;
	
	private Organization testVerket;

	
	@BeforeMethod
	public void setUp() {
		Assert.assertNotNull(applicationContext, "Spring context not properly configured");
		Assert.assertNotNull(directoryAdminService, "Ldap directory not created");
		if (directoryAdminService instanceof LdapDirectoryAdminService) {
			LdapDirectoryAdminService d = (LdapDirectoryAdminService) directoryAdminService;
			Assert.assertNotNull(d.getLdapTemplate(), "Ldap template not loaded");
			LdapOperations ops = d.getLdapTemplate().getLdapOperations();
			Assert.assertNotNull(ops, "ldap directory not connected");
		}
		
		testVerket = new Organization();
		testVerket.setOrgName("Testverket");
		testVerket.setOrgNumber("666");
		testVerket.setDescription("Verket");
		
		directoryAdminService.saveActor(testVerket);		
	}

	@AfterMethod
	public void tearDown() {
		directoryAdminService.deleteActor(testVerket);
		testVerket = null;
	}
	
	@Test
	public void getActorsTest() {
		
		List<Organization> l = directoryAdminService.getOrganizations();
		Assert.assertNotNull(l, "no actors found");
		Assert.assertNotEquals(l.size(), 0, "no actors found");
		
		log.info("Entries found {}", l.size());
		
		for (Organization a : l) {
			log.info("actor={}", a.orgName);
		}
	}

	
	@Test
	public void updateActorTest() {
		String orgNumber = testVerket.getOrgNumber();
		Organization organization = directoryAdminService.getOrganization(orgNumber);
		
		
		assertNotNull(organization);
		organization.setDescription("new description");

		directoryAdminService.saveActor(organization);
		
		organization = directoryAdminService.getOrganization(orgNumber);
		Assert.assertEquals(organization.getDescription(), "new description");
		
		organization.setDescription(null);

		directoryAdminService.saveActor(organization);
		
		organization = directoryAdminService.getOrganization(orgNumber);
		Assert.assertNull(organization.getDescription());
	}
	
	@Test
	public void removeActorTest() {
		String orgNumber = testVerket.getOrgNumber();
		
		Organization organization = directoryAdminService.getOrganization(orgNumber);
		assertNotNull(organization);
		
		directoryAdminService.deleteActor(organization);
		
		// TODO fix not logging everything for missing actors in directoryservice
		organization = directoryAdminService.getOrganization(orgNumber);

		assertNull(organization);
		
		// TODO fix "delete organization" properly.
//		List<Agreement> agreementList = directoryAdminService.getAgreements(organization);
//		assertEquals(agreementList.size(), 0, "no agreements should exist for deleted organization");
	}
	
	

	@Test
	public void createProductTest() {
		String orgNumber = testVerket.getOrgNumber();
		
		Organization organization = directoryAdminService.getOrganization(orgNumber);
		assertNotNull(organization);
		
		String productId = "0f696d90-e854-11e1-aff1-0800200c9a66";
		
		ProductType product =  directoryAdminService.getProductType(orgNumber, productId);
		assertNull(product);
		
		product = new ProductType();
		product.setProductName("axelTestProduct");
		product.setSerialNumber(productId);
		
		directoryAdminService.saveProduct(organization, product);
		
		product =  directoryAdminService.getProductType(orgNumber, productId);
		assertNotNull(product);
		assertEquals(product.getProductName(), "axelTestProduct");
	}
	
	@Test
	public void updateProductTest() {
		String orgNumber = testVerket.getOrgNumber();
		
		Organization organization = directoryAdminService.getOrganization(orgNumber);
		assertNotNull(organization);
		String productId = "0f696d90-e854-11e1-aff1-0800200c9a66";
		
		ProductType product =  directoryAdminService.getProductType(orgNumber, productId);
		assertNull(product);
		
		product = new ProductType();
		product.setSerialNumber(productId);		
		product.setProductName("test.product");
		product.setDescription("new description");

		directoryAdminService.saveProduct(organization, product);
		
		product = directoryAdminService.getProductType(orgNumber, productId);
		
		assertNotNull(product);
		Assert.assertEquals(product.getDescription(), "new description");
		
		product.setDescription(null);

		directoryAdminService.saveProduct(organization, product);
		
		product = directoryAdminService.getProductType(orgNumber, productId);
		Assert.assertNull(product.getDescription());
	}
	
	
	@Test
	public void getProductTypesTest() throws DirectoryException {
		String orgNumber = testVerket.getOrgNumber();
		
		Organization organization = directoryAdminService.getOrganization(orgNumber);
		assertNotNull(organization);
		
		List<ProductType> productList = directoryAdminService.getProductTypes(organization);
		assertNotNull(productList);
		assertEquals(productList.size(), 0, "There should be no product type for this organization");
		
		ProductType product = new ProductType();
		product.setProductName("axelTestProduct");
		product.setSerialNumber("0f696d90-e854-11e1-aff1-0800200c9a66");
		
		directoryAdminService.saveProduct(organization, product);

		product = new ProductType();
		product.setProductName("axelTestProduct2");
		product.setSerialNumber("0f696d90-e854-11e1-aff1-0800200c9a44");
		
		directoryAdminService.saveProduct(organization, product);
		
		productList = directoryAdminService.getProductTypes(organization);
		assertNotNull(productList);
		assertEquals(productList.size(), 2, "There should be two product types for this organization");
	}
	
	@Test
	public void removeProductTest() {
		String orgNumber = testVerket.getOrgNumber();
		
		Organization organization = directoryAdminService.getOrganization(orgNumber);
		assertNotNull(organization);
		
		String productId = "0f696d90-e854-11e1-aff1-0800200c9a66";
		
		ProductType product = new ProductType();
		product.setSerialNumber(productId);		
		product.setProductName("test.product");
		product.setDescription("new description");

		directoryAdminService.saveProduct(organization, product);
		
		product = directoryAdminService.getProductType(orgNumber, productId);
		assertNotNull(product);
		
		directoryAdminService.removeProduct(organization, product);
		
		product = directoryAdminService.getProductType(orgNumber, productId);
		assertNull(product);
	}

	
	
	@Test
	public void createAgreementTest() {
		String orgNumber = testVerket.getOrgNumber();
		
		Organization organization = directoryAdminService.getOrganization(orgNumber);
		assertNotNull(organization);
		
		String productId = "0f696d90-e854-11e1-aff1-0800200c9a66";
		String transferType = "asynch";
		Agreement agreement = directoryAdminService.getAgreement(orgNumber, productId, transferType);
		assertNull(agreement);
		
		agreement = new Agreement();
		String agreementId = "131f12d0-e865-11e1-aff1-0800200c9a66";
		agreement.setSerialNumber(agreementId);
		agreement.setPrincipal(orgNumber); // TODO orgNumber.toUrnForm()
		agreement.setProductName("axelTestProduct");
		agreement.setProductId(productId);
		agreement.setTransferType(transferType);
		
		directoryAdminService.saveAgreement(organization, agreement);
		agreement = directoryAdminService.getAgreement(orgNumber, productId, transferType);
		assertNotNull(agreement);
		assertEquals(agreement.getSerialNumber(), agreementId);
	}
	
	@Test
	public void updateAgreementTest() {
		String orgNumber = testVerket.getOrgNumber();
		
		Organization organization = directoryAdminService.getOrganization(orgNumber);
		assertNotNull(organization);
		
		String productId = "0f696d90-e854-11e1-aff1-0800200c9a66";
		String transferType = "asynch";
		
		Agreement agreement = new Agreement();
		String agreementId = "131f12d0-e865-11e1-aff1-0800200c9a66";
		agreement.setSerialNumber(agreementId);
		agreement.setPrincipal(orgNumber); // TODO orgNumber.toUrnForm()
		agreement.setProductName("axelTestProduct");
		agreement.setProductId(productId);
		agreement.setTransferType(transferType);
		
		directoryAdminService.saveAgreement(organization, agreement);
		
		agreement = directoryAdminService.getAgreement(orgNumber, productId, transferType);
		assertNotNull(agreement);		
		assertNull(agreement.getDescription());
		
		agreement.setDescription("new description");
		directoryAdminService.saveAgreement(organization, agreement);
		
		agreement = directoryAdminService.getAgreement(orgNumber, productId, transferType);
		assertNotNull(agreement);
		Assert.assertEquals(agreement.getDescription(), "new description");
	}
	

	@Test
	public void getAgreementsTest() throws DirectoryException {
		
		String orgNumber = testVerket.getOrgNumber();
		
		Organization organization = directoryAdminService.getOrganization(orgNumber);
		assertNotNull(organization);
		
		List<Agreement> agreementList = directoryAdminService.getAgreements(organization);
		assertNotNull(agreementList);
		assertEquals(agreementList.size(), 0, "There should be no agreements for this organization");
		
		Agreement agreement = new Agreement();
		agreement.setSerialNumber("131f12d0-e865-11e1-aff1-0800200c9a66");
		agreement.setPrincipal(orgNumber);
		agreement.setProductName("axelTestProduct");
		agreement.setProductId("0f696d90-e854-11e1-aff1-0800200c9a66");
		agreement.setTransferType("asynch");
		
		directoryAdminService.saveAgreement(organization, agreement);
		
		agreement = new Agreement();
		agreement.setSerialNumber("131f12d0-e865-11e1-aff1-0800200c9a44");
		agreement.setPrincipal(orgNumber);
		agreement.setProductName("axelTestProduct2");
		agreement.setProductId("0f696d90-e854-11e1-aff1-0800200c9a44");
		agreement.setTransferType("synch");
		
		directoryAdminService.saveAgreement(organization, agreement);
		
		agreementList = directoryAdminService.getAgreements(organization);
		assertNotNull(agreementList);
		assertEquals(agreementList.size(), 2, "There should be two agreements for this organization");
	}

	
	@Test
	public void removeAgreementTest() {
		String orgNumber = testVerket.getOrgNumber();
		
		Organization organization = directoryAdminService.getOrganization(orgNumber);
		assertNotNull(organization);
		
		String productId = "0f696d90-e854-11e1-aff1-0800200c9a66";
		String transferType = "asynch";
		
		Agreement agreement = new Agreement();
		String agreementId = "131f12d0-e865-11e1-aff1-0800200c9a66";
		agreement.setSerialNumber(agreementId);
		agreement.setPrincipal(orgNumber); // TODO orgNumber.toUrnForm()
		agreement.setProductName("axelTestProduct");
		agreement.setProductId(productId);
		agreement.setTransferType(transferType);
		
		directoryAdminService.saveAgreement(organization, agreement);
		
		agreement = directoryAdminService.getAgreement(orgNumber, productId, transferType);
		assertNotNull(agreement);		
		
		directoryAdminService.removeAgreement(organization, agreement);
		agreement = directoryAdminService.getAgreement(orgNumber, productId, transferType);
		
		assertNull(agreement);
	}

	
	@Test
	public void createAddressTest() {
		String orgNumber = testVerket.getOrgNumber();
		
		Organization organization = directoryAdminService.getOrganization(orgNumber);
		assertNotNull(organization);
		
		String productId = "0f696d90-e854-11e1-aff1-0800200c9a66";
		Address address = directoryAdminService.getAddress(orgNumber, productId);
		assertNull(address);
		
		address = new Address();
		address.setSerialNumber(productId);
		address.setOrganizationNumber(orgNumber);
		
		directoryAdminService.saveAddress(organization, address);
		
		address = directoryAdminService.getAddress(orgNumber, productId);
		assertNotNull(address);
		Assert.assertEquals(address.getSerialNumber(), productId);
	}
	
	@Test
	public void updateAddressTest() {
		String orgNumber = testVerket.getOrgNumber();
		
		Organization organization = directoryAdminService.getOrganization(orgNumber);
		assertNotNull(organization);
		
		String productId = "0f696d90-e854-11e1-aff1-0800200c9a66";
		Address address = directoryAdminService.getAddress(orgNumber, productId);
		assertNull(address);
		
		address = new Address();
		address.setSerialNumber(productId);
		address.setOrganizationNumber(orgNumber);
		
		directoryAdminService.saveAddress(organization, address);
		address = directoryAdminService.getAddress(orgNumber, productId);
		assertNotNull(address);
		assertNull(address.getDeliveryMethods());
		
		address.setDeliveryMethods("https://server");
		directoryAdminService.saveAddress(organization, address);
		address = directoryAdminService.getAddress(orgNumber, productId);
		assertNotNull(address);
		Assert.assertEquals(address.getDeliveryMethods(), "https://server");
	}
	

	@Test
	public void getAddressesTest() throws DirectoryException {
		String orgNumber = testVerket.getOrgNumber();
		
		Organization organization = directoryAdminService.getOrganization(orgNumber);
		assertNotNull(organization);
		List<Address> addressList = directoryAdminService.getAddresses(organization);
		assertNotNull(addressList);
		assertEquals(addressList.size(), 0, "There should be at least one address in list");
		
		Address address = new Address();
		address.setSerialNumber("0f696d90-e854-11e1-aff1-0800200c9a66");
		address.setOrganizationNumber(orgNumber);
		address.setDeliveryMethods("https://localhost");
		
		directoryAdminService.saveAddress(organization, address);

		address = new Address();
		address.setSerialNumber("0f696d90-e854-11e1-aff1-0800200c9a22");
		address.setOrganizationNumber(orgNumber);
		address.setDeliveryMethods("https://localhost2");
		
		directoryAdminService.saveAddress(organization, address);

		addressList = directoryAdminService.getAddresses(organization);
		
		assertNotNull(addressList);
		assertEquals(addressList.size(), 2, "There should be two addresses in list");
		
	}
	
	@Test
	public void removeAddressTest() {
		String orgNumber = testVerket.getOrgNumber();
		
		Organization organization = directoryAdminService.getOrganization(orgNumber);
		assertNotNull(organization);
		
		String productId = "0f696d90-e854-11e1-aff1-0800200c9a66";

		Address address = new Address();
		address.setSerialNumber(productId);
		address.setOrganizationNumber(orgNumber);
		
		directoryAdminService.saveAddress(organization, address);
		address = directoryAdminService.getAddress(orgNumber, productId);
		assertNotNull(address);
		assertNull(address.getDeliveryMethods());
		
		
		directoryAdminService.removeAddress(organization, address);
		
		address = directoryAdminService.getAddress(orgNumber, productId);
		
		assertNull(address);
	}

}
