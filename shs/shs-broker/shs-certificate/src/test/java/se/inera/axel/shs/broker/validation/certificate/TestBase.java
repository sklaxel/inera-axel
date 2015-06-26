package se.inera.axel.shs.broker.validation.certificate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.testng.annotations.BeforeClass;

public abstract class TestBase {
    
    public static final String CERTIFICATE_FILE_NAME_VALID = "certificates/transportstyrelsen.crt";
    public static final String CERTIFICATE_FILE_NAME_INVALID = "certificates/invalidCertificate.crt";
    public static final String SENDER_IN_VALID_CERTIFICATE = "165563372191";

    protected String PEM_CERTIFICATE_VALID;
    protected String PEM_CERTIFICATE_INVALID;

    protected X509Certificate X509_CERTIFICATE_VALID;

    @BeforeClass
    public void beforeClass() throws IOException, URISyntaxException,
            CertificateException {

        PEM_CERTIFICATE_VALID = createPemCertificateFromFile(CERTIFICATE_FILE_NAME_VALID);
        X509_CERTIFICATE_VALID = PemConverter.convertPemToX509Certificate(PEM_CERTIFICATE_VALID);

        PEM_CERTIFICATE_INVALID = createPemCertificateFromFile(CERTIFICATE_FILE_NAME_INVALID);
    }

    private String createPemCertificateFromFile(String fileName) throws IOException, URISyntaxException {
        byte[] validBytes = Files.readAllBytes(Paths.get(getClass()
                .getClassLoader()
                .getResource(fileName).toURI()));
        return new String(validBytes, Charset.forName("UTF-8"));
    }
}
