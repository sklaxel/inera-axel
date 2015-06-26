package se.inera.axel.shs.broker.validation.certificate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import se.inera.axel.shs.exception.IllegalSenderException;

@ContextConfiguration
public class SenderCertificateValidatorTest extends AbstractTestNGSpringContextTests {
    
    private String PEM_CERTIFICATE_DUMMY;
    private X509Certificate X509_CERTIFICATE_DUMMY;

    @Autowired
    private SenderCertificateValidator validator = new SenderCertificateValidator();

    @Autowired
    private CertificateExtractor certificateExtractor;

    @BeforeClass
    public void beforeClass() throws IOException, URISyntaxException,
            CertificateException {

        byte[] validBytes = Files.readAllBytes(Paths.get(getClass()
                .getClassLoader()
                .getResource(TestBase.CERTIFICATE_FILE_NAME_VALID).toURI()));
        PEM_CERTIFICATE_DUMMY = new String(validBytes, Charset.forName("UTF-8"));

        X509_CERTIFICATE_DUMMY = PemConverter.convertPemToX509Certificate(PEM_CERTIFICATE_DUMMY);
    }

    @DirtiesContext
    @Test
    public void shouldPassValidationWhenAllValid() {
        validator.validateSender(MockConfig.CALLER_IP_EXISTING_IN_WHITELIST, PEM_CERTIFICATE_DUMMY, MockConfig.SENDER_THAT_MATCHES_MOCK);
    }

    @DirtiesContext
    @Test
    public void shouldPassValidationWhenAllValidAndCertificateIsOfTypeX509() {
        validator.validateSender(MockConfig.CALLER_IP_EXISTING_IN_WHITELIST, X509_CERTIFICATE_DUMMY, MockConfig.SENDER_THAT_MATCHES_MOCK);
    }

    @DirtiesContext
    @Test(expectedExceptions = IllegalSenderException.class)
    public void shouldThrowExceptionWhenSenderDoesNotMatchCertificateSender() {
        validator.validateSender(MockConfig.CALLER_IP_EXISTING_IN_WHITELIST, PEM_CERTIFICATE_DUMMY, MockConfig.SENDER_THAT_DOES_NOT_MATCH_MOCK);
    }

    @DirtiesContext
    @Test
    public void shouldPassValidationWhenSenderDoesNotMatchCertificateSenderAndWhenValidationDisabled() {
        validator.setEnabled(false);
        validator.validateSender(MockConfig.CALLER_IP_EXISTING_IN_WHITELIST, PEM_CERTIFICATE_DUMMY, MockConfig.SENDER_THAT_DOES_NOT_MATCH_MOCK);
    }

    @DirtiesContext
    @Test(expectedExceptions = IllegalSenderException.class)
    public void shouldThrowExceptionWhenCallerIpIsNull() {
        validator.validateSender(null, PEM_CERTIFICATE_DUMMY, MockConfig.SENDER_THAT_MATCHES_MOCK);
    }

    @DirtiesContext
    @Test(expectedExceptions = IllegalSenderException.class)
    public void shouldThrowExceptionWhenCertificateIsNull() {
        validator.validateSender(MockConfig.CALLER_IP_EXISTING_IN_WHITELIST, null, MockConfig.SENDER_THAT_MATCHES_MOCK);
    }

    @DirtiesContext
    @Test(expectedExceptions = IllegalSenderException.class)
    public void shouldThrowExceptionWhenSenderIsNull() {
        validator.validateSender(MockConfig.CALLER_IP_EXISTING_IN_WHITELIST, PEM_CERTIFICATE_DUMMY, null);
    }

    @DirtiesContext
    @Test(expectedExceptions = IllegalSenderException.class)
    public void shouldThrowExceptionWhenCallerIpIsNotOnWhiteList() {
        validator.validateSender(MockConfig.CALLER_IP_MISSING_IN_WHITELIST, PEM_CERTIFICATE_DUMMY, MockConfig.SENDER_THAT_MATCHES_MOCK);
    }

    @DirtiesContext
    @Test
    public void shouldAlwaysPassValidationWhenSenderEqualsTheOrgIdOfThisShsServer() {
        validator.validateSender(MockConfig.CALLER_IP_EXISTING_IN_WHITELIST, PEM_CERTIFICATE_DUMMY, MockConfig.SENDER_THAT_MATCHES_LOCAL_ORGID);
    }
}
