package tr.com.ahmetaltay.esign;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import tr.com.ahmetaltay.esign.dto.Certificate;
import tr.com.ahmetaltay.esign.dto.Terminal;
import tr.com.ahmetaltay.esign.dto.TerminalWithCertificates;
import tr.com.ahmetaltay.esign.util.ESignUtil;

/**
 * ESYA Api Test App
 *
 */
public class App {
	public static void main(String[] args) {
		System.out.println("-------------------Program Start-------------------");
			
		String loggingPropertiesFile = App.class.getClassLoader().getResource("logging.properties").getFile();
		System.setProperty("java.util.logging.config.file", loggingPropertiesFile);
		Logger logger = LoggerFactory.getLogger(App.class);
		
		logger.info("Test info log level");
		logger.debug("Test debug log level");
		logger.warn("Test warn log level");
		logger.error("Test error log level");
		logger.trace("Test trace log level");

		ESignUtil eSignUtil = new ESignUtil();
		try {
			eSignUtil.initFilesAndFolders();
		} catch (IOException e) {
			logger.error(e.toString());
		}		
		
		try {
			Gson gson = new GsonBuilder()
					.setPrettyPrinting()
					.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
					.serializeNulls()
					.disableHtmlEscaping()
					.create();

			
			logger.trace("-------------------SmartCardManager2 Start-------------------");
			SmartCardManager scm2 = new SmartCardManager();

			logger.trace("Terminal Listesi Özet");
			List<String> terminals = scm2.getTerminals();
			logger.debug(gson.toJson(terminals));

			logger.trace("Terminal Listesi Detaylı");
			List<Terminal> terminalsDetailed = scm2.getTerminalsDetailed();
			logger.debug(gson.toJson(terminalsDetailed));	
			
			logger.trace("Terminal ve Sertifika Listesi Detaylı");
			List<TerminalWithCertificates> terminalWithCerts = scm2.getTerminalsWithCertificates();
			logger.debug(gson.toJson(terminalWithCerts));				
			
			logger.trace("Sertifika Listesi");
			Certificate cert = null;
			for (String terminal : terminals) {
				List<Certificate> certList = scm2.getCertificates(terminal);
				cert = certList.get(0);
				logger.debug(gson.toJson(certList));
			}
			logger.trace("-------------------SmartCardManager2 End-------------------");
		
			
			logger.trace("-------------------Certificate Validation Start-------------------");
			CertValidator certVal = new CertValidator();
			try {
				certVal.validate(terminals.get(0), cert.SerialNumber);
				logger.trace("Certificate Valid");
			} catch (Exception e) {
				logger.error(e.toString());
			}				
			logger.trace("-------------------Certificate Validation End-------------------");
			
			
			logger.trace("-------------------XML Signing Start-------------------");
			XmlSigner signer = new XmlSigner();
			
			String xml = IOUtils.resourceToString("/esya/test/ImzasizRecete.xml", StandardCharsets.UTF_8);		
			logger.trace("Unsigned XML");
			logger.debug(xml);
			
			logger.trace("Signing XML");
			String signedXml = signer.signBes(xml, terminals.get(0), cert.SerialNumber, "12345");
			
			logger.trace("Signed XML");
			logger.debug(signedXml);
			logger.trace("-------------------XML Signing End-------------------");
		
		} catch (Exception e) {
			logger.error(e.toString());
		}
		System.out.println();

		System.out.println("-------------------Program End-------------------");
	}
}
