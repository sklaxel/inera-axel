package se.inera.axel.shs.broker.validation.certificate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.security.cert.CertificateException;
import java.util.regex.Pattern;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class CertificateExtractorImplTest extends TestBase {
    
    private static final String SENDER_PATTERN_INVALID = "2.5.4.5=([^,]+)";
    private static final String SENDER_PATTERN_VALID = "2.5.4.5=([^+]+)";
    
    private CertificateExtractorImpl extractor;
    private CertificateExtractorImpl extractorWithInvalidPattern;

    @BeforeTest
    public void beforeTest() {
        Pattern validPattern = Pattern.compile(SENDER_PATTERN_VALID);
        extractor = new CertificateExtractorImpl(validPattern);

        Pattern invalidPattern = Pattern.compile(SENDER_PATTERN_INVALID);
        extractorWithInvalidPattern = new CertificateExtractorImpl(invalidPattern);
    }

    @Test
    public void shouldExtractSender() throws CertificateException {
        String sender = extractor.extractSender(PEM_CERTIFICATE_VALID);
        assertThat(sender, equalTo(SENDER_IN_VALID_CERTIFICATE));
    }

    @Test(expectedExceptions = CertificateException.class)
    public void shouldThrowExceptionWhenParsingInvalidCertificateWithValidPattern() throws CertificateException {
        extractor.extractSender(PEM_CERTIFICATE_INVALID);
    }

    @Test(expectedExceptions = CertificateException.class)
    public void shouldThrowExceptionWhenParsingValidCertificateWithInvalidPattern() throws CertificateException {
        extractorWithInvalidPattern.extractSender(PEM_CERTIFICATE_VALID);
    }
}
