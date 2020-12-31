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
		System.out.println("Program Start");
			
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

			System.out.println("SmartCardManager2 Start");
			SmartCardManager scm2 = new SmartCardManager();

			System.out.println();
			System.out.println("Terminal Listesi Özet");
			List<String> terminals = scm2.getTerminals();
			System.out.println(gson.toJson(terminals));

			System.out.println();
			System.out.println("Terminal Listesi Detaylı");
			List<Terminal> terminalsDetailed = scm2.getTerminalsDetailed();
			System.out.println(gson.toJson(terminalsDetailed));	
			
			System.out.println();
			System.out.println("Terminal ve Sertifika Listesi Detaylı");
			List<TerminalWithCertificates> terminalWithCerts = scm2.getTerminalsWithCertificates();
			System.out.println(gson.toJson(terminalWithCerts));				
			
			System.out.println();
			System.out.println("Sertifika Listesi");
			Certificate cert = null;
			for (String terminal : terminals) {
				List<Certificate> certList = scm2.getCertificates(terminal);
				cert = certList.get(0);
				System.out.println(gson.toJson(certList));
				System.out.println();
			}
			System.out.println("SmartCardManager2 End");
		
			XmlSigner signer = new XmlSigner();
			
			String xml = IOUtils.resourceToString("/esya/test/ImzasizRecete.xml", StandardCharsets.UTF_8);		
			System.out.println("Unsigned XML");
			System.out.println(xml);
			
			System.out.println("Signing XML");
			String signedXml = signer.signBes(xml, terminals.get(0), cert.SerialNumber, "12345");
			
			System.out.println("Signed XML");
			System.out.println(signedXml);
		
		} catch (Exception e) {
			logger.error(e.toString());
		}
		System.out.println();

		System.out.println("Program End");
	}
}
