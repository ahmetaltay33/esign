package tr.com.ahmetaltay.esign;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Calendar;

import javax.print.attribute.DocAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.security.pkcs11.wrapper.PKCS11Exception;
import tr.com.ahmetaltay.esign.util.ESignUtil;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;
import tr.gov.tubitak.uekae.esya.api.common.crypto.Algorithms;
import tr.gov.tubitak.uekae.esya.api.common.crypto.BaseSigner;
import tr.gov.tubitak.uekae.esya.api.pades.PAdESContext;
import tr.gov.tubitak.uekae.esya.api.pades.PAdESSignature;
import tr.gov.tubitak.uekae.esya.api.pades.Position;
import tr.gov.tubitak.uekae.esya.api.pades.VisibleSignature;
import tr.gov.tubitak.uekae.esya.api.signature.ContainerValidationResult;
import tr.gov.tubitak.uekae.esya.api.signature.ContainerValidationResultType;
import tr.gov.tubitak.uekae.esya.api.signature.Signature;
import tr.gov.tubitak.uekae.esya.api.signature.SignatureContainer;
import tr.gov.tubitak.uekae.esya.api.signature.SignatureException;
import tr.gov.tubitak.uekae.esya.api.signature.SignatureFactory;
import tr.gov.tubitak.uekae.esya.api.signature.SignatureFormat;
import tr.gov.tubitak.uekae.esya.api.signature.config.Config;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.BaseSmartCard;

/**
 * PDF imzalayici
 * 
 * @author ahmet
 */
public class PdfSigner {

	private Logger logger = LoggerFactory.getLogger(PdfSigner.class);
	
	private PAdESContext createContext()
	{
		PAdESContext context = new PAdESContext();
		Config config = new Config(ESignUtil.ESYA_SIGNATURE_CONFIG_FILE);
		context.setConfig(config);
		return context;
	}
	
	private void sign(InputStream aPdf, OutputStream aSignedPdf, String aTerminalName, BigInteger aCertSerial, String aPinCode, SignatureFormat signatureFormat, boolean useTimestamp, boolean showSignatureOnPdf)
			throws PKCS11Exception, IOException, ESYAException, SignatureException 
	{
		PAdESContext context = createContext();

		if (useTimestamp)
			context.setSignWithTimestamp(true);
		
		SmartCardManager scm = new SmartCardManager();
		BaseSmartCard bsc = scm.getSmartCard(aTerminalName);
		try {
			ECertificate cert = scm.getECertificate(bsc, aCertSerial);
			SignatureContainer signatureContainer = SignatureFactory.readContainer(signatureFormat, aPdf, context);
			PAdESSignature signature = (PAdESSignature)signatureContainer.createSignature(cert);
			signature.setSigningTime(Calendar.getInstance());
			
			if (showSignatureOnPdf)
			{			
				// set visible position
		        VisibleSignature visibleSignature = new VisibleSignature();
		        visibleSignature.setPosition(new Position(1, 400 , 150 , 550, 80));
	
		        // set visible content
		        String visibleText = "Bu belge "+ cert.getSubject().getCommonNameAttribute() +" tarafından elektronik olarak imzalanmıştır.";
		        visibleSignature.setText(visibleText);
	
		        signature.setVisibleSignature(visibleSignature);
			}

			bsc.login(aPinCode);
			try {
				BaseSigner signer = bsc.getSigner(cert.asX509Certificate(), Algorithms.SIGNATURE_RSA_SHA256);
				signature.sign(signer);
			} finally {
				bsc.logout();
			}
			signatureContainer.write(aSignedPdf);
		} finally {
			if (bsc.isSessionActive())
				bsc.closeSession();
		}
	}
	
	private boolean validateSignedPdf(InputStream aPdf, SignatureFormat signatureFormat) throws SignatureException
	{
        SignatureContainer sc = SignatureFactory.readContainer(signatureFormat, aPdf, createContext());

        ContainerValidationResult validationResult = sc.verifyAll();
             
        logger.trace(validationResult.toString());
        logger.trace(validationResult.getResultType().name());
        
        return validationResult.getResultType() == ContainerValidationResultType.ALL_VALID;
	}

	/**
	 * @deprecated
	 * XAdES imzalama certval-policy.xml yükleme işleminde hata veriyor. Esya kaynaklı bir problm olabilir
	 */
	@Deprecated()
	public void signXAdES(InputStream aPdf, OutputStream aSignedPdf, String aTerminalName, BigInteger aCertSerial, String aPinCode, boolean useTimestamp, boolean showSignatureOnPdf)
			throws PKCS11Exception, IOException, ESYAException, SignatureException 
	{
		sign(aPdf, aSignedPdf, aTerminalName, aCertSerial, aPinCode, SignatureFormat.XAdES, useTimestamp, showSignatureOnPdf);
	}

	/**
	 * @deprecated
	 * CAdES imzalama certval-policy.xml yükleme işleminde hata veriyor. Esya kaynaklı bir problm olabilir
	 */
	@Deprecated()
	public void signCAdES(InputStream aPdf, OutputStream aSignedPdf, String aTerminalName, BigInteger aCertSerial, String aPinCode, boolean useTimestamp, boolean showSignatureOnPdf)
			throws PKCS11Exception, IOException, ESYAException, SignatureException 
	{
		sign(aPdf, aSignedPdf, aTerminalName, aCertSerial, aPinCode, SignatureFormat.CAdES, useTimestamp, showSignatureOnPdf);
	}

	public void signPAdES(InputStream aPdf, OutputStream aSignedPdf, String aTerminalName, BigInteger aCertSerial, String aPinCode, boolean useTimestamp, boolean showSignatureOnPdf)
			throws PKCS11Exception, IOException, ESYAException, SignatureException 
	{
		sign(aPdf, aSignedPdf, aTerminalName, aCertSerial, aPinCode, SignatureFormat.PAdES, useTimestamp, showSignatureOnPdf);
	}
	
	/**
	 * @deprecated
	 * XAdES imzalama certval-policy.xml yükleme işleminde hata veriyor. Esya kaynaklı bir problm olabilir
	 */
	public boolean validateXAdESSignedPdf(InputStream aPdf) throws SignatureException
	{
		return validateSignedPdf(aPdf, SignatureFormat.XAdES);
	}
	
	/**
	 * @deprecated
	 * CAdES imzalama certval-policy.xml yükleme işleminde hata veriyor. Esya kaynaklı bir problm olabilir
	 */
	public boolean validateCAdESSignedPdf(InputStream aPdf) throws SignatureException
	{
		return validateSignedPdf(aPdf, SignatureFormat.CAdES);
	}

	public boolean validatePAdESSignedPdf(InputStream aPdf) throws SignatureException
	{
		return validateSignedPdf(aPdf, SignatureFormat.PAdES);
	}
}
