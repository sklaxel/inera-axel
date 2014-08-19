package se.inera.axel.riv.insuranceprocess.healthreporting.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;
import se.inera.ifv.v2.ResultCodeEnum;
import se.inera.ifv.v2.ResultOfCall;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

@WebService(endpointInterface = "se.inera.ifv.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface", targetNamespace = "urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificate:3:rivtabp20", name = "RegisterMedicalCertificateResponderInterface")
@XmlSeeAlso({se.inera.ifv.registermedicalcertificateresponder.v3.ObjectFactory.class, org.w3.wsaddressing10.ObjectFactory.class, se.inera.mu7263.v3.ObjectFactory.class, se.inera.ifv.v2.ObjectFactory.class, iso.v21090.dt.v1.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class RegisterMedCertImpl implements RegisterMedicalCertificateResponderInterface {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public RegisterMedicalCertificateResponseType registerMedicalCertificate(
			AttributedURIType logicalAddress,
			RegisterMedicalCertificateType parameters) {
		try {
			logger.debug("Received call not validating!");
			RegisterMedicalCertificateResponseType response = new RegisterMedicalCertificateResponseType();
						
			String logiskAdress = logicalAddress.getValue();
			String name = "dummy";
			// Check if we should throw some kind of exception or simulate a timeout.
			if (    parameters != null && parameters.getLakarutlatande() != null && 
					parameters.getLakarutlatande().getPatient() != null && 
					parameters.getLakarutlatande().getPatient().getFullstandigtNamn() != null &&
					parameters.getLakarutlatande().getPatient().getFullstandigtNamn().length() > 0) {

				name = parameters.getLakarutlatande().getPatient().getFullstandigtNamn();
			}
				
			if (name.equalsIgnoreCase("Error") || logiskAdress.contains("Error") ) {
				ResultOfCall result = new ResultOfCall();
				result.setResultCode(ResultCodeEnum.ERROR);
				response.setResult(result);
				logger.debug("Returned Error for not validating!");
			} else if (name.equalsIgnoreCase("Exception") || logiskAdress.contains("Exception")) {
				logger.debug("Returned Exception for not validating!");
				throw new RuntimeException("Exception called");
			} else if (name.equalsIgnoreCase("Timeout") || logiskAdress.contains("Timeout")) {
				logger.debug("Returned Timeout for not validating!");
				Thread.currentThread();
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				ResultOfCall resCall = new ResultOfCall();
				resCall.setResultCode(ResultCodeEnum.OK);
				response.setResult(resCall);				
				logger.debug("Returned OK for not validating!");
			}
			
			return response;
		} catch (RuntimeException e) {
			throw e;
		}
	}
}