package se.inera.axel.ssek.impl;

import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.registermedicalcertificateresponder.v3.ObjectFactory;
import se.inera.ifv.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;

import javax.jws.WebService;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
@WebService(endpointInterface = "se.inera.ifv.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface")
public class RegisterMedicalCertificateImpl implements RegisterMedicalCertificateResponderInterface {
    @Override
    public RegisterMedicalCertificateResponseType registerMedicalCertificate(RegisterMedicalCertificateType parameters) {
        ObjectFactory objectFactory = new ObjectFactory();
        return objectFactory.createRegisterMedicalCertificateResponseType();
    }
}
