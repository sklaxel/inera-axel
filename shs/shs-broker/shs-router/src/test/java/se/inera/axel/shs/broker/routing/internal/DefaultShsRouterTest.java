package se.inera.axel.shs.broker.routing.internal;

import com.natpryce.makeiteasy.MakeItEasy;
import com.natpryce.makeiteasy.Maker;
import com.natpryce.makeiteasy.SameValueDonor;
import org.hamcrest.MatcherAssert;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.inera.axel.shs.broker.agreement.AgreementService;
import se.inera.axel.shs.broker.directory.DirectoryService;
import se.inera.axel.shs.xml.agreement.Customer;
import se.inera.axel.shs.xml.agreement.ShsAgreement;
import se.inera.axel.shs.xml.agreement.ShsAgreementMaker;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.ShsLabelMaker;
import se.inera.axel.shs.xml.label.To;

import java.util.Arrays;
import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.*;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsAgreementInstantiator.shs;
import static se.inera.axel.shs.xml.agreement.ShsAgreementMaker.ShsInstantiator.customer;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.*;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.*;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ToInstantiator.*;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class DefaultShsRouterTest {
    private DefaultShsRouter defaultShsRouter;

    @Mock
    private AgreementService agreementServiceMock;

    @Mock
    private DirectoryService directoryServiceMock;

    private se.inera.axel.shs.xml.agreement.Customer customer2;
    private static String CUSTOMER2_ORG_NUMBER = "9999999999";

    @BeforeClass
    public void before() {
        customer2 = make(a(Customer,
                with(CustomerInstantiator.value, CUSTOMER2_ORG_NUMBER)));
    }

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        defaultShsRouter = new DefaultShsRouter();
        defaultShsRouter.setAgreementService(agreementServiceMock);
        defaultShsRouter.setDirectoryService(directoryServiceMock);
    }

    @Test
    public void resolveRecipientsWithDirectAddressing() throws Exception {
        ShsLabel label = make(a(ShsLabel));

        List<String> recipients = defaultShsRouter.resolveRecipients(label);

        assertThat("Recipient in label should be resolved", recipients, is(Arrays.asList(label.getTo().getvalue())));
    }

    @Test
    public void resolveRecipientFromExplicitAgreement() {
        ShsLabel label = make(a(ShsLabel,
                with(to, emptyTo()),
                with(shsAgreement, ShsAgreementMaker.DEFAULT_UUID)
                ));
        ShsAgreement agreement = make(a(ShsAgreement));

        when(agreementServiceMock.findOne(ShsAgreementMaker.DEFAULT_UUID)).thenReturn(agreement);

        List<String> recipients = defaultShsRouter.resolveRecipients(label);

        assertThat(recipients, is(Arrays.asList(agreement.getShs().getCustomer().getvalue())));
    }

    @Test
    public void resolveRecipientsForOneToMany() {
        ShsLabel label = make(a(ShsLabel,
                with(to, emptyTo())
            ));
        ShsAgreement agreement1 = make(a(ShsAgreement));
        ShsAgreement agreement2 = make(a(ShsAgreement,
                with(shs, a(Shs,
                        with(customer, customer2)))));


        when(agreementServiceMock.findAgreements(any(ShsLabel.class))).thenReturn(
                Arrays.asList(agreement1, agreement2)
        );

        List<String> recipients = defaultShsRouter.resolveRecipients(label);

        assertThat(recipients, is(Arrays.asList(ShsAgreementMaker.DEFAULT_CUSTOMER, CUSTOMER2_ORG_NUMBER)));
    }

    @Test
    public void resolveRecipientForManyToOne() {
        ShsLabel label = make(a(ShsLabel,
                with(to, emptyTo())
        ));

        ShsAgreement agreement = make(a(ShsAgreement,
                with(shs, a(Shs,
                        with(customer, new SameValueDonor<se.inera.axel.shs.xml.agreement.Customer>(null))))));

        when(agreementServiceMock.findAgreements(any(ShsLabel.class))).thenReturn(
                Arrays.asList(agreement)
        );

        List<String> recipients = defaultShsRouter.resolveRecipients(label);

        assertThat(recipients, is(Arrays.asList(ShsAgreementMaker.DEFAULT_PRINCIPAL)));
    }
}
