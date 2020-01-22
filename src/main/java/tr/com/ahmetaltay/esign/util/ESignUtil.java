/**
 * 
 */
package tr.com.ahmetaltay.esign.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * ESign için kullanılacak genel araçlardır.
 * 
 * @author ahmet
 */
public class ESignUtil {
	public static final String ROOT_DIR = Paths.get("").toAbsolutePath().toString();
	public static final String ESYA_FOLDER = FilenameUtils.concat(ROOT_DIR, "esya");
	public static final String ESYA_CONFIG_FOLDER = FilenameUtils.concat(ESYA_FOLDER, "config");
	public static final String ESYA_CERTSTORE_FOLDER = FilenameUtils.concat(ESYA_FOLDER, "certstore");
	public static final String ESYA_TRUSTED_FOLDER = FilenameUtils.concat(ESYA_CERTSTORE_FOLDER, "trusted");
	public static final String ESYA_CERTVAL_POLICY_FILE = FilenameUtils.concat(ESYA_CONFIG_FOLDER, "certval-policy.xml");
	public static final String ESYA_CERTVAL_POLICY_MALIMUHUR_FILE = FilenameUtils.concat(ESYA_CONFIG_FOLDER, "certval-policy-malimuhur.xml");
	public static final String ESYA_XMLSIGNATURE_CONFIG_FILE = FilenameUtils.concat(ESYA_CONFIG_FOLDER, "xmlsignature-config.xml");
	public static final String ESYA_SMARTCARD_CONFIG_FILE = FilenameUtils.concat(ESYA_CONFIG_FOLDER, "smartcard-config.xml");
	public static final String ESYA_CERTSTORE_FILE = FilenameUtils.concat(ESYA_CERTSTORE_FOLDER, "SertifikaDeposu.svt");	
	public static final String ESYA_LISANS_FILE = FilenameUtils.concat(ESYA_FOLDER, "lisans.xml");	

	public static URL getResourceURL(String aResourceName) throws IOException {
		return IOUtils.resourceToURL(aResourceName);
	}

	public static String getResourceString(String aResourceName) throws IOException {
		return IOUtils.resourceToString(aResourceName, StandardCharsets.UTF_8);
	}

	private void createLisansXml() throws IOException {
		FileUtils.copyURLToFile(getResourceURL("/esya/lisans.xml"), new File(ESYA_LISANS_FILE));
	}
	
	private void createCertStore() throws IOException {
		FileUtils.copyURLToFile(getResourceURL("/esya/certstore/SertifikaDeposu.svt"), new File(ESYA_CERTSTORE_FILE));
	}
	
	private void createTrustedCerts() throws IOException {
		FileUtils.copyURLToFile(getResourceURL("/esya/certstore/trusted/kokshs384.t6.crt"), new File(FilenameUtils.concat(ESYA_TRUSTED_FOLDER, "kokshs384.t6.crt")));
		FileUtils.copyURLToFile(getResourceURL("/esya/certstore/trusted/test-kokhs-sha256.crt"), new File(FilenameUtils.concat(ESYA_TRUSTED_FOLDER, "test-kokhs-sha256.crt")));
	}

	private void createSmartCardConfigXml() throws IOException {
		FileUtils.copyURLToFile(getResourceURL("/esya/config/smartcard-config.xml"), new File(ESYA_SMARTCARD_CONFIG_FILE));
	}	
	
	private void createCertvalPolicyXml() throws IOException {
		String xml = getResourceString("/esya/config/certval-policy.xml");
		xml = xml.replace("@certStoreFile", ESYA_CERTSTORE_FILE)
				 .replace("@trustedfolder", ESYA_TRUSTED_FOLDER);
		FileUtils.writeStringToFile(new File(ESYA_CERTVAL_POLICY_FILE), xml, StandardCharsets.UTF_8);
	}
	
	private void createCertvalPolicyMalimuhurXml() throws IOException {
		String xml = getResourceString("/esya/config/certval-policy-malimuhur.xml");
		xml = xml.replace("@certStoreFile", ESYA_CERTSTORE_FILE)
				 .replace("@trustedfolder", ESYA_TRUSTED_FOLDER);
		FileUtils.writeStringToFile(new File(ESYA_CERTVAL_POLICY_MALIMUHUR_FILE), xml, StandardCharsets.UTF_8);
	}

	private void createXmlsignatureConfigXml() throws IOException {
		String xml = getResourceString("/esya/config/xmlsignature-config.xml");
		xml = xml.replace("certval-policy-test.xml", ESYA_CERTVAL_POLICY_FILE)
				 .replace("certval-policy-malimuhur.xml", ESYA_CERTVAL_POLICY_MALIMUHUR_FILE);
		FileUtils.writeStringToFile(new File(ESYA_XMLSIGNATURE_CONFIG_FILE), xml, StandardCharsets.UTF_8);
	}

	public void initFilesAndFolders() throws IOException {
		FileUtils.forceMkdir(new File(ESYA_CONFIG_FOLDER));
		FileUtils.forceMkdir(new File(ESYA_CERTSTORE_FOLDER));
		FileUtils.forceMkdir(new File(ESYA_TRUSTED_FOLDER));
		createLisansXml();
		createCertStore();
		createTrustedCerts();
		createSmartCardConfigXml();
		createCertvalPolicyXml();
		createCertvalPolicyMalimuhurXml();
		createXmlsignatureConfigXml();
	}
}
