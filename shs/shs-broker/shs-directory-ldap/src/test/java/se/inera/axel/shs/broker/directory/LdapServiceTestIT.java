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
package se.inera.axel.shs.broker.directory;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.directory.internal.LdapDirectoryService;

import javax.annotation.Resource;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@ContextConfiguration("classpath:LdapServiceTest-context.xml")
public class LdapServiceTestIT extends AbstractTestNGSpringContextTests {

	private org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LdapServiceTestIT.class);
	
	@Resource(name="directoryService")
	private DirectoryService ldapDirectory;

	@Resource(name="directoryAdminService")
	private DirectoryAdminService directoryAdminService;
	
	@Resource(name="ehCacheManager")
	private CacheManager cacheManager;
	
	private Organization testVerket;
	ProductType testProduct;
	Agreement testAgreement;
	Address testAddress;
	
	@AfterMethod
	public void clearCache() {
		cacheManager.clearAll();
	}

	@BeforeClass
	public void setUp() {
		Assert.assertNotNull(applicationContext, "Spring context not properly configured");
		Assert.assertNotNull(ldapDirectory, "Ldap directory not created");
		if (ldapDirectory instanceof LdapDirectoryService) {
			LdapDirectoryService d = (LdapDirectoryService) ldapDirectory;
			Assert.assertNotNull(d.getLdapTemplate(), "Ldap template not loaded");
			LdapOperations ops = d.getLdapTemplate().getLdapOperations();
			Assert.assertNotNull(ops, "ldap directory not connected");
		}
		
		log.info("app context checked");
		
		testVerket = new Organization();
		testVerket.setOrgName("Testverket");
		testVerket.setOrgNumber("666");
		testVerket.setDescription("Verket");
		
		directoryAdminService.saveActor(testVerket);	
		
		testProduct = new ProductType();
		testProduct.setProductName("axelTestProduct");
		testProduct.setSerialNumber("0f696d90-e854-11e1-aff1-0800200c9a66");
		
		directoryAdminService.saveProduct(testVerket, testProduct);
		
		testAgreement = new Agreement();
		testAgreement.setSerialNumber("131f12d0-e865-11e1-aff1-0800200c9a11");
		testAgreement.setPrincipal(testVerket.getOrgNumber());
		testAgreement.setProductName(testProduct.getProductName());
		testAgreement.setProductId(testProduct.getSerialNumber());
		testAgreement.setTransferType("asynch");
		
		directoryAdminService.saveAgreement(testVerket, testAgreement);
		
		testAgreement = new Agreement();
		testAgreement.setSerialNumber("77adc444-cd77-4e0e-8c28-8e1c6066385c");
		testAgreement.setPrincipal(testVerket.getOrgNumber());
		testAgreement.setProductName(testProduct.getProductName());
		testAgreement.setProductId(testProduct.getSerialNumber());
		testAgreement.setTransferType("synch");
		
		directoryAdminService.saveAgreement(testVerket, testAgreement);
		
		testAddress = new Address();
		testAddress.setSerialNumber(testProduct.getSerialNumber());
		testAddress.setOrganizationNumber(testVerket.getOrgNumber());
		testAddress.setDeliveryMethods("http://localhost");
		directoryAdminService.saveAddress(testVerket, testAddress);
	}
	

	@AfterClass
	public void tearDown() {
		directoryAdminService.deleteActor(testVerket);
		testVerket = null;
	}
	

	@Test
	public void getActorTest() {
		String orgNumber = testVerket.getOrgNumber();
		Organization organization = ldapDirectory.getOrganization(orgNumber);
		
		assertNotNull(organization, "Organization not found");
		assertEquals(organization.getDescription(), testVerket.getDescription());
		
		log.info("organization={}", organization.getOrgName());
	}
	

	@Test
	public void getProductTypeTest() throws DirectoryException {
		String orgNumber = testVerket.getOrgNumber();
		String productId = testProduct.getSerialNumber();
		
		ProductType product = ldapDirectory.getProductType(orgNumber, productId);
		
		Assert.assertNotNull(product, "Product not found");
		Assert.assertEquals(product.getProductName(), testProduct.getProductName(), "Product not match");
		log.info("Product name={}", product.getProductName());
	}



	@Test
	public void getAgreementTest() throws DirectoryException {
		String principal = testVerket.getOrgNumber();
		String productId = testProduct.getSerialNumber();
		String transferType = testAgreement.getTransferType();
		
		Agreement agreement = ldapDirectory.getAgreement(principal, productId, transferType);
		
		Assert.assertNotNull(agreement, "agreement not found");
		Assert.assertEquals(agreement.getProductName(), testProduct.getProductName());
		
		log.info("agreement={}", agreement.getSerialNumber());
		
	}
	
	@Test
	public void findAgreementsTest() throws DirectoryException {
		String principal = testVerket.getOrgNumber();
		String productId = testProduct.getSerialNumber();
		
		List<Agreement> agreements = ldapDirectory.findAgreements(principal, productId);
		
		assertThat(agreements, hasSize(2));
		
		for (Agreement agreement : agreements) {
			Assert.assertEquals(agreement.getProductName(), testProduct.getProductName());
			log.info("agreement={}", agreement.getSerialNumber());
		}
	}
	
	@Test
	public void findAgreementsWithNonExistingProductShouldReturnEmptyList() throws DirectoryException {
		String principal = testVerket.getOrgNumber();
		// Product that does not exist in the directory
		String productId = "da342e9d-94da-45ef-9387-917c560f72f1";
		
		List<Agreement> agreements = ldapDirectory.findAgreements(principal, productId);
		
		assertThat(agreements, hasSize(0));
	}

	
	@Test
	public void getAddressTest() throws DirectoryException {
		String principal = testVerket.getOrgNumber();
		String productSerialNumber = testProduct.getSerialNumber();
		
		Address address = ldapDirectory.getAddress(principal, productSerialNumber);
		
		Assert.assertNotNull(address, "address not found");
		Assert.assertEquals(address.getDeliveryMethods(), testAddress.getDeliveryMethods());
		
		log.info("address={}", address.getSerialNumber());
		
	}
	
	@Test
	public void getAddressWithCacheTest() throws DirectoryException {
		Cache cache = cacheManager.getCache("ldap");

		String principal = testVerket.getOrgNumber();
		String productSerialNumber = testProduct.getSerialNumber();
		
		
		for (Object key : cache.getKeys()) {
			System.out.println("key: " + key);
		}
		assertEquals(cache.getKeys().size(), 0, "cache should be empty before call");
		
		Address address = ldapDirectory.getAddress(principal, productSerialNumber);
		
		Assert.assertNotNull(address, "address not found");
		Assert.assertEquals(address.getDeliveryMethods(), testAddress.getDeliveryMethods());
		
		log.info("address={}", address.getSerialNumber());
		assertEquals(cache.getKeys().size(), 1, "cache should contain one entry after call");
		
		address = ldapDirectory.getAddress(principal, productSerialNumber);
		
		Assert.assertNotNull(address, "address not found");
		Assert.assertEquals(address.getDeliveryMethods(), testAddress.getDeliveryMethods());
		
		log.info("address={}", address.getSerialNumber());
		assertEquals(cache.getKeys().size(), 1, "cache should contain one entry after second call");
		
	}
	 
}
