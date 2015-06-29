package se.inera.axel.shs.broker.validation.certificate;

import java.io.IOException;
import java.net.URISyntaxException;
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
    
    private X509Certificate validX509Certificate;

    @Autowired
    private SenderCertificateValidator validator;

    @Autowired
    private CertificateExtractor certificateExtractor;

    @BeforeClass
    public void beforeClass() throws IOException, URISyntaxException,
            CertificateException {

        validX509Certificate = PemConverter.convertPemToX509Certificate(TestCertificates.PEM_CERTIFICATE_VALID);
    }

    @DirtiesContext
    @Test
    public void shouldPassValidationWhenAllValid() {
        validator.validateSender(CertificateExtractorMock.CALLER_IP_EXISTING_IN_WHITELIST, TestCertificates.PEM_CERTIFICATE_VALID, CertificateExtractorMock.SENDER_VALID);
    }

    @DirtiesContext
    @Test
    public void shouldPassValidationWhenAllValidAndCertificateIsOfTypeX509() {
        validator.validateSender(CertificateExtractorMock.CALLER_IP_EXISTING_IN_WHITELIST, validX509Certificate, CertificateExtractorMock.SENDER_VALID);
    }

    @DirtiesContext
    @Test(expectedExceptions = IllegalSenderException.class)
    public void shouldThrowExceptionWhenSenderDoesNotMatchCertificateSender() {
        validator.validateSender(CertificateExtractorMock.CALLER_IP_EXISTING_IN_WHITELIST, TestCertificates.PEM_CERTIFICATE_VALID, CertificateExtractorMock.SENDER_INVALID);
    }

    @DirtiesContext
    @Test
    public void shouldPassValidationWhenSenderDoesNotMatchCertificateSenderAndWhenValidationDisabled() {
        validator.setEnabled(false);
        validator.validateSender(CertificateExtractorMock.CALLER_IP_EXISTING_IN_WHITELIST, TestCertificates.PEM_CERTIFICATE_VALID, CertificateExtractorMock.SENDER_INVALID);
    }

    @DirtiesContext
    @Test(expectedExceptions = IllegalSenderException.class)
    public void shouldThrowExceptionWhenCallerIpIsNull() {
        validator.validateSender(null, TestCertificates.PEM_CERTIFICATE_VALID, CertificateExtractorMock.SENDER_VALID);
    }

    @DirtiesContext
    @Test(expectedExceptions = IllegalSenderException.class)
    public void shouldThrowExceptionWhenCertificateIsNull() {
        validator.validateSender(CertificateExtractorMock.CALLER_IP_EXISTING_IN_WHITELIST, null, CertificateExtractorMock.SENDER_VALID);
    }

    @DirtiesContext
    @Test(expectedExceptions = IllegalSenderException.class)
    public void shouldThrowExceptionWhenSenderIsNull() {
        validator.validateSender(CertificateExtractorMock.CALLER_IP_EXISTING_IN_WHITELIST, TestCertificates.PEM_CERTIFICATE_VALID, null);
    }

    @DirtiesContext
    @Test(expectedExceptions = IllegalSenderException.class)
    public void shouldThrowExceptionWhenCallerIpIsNotInWhiteList() {
        validator.validateSender(CertificateExtractorMock.CALLER_IP_MISSING_IN_WHITELIST, TestCertificates.PEM_CERTIFICATE_VALID, CertificateExtractorMock.SENDER_VALID);
    }

    @DirtiesContext
    @Test
    public void shouldAlwaysPassValidationWhenSenderEqualsTheOrgIdOfThisShsServer() {
        validator.validateSender(null, null, CertificateExtractorMock.SENDER_THAT_MATCHES_LOCAL_ORGID);
    }
}
