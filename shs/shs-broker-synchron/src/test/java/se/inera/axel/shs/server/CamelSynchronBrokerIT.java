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
package se.inera.axel.shs.server;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelSpringTestSupport;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.inera.axel.shs.agreement.AgreementAdminService;
import se.inera.axel.shs.xml.agreement.ShsAgreement;
import se.inera.axel.shs.xml.label.ShsLabel;

public class CamelSynchronBrokerIT extends CamelSpringTestSupport {


	//@Autowired
	private AgreementAdminService agreementService;
	
	@Override
	protected AbstractApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("CamelSynchronBrokerIT-context.xml");
	}
	
    @EndpointInject(uri = "mock:shsSenderTestEndpoint")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "ref:shsSynchronBrokerEndpoint")
    protected ProducerTemplate template;

    @BeforeMethod
    public void setup() {
    	populateAgreements();
    }
    
    
//    @DirtiesContext
//    @Test
//    public void testDirectAddressing() throws Exception {
//    	
//    	ShsMessage msg = new ShsMessage();
//    	ShsLabel label = loadLabel("src/test/testdata/shs-label-sync-direct-addr.xml");
//    	msg.setLabel(label);
//
//        resultEndpoint.expectedBodiesReceived(msg);
//        resultEndpoint.expectedHeaderReceived(ForwardBean.ORG_NUMBER_HDR, "5565317129");
//
//        template.sendBody(msg);
//
//        resultEndpoint.assertIsSatisfied();
//    }
//
//    @DirtiesContext
//    @Test
//    public void testDirectExternalAddressing() throws Exception {
//    	
//    	ShsMessage msg = new ShsMessage();
//    	ShsLabel label = loadLabel("src/test/testdata/shs-label-sync-direct-external-addr.xml");
//    	msg.setLabel(label);
//
//        resultEndpoint.expectedBodiesReceived(msg);
//        resultEndpoint.expectedHeaderReceived(Exchange.HTTP_URI, "http://192.168.1.43:8080/Logica/rs");
//
//        template.sendBody(msg);
//
//        resultEndpoint.assertIsSatisfied();
//    }
//
//
//    @DirtiesContext
//    @Test
//    public void testProductAddressing() throws Exception {
//    	
//    	ShsMessage msg = new ShsMessage();
//    	ShsLabel label = loadLabel("src/test/testdata/shs-label-sync-product-addr.xml");
//    	msg.setLabel(label);
//
//        resultEndpoint.expectedBodiesReceived(msg);
//        resultEndpoint.expectedHeaderReceived(ForwardBean.ORG_NUMBER_HDR, "5565317129");
//
//        template.sendBody(msg);
//
//        resultEndpoint.assertIsSatisfied();
//    }
//
//    @DirtiesContext
//    @Test
//    public void testPublicAgreementAddressing() throws Exception {
//    	
//    	ShsMessage msg = new ShsMessage();
//    	ShsLabel label = loadLabel("src/test/testdata/shs-label-sync-public-agreement-addr.xml");
//    	msg.setLabel(label);
//
//        resultEndpoint.expectedBodiesReceived(msg);
//        resultEndpoint.expectedHeaderReceived(ForwardBean.ORG_NUMBER_HDR, "5565317129");
//
//        template.sendBody(msg);
//
//        resultEndpoint.assertIsSatisfied();
//    }

    private ShsLabel loadLabel(String path) {
		ShsLabel label = loadEntity(path, ShsLabel.class);
		return label;
	}

	private <T> T loadEntity(String path, Class<T> type) {
		try {
			JAXBContext jc = JAXBContext.newInstance(type);
			
			Unmarshaller u = jc.createUnmarshaller ();
			
			File f = new File (path);
			T entity = (T) u.unmarshal(f);
			return entity;
    	} catch (Exception e) {
    		Assert.fail("failed to load entity", e);
    	}
		return null;
	}
    
    @Test(enabled=false)	// for setup only
    public void populateAgreements() {
    	agreementService =  (AgreementAdminService) applicationContext.getBean("agreementService");
		emptyDatabase();
		ShsAgreement a;
		a = loadEntity("src/test/testdata/shs-agreement1.xml", ShsAgreement.class);
		agreementService.save(a);
		a = loadEntity("src/test/testdata/shs-agreement2.xml", ShsAgreement.class);
		agreementService.save(a);
		a = loadEntity("src/test/testdata/shs-agreement3.xml", ShsAgreement.class);
		agreementService.save(a);
		a = loadEntity("src/test/testdata/shs-agreement4.xml", ShsAgreement.class);
		agreementService.save(a);
    }

    @Test(enabled=false)	// for setup only
	public void emptyDatabase() {
		List<ShsAgreement> l = agreementService.findAll();
		
		for (ShsAgreement a : l) {
			agreementService.delete(a.getUuid());
		}
	}

}
