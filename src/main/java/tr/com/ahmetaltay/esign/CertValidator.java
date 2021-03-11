package tr.com.ahmetaltay.esign;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;

import javax.xml.bind.ValidationException;

import sun.security.pkcs11.wrapper.PKCS11Exception;
import tr.com.ahmetaltay.esign.util.ESignUtil;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.certificate.validation.CertificateStatus;
import tr.gov.tubitak.uekae.esya.api.certificate.validation.CertificateValidation;
import tr.gov.tubitak.uekae.esya.api.certificate.validation.ValidationSystem;
import tr.gov.tubitak.uekae.esya.api.certificate.validation.check.certificate.CertificateStatusInfo;
import tr.gov.tubitak.uekae.esya.api.certificate.validation.policy.PolicyReader;
import tr.gov.tubitak.uekae.esya.api.certificate.validation.policy.ValidationPolicy;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;
import tr.gov.tubitak.uekae.esya.api.common.util.LicenseUtil;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.BaseSmartCard;

/**
 * Sertifila Doðrulayýcý
 * 
 * @author ahmet
 */
public class CertValidator {
	
	public void validate(String aTerminalName, BigInteger aCertSerial) throws PKCS11Exception, IOException, ESYAException, ValidationException
	{
		try (FileInputStream fs = new FileInputStream(ESignUtil.ESYA_LISANS_FILE)) {
			LicenseUtil.setLicenseXml(fs);
		}
		SmartCardManager scm = new SmartCardManager();
		BaseSmartCard bsc = scm.getSmartCard(aTerminalName);
		try {
			ECertificate cert = scm.getECertificate(bsc, aCertSerial);
			ValidationPolicy policy = PolicyReader.readValidationPolicy(ESignUtil.ESYA_CERTVAL_POLICY_FILE);
		    ValidationSystem vs = CertificateValidation.createValidationSystem(policy);
		    vs.setBaseValidationTime(Calendar.getInstance());
		    
		    CertificateStatusInfo inf = CertificateValidation.validateCertificate(vs, cert);
		    String detail = inf.getDetailedMessage();
		    CertificateStatus status = inf.getCertificateStatus();
		    
		    switch (status) {
		      case REVOCATION_CHECK_FAILURE: 
		        throw new ValidationException(detail, status.name());
		      case CERTIFICATE_SELF_CHECK_FAILURE:
		        throw new ValidationException(detail, status.name());
		      case NO_TRUSTED_CERT_FOUND:
		        throw new ValidationException(detail, status.name());
		      case PATH_VALIDATION_FAILURE:
		        throw new ValidationException(detail, status.name());
		      case NOT_CHECKED:
		        throw new ValidationException(detail, status.name());
			case VALID:
				break;
			default:
				break;
		    } 
		    
		} finally {
			if (bsc.isSessionActive())
				bsc.closeSession();
		}
	}
	
}
