package tr.com.ahmetaltay.esign;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.security.pkcs11.wrapper.PKCS11Exception;
import tr.com.ahmetaltay.esign.dto.Certificate;
import tr.com.ahmetaltay.esign.dto.Terminal;
import tr.com.ahmetaltay.esign.dto.TerminalWithCertificates;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;
import tr.gov.tubitak.uekae.esya.api.common.util.StringUtil;
import tr.gov.tubitak.uekae.esya.api.common.util.bag.Pair;
import tr.gov.tubitak.uekae.esya.api.smartcard.apdu.APDUSmartCard;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.BaseSmartCard;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.CardType;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.P11SmartCard;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.SmartCardException;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.SmartOp;

/**
 * Akilli kart yoneticisi
 * @author ahmet
 */
public class SmartCardManager {

	private static final Logger logger = LoggerFactory.getLogger(SmartCardManager.class);

	public BaseSmartCard getSmartCard(String aTerminalName) throws SmartCardException, PKCS11Exception, IOException {
		BaseSmartCard bsc;
		if (APDUSmartCard.isSupported(aTerminalName)) {
			APDUSmartCard asc = new APDUSmartCard();
			CardTerminal ct = TerminalFactory.getDefault().terminals().getTerminal(aTerminalName);
			asc.openSession(ct);
			bsc = asc;
		} else {
			Pair<Long, CardType> slotAndCardType = SmartOp.getSlotAndCardType(aTerminalName);
			bsc = new P11SmartCard(slotAndCardType.getObject2());
			bsc.openSession(slotAndCardType.getObject1());
		}
		return bsc;
	}

	public List<String> getTerminals() throws SmartCardException {
		String[] terminals = SmartOp.getCardTerminals();
		if (terminals.length == 0)
			throw new SmartCardException("Terminal listesi boş. Takılı USB bulunamadı!");
		return Arrays.asList(terminals);
	}

	public List<Terminal> getTerminalsDetailed() throws SmartCardException, PKCS11Exception, IOException {
		List<String> terminals = getTerminals();
		List<Terminal> result = new ArrayList<>();
		for (String terminalName : terminals) {
			BaseSmartCard bsc = getSmartCard(terminalName);
			try {
				Terminal terminal = new Terminal();
				terminal.Name = terminalName;
				terminal.SerialNumber = StringUtil.toString(bsc.getSerial());
				result.add(terminal);
			} finally {
				if (bsc.isSessionActive())
					bsc.closeSession();
			}
		}
		return result;
	}

	public List<TerminalWithCertificates> getTerminalsWithCertificates()
			throws PKCS11Exception, IOException, ESYAException {
		List<String> terminals = getTerminals();
		List<TerminalWithCertificates> result = new ArrayList<>();
		for (String terminalName : terminals) {
			BaseSmartCard bsc = getSmartCard(terminalName);
			try {
				TerminalWithCertificates terminal = new TerminalWithCertificates();
				terminal.Name = terminalName;
				terminal.SerialNumber = StringUtil.toString(bsc.getSerial());
				terminal.Certificates = getCertificates(bsc);
				result.add(terminal);
			} finally {
				if (bsc.isSessionActive())
					bsc.closeSession();
			}
		}
		return result;
	}

	private List<Certificate> getCertificates(BaseSmartCard aSmartCard)
			throws PKCS11Exception, IOException, ESYAException {
		List<byte[]> certBytes = aSmartCard.getSignatureCertificates();
		List<Certificate> certs = new ArrayList<>();
		for (byte[] bs : certBytes) {
			ECertificate eCert = new ECertificate(bs);
			Certificate cert = new Certificate();
			cert.SerialNumber = eCert.getSerialNumber();
			cert.SubjectFull = eCert.getSubject().stringValue();
			cert.IssuerFull = eCert.getIssuer().stringValue();
			cert.IsCACertificate = eCert.isCACertificate();
			cert.IsMaliMuhurCertificate = eCert.isMaliMuhurCertificate();
			cert.IsOCSPSigningCertificate = eCert.isOCSPSigningCertificate();
			cert.IsQualifiedCertificate = eCert.isQualifiedCertificate();
			cert.IsSelfIssued = eCert.isSelfIssued();
			cert.IsTimeStampingCertificate = eCert.isTimeStampingCertificate();
			cert.SignatureAlgorithmName = eCert.asX509Certificate().getSigAlgName();
			cert.Version = eCert.getVersionStr();
			try {
				String[] splitIssuer = cert.IssuerFull.split(",");
				for (String x : splitIssuer) {
					if (x.contains("CN=")) {
						cert.IssuerCertificateName = x.trim().substring(3);
					}
					if (x.contains("OU=")) {
						cert.IssuerOwnerUnit = x.trim().substring(3);
					}
					if (x.contains("O=")) {
						cert.IssuerOwner = x.trim().substring(2);
					}
					if (x.contains("C=")) {
						cert.IssuerCountry = x.trim().substring(2);
					}
				}
			} catch (Exception e) {
				logger.warn("Certificate Issuer parse işleminde problem oluştu.\n" + e.toString());
			}
			try {
				String[] splitSubject = cert.SubjectFull.split(",");
				for (String x : splitSubject) {
					if (x.contains("CN=")) {
						cert.SubjectCertificateName = x.trim().substring(3);
					}
					if (x.contains("SERIALNUMBER=")) {
						cert.SubjectSerialNumber = x.trim().substring(13);
					}
					if (x.contains("C=")) {
						cert.SubjectCountry = x.trim().substring(2);
					}
				}
			} catch (Exception e) {
				logger.warn("Certificate Subject parse işleminde problem oluştu.\n" + e.toString());
			}
			certs.add(cert);
		}
		return certs;
	}	
	
	public List<Certificate> getCertificates(String aTerminalName) throws PKCS11Exception, IOException, ESYAException {
		BaseSmartCard bsc = getSmartCard(aTerminalName);
		try {
			return getCertificates(bsc);
		} finally {
			if (bsc.isSessionActive())
				bsc.closeSession();
		}
	}
	
	public ECertificate getECertificate(BaseSmartCard aBaseSmartCard, BigInteger aCertSerial) throws ESYAException {
		List<byte[]> eCerts = aBaseSmartCard.getSignatureCertificates();
		ECertificate eCert = null;
		for (byte[] bs : eCerts) {
			eCert = new ECertificate(bs);
			if (eCert.getSerialNumber().equals(aCertSerial)) {
				break;
			}
		}
		if (eCert == null)
			throw new SmartCardException("Sertifika bulunamadı!");
		return eCert;
	}
}
