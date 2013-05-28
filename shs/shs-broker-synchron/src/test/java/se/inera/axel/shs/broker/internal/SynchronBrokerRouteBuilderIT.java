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
package se.inera.axel.shs.broker.internal;

import com.natpryce.makeiteasy.Maker;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.apache.camel.testng.AbstractCamelTestNGSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.inera.axel.shs.agreement.AgreementAdminService;
import se.inera.axel.shs.directory.Address;
import se.inera.axel.shs.directory.DirectoryService;
import se.inera.axel.shs.messagestore.MessageLogService;
import se.inera.axel.shs.messagestore.ShsMessageEntry;
import se.inera.axel.shs.product.ProductAdminService;
import se.inera.axel.shs.protocol.ShsMessage;
import se.inera.axel.shs.xml.label.ShsLabelMaker;
import se.inera.axel.shs.xml.label.TransferType;

import java.util.UUID;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.apache.camel.builder.SimpleBuilder.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static se.inera.axel.shs.protocol.ShsMessageMaker.ShsMessage;
import static se.inera.axel.shs.protocol.ShsMessageMaker.ShsMessageInstantiator.label;
import static se.inera.axel.shs.xml.Product.ShsProductMaker.testProduct1;
import static se.inera.axel.shs.xml.Product.ShsProductMaker.testProduct2;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.*;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.CustomerInstantiator.value;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsAgreementInstantiator.shs;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsAgreementInstantiator.uuid;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsInstantiator.customer;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabel;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.to;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.transferType;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.To;

@ContextConfiguration
@MockEndpointsAndSkip("http:shsServer|shs:local")
public class SynchronBrokerRouteBuilderIT extends AbstractCamelTestNGSpringContextTests {

    static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SynchronBrokerRouteBuilderIT.class);

    public SynchronBrokerRouteBuilderIT() {
    }

    @Autowired
    MessageLogService messageLogService;

    @Autowired
    AgreementAdminService agreementAdminService;

    @Autowired
    ProductAdminService productAdminService;

    @Autowired
    DirectoryService directoryService;

    @Produce(context = "shs-broker-synchron")
    ProducerTemplate camel;

    @EndpointInject(uri = "mock:http:shsServer")
    MockEndpoint shsServerEndpoint;

    @EndpointInject(uri = "mock:shs:local")
    MockEndpoint shsLocalEndpoint;

    @BeforeMethod
    public void beforeMethod() {
        initDb();
        initDirectory();
    }

    private void initDirectory() {
        Address testAddress = new Address();
        testAddress.setSerialNumber("");
        testAddress.setOrganizationNumber("1111111111");
        testAddress.setDeliveryMethods("http://nonexisting");

        given(directoryService.getAddress(eq("1111111111"), anyString()))
            .willReturn(testAddress);
    }

    private void initDb() {
        initProducts();
        initAgreements();
    }

    private void initProducts() {
        productAdminService.save(make(testProduct1));
        productAdminService.save(make(testProduct2));
    }

    private void initAgreements() {
        agreementAdminService.save(make(a(ShsAgreement)));

        // Local agreement
        agreementAdminService.save(make(a(ShsAgreement,
                with(uuid, UUID.randomUUID().toString()),
                with(shs, a(Shs,
                        with(customer, a(Customer,
                                with(value, "0000000000"))))))));
    }

    @DirtiesContext
    @Test
    public void sendingSynchRequestWithLocalReceiver() throws Exception {
        shsLocalEndpoint.expectedMessageCount(1);

        ShsMessage testMessage = make(createSynchMessageWithLocalReceiver());
        ShsMessageEntry entry = messageLogService.createEntry(testMessage);
        String response = camel.requestBody("direct-vm:shs:synchronBroker", entry, String.class);

        shsLocalEndpoint.assertIsSatisfied();
    }

    @DirtiesContext
    @Test
    public void sendingSynchRequestWithKnownReceiver() throws Exception {
        shsServerEndpoint.expectedMessageCount(1);
        shsServerEndpoint.expectedMessagesMatches(simple("${body.dataParts[0]?.dataHandler.content} == '" + ShsLabelMaker.DEFAULT_TEST_BODY + "'"));

        ShsMessage testMessage = make(createSynchMessageWithKnownReceiver());
        ShsMessageEntry entry = messageLogService.createEntry(testMessage);

        String response = camel.requestBody("direct-vm:shs:synchronBroker", entry, String.class);

        shsServerEndpoint.assertIsSatisfied();
    }

    private Maker<ShsMessage> createSynchMessageWithLocalReceiver() {
        return a(ShsMessage,
                with(label, a(ShsLabel,
                        with(to, a(To,
                                with(ShsLabelMaker.ToInstantiator.value, "0000000000"))),
                        with(transferType, TransferType.SYNCH))));
    }

    private Maker<ShsMessage> createSynchMessageWithKnownReceiver() {

        return a(ShsMessage,
                with(label, a(ShsLabel,
                        with(transferType, TransferType.SYNCH),
                        with(to, a(To,
                                with(ShsLabelMaker.ToInstantiator.value, "1111111111"))))));
    }


}
