package tr.com.ahmetaltay.esign;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import sun.security.pkcs11.wrapper.PKCS11Exception;
import tr.com.ahmetaltay.esign.util.ESignUtil;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;
import tr.gov.tubitak.uekae.esya.api.common.crypto.Algorithms;
import tr.gov.tubitak.uekae.esya.api.common.crypto.BaseSigner;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.BaseSmartCard;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.Context;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignature;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignatureException;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.config.Config;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.document.InMemoryDocument;

/**
 * XML imzalayici
 * 
 * @author ahmet
 */
public class XmlSigner {

	public String signBes(String aXml, String aTerminalName, BigInteger aCertSerial, String aPinCode)
			throws PKCS11Exception, IOException, ESYAException, XMLSignatureException {
		Context context = new Context();
		Config config = new Config(ESignUtil.ESYA_XMLSIGNATURE_CONFIG_FILE);
		context.setConfig(config);

		XMLSignature signature = new XMLSignature(context);
		byte[] xmlBuffer = aXml.getBytes(StandardCharsets.UTF_8);
		InMemoryDocument xmlDoc = new InMemoryDocument(xmlBuffer, "", "text/xml", StandardCharsets.UTF_8.name());
		signature.addDocument(xmlDoc);

		SmartCardManager scm = new SmartCardManager();
		BaseSmartCard bsc = scm.getSmartCard(aTerminalName);
		try {
			ECertificate cert = scm.getECertificate(bsc, aCertSerial);
			signature.addKeyInfo(cert);

			bsc.login(aPinCode);
			try {
				BaseSigner signer = bsc.getSigner(cert.asX509Certificate(), Algorithms.SIGNATURE_RSA_SHA256);
				signature.sign(signer);
			} finally {
				bsc.logout();
			}
			
			return new String(signature.write(), StandardCharsets.UTF_8);
		} finally {
			if (bsc.isSessionActive())
				bsc.closeSession();
		}
	}
	
	public void signBes(InputStream inStream, OutputStream outStream, String aTerminalName, BigInteger aCertSerial, String aPinCode, String docType)
			throws PKCS11Exception, IOException, ESYAException, XMLSignatureException {
		Context context = new Context();
		Config config = new Config(ESignUtil.ESYA_XMLSIGNATURE_CONFIG_FILE);
		context.setConfig(config);

		XMLSignature signature = new XMLSignature(context);
		byte[] buffer = new byte[inStream.available()];
		IOUtils.read(inStream, buffer);
		InMemoryDocument doc = new InMemoryDocument(buffer, "", docType, StandardCharsets.UTF_8.name());
		signature.addDocument(doc);

		SmartCardManager scm = new SmartCardManager();
		BaseSmartCard bsc = scm.getSmartCard(aTerminalName);
		try {
			ECertificate cert = scm.getECertificate(bsc, aCertSerial);
			signature.addKeyInfo(cert);

			bsc.login(aPinCode);
			try {
				BaseSigner signer = bsc.getSigner(cert.asX509Certificate(), Algorithms.SIGNATURE_RSA_SHA256);
				signature.sign(signer);
			} finally {
				bsc.logout();
			}
			
			signature.write(outStream);
		} finally {
			if (bsc.isSessionActive())
				bsc.closeSession();
		}
	}
}
