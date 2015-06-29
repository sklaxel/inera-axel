package se.inera.axel.shs.broker.validation.certificate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.security.cert.CertificateException;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class CertificateExtractorImplTest {
    
    private static final String SENDER_PATTERN_VALID = "2.5.4.5=([^+]+)";
    private static final String SENDER_PATTERN_INVALID = "XXX=([^+]+)";
    
    private CertificateExtractorImpl extractor;

    @BeforeTest
    public void beforeTest() {
        extractor = new CertificateExtractorImpl(SENDER_PATTERN_VALID);
    }

    @Test
    public void shouldExtractSender() throws CertificateException {
        String sender = extractor.extractSender(TestCertificates.PEM_CERTIFICATE_VALID);
        assertThat(sender, equalTo(TestCertificates.SENDER_IN_VALID_CERTIFICATE));
    }

    @Test(expectedExceptions = CertificateException.class)
    public void shouldThrowExceptionWhenParsingInvalidCertificateWithValidPattern() throws CertificateException {
        extractor.extractSender(TestCertificates.PEM_CERTIFICATE_WITH_INVALID_SENDER);
    }

    @Test(expectedExceptions = CertificateException.class)
    public void shouldThrowExceptionWhenParsingValidCertificateWithInvalidPattern() throws CertificateException {
        CertificateExtractorImpl extractorWithInvalidPattern = new CertificateExtractorImpl(SENDER_PATTERN_INVALID);
        extractorWithInvalidPattern.extractSender(TestCertificates.PEM_CERTIFICATE_VALID);
    }
}
