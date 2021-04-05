package tr.com.ahmetaltay.esign.util;

public final class SysUtils {

	  public static String javaRuntimeName() {
	    return System.getProperty("java.runtime.name");
	  }  
	  
	  public static String javaVendor() {
	    return System.getProperty("java.vendor");
	  }  

	  public static String javaVersion() {
	    return System.getProperty("java.version");
	  }

	  public static String javaHome() {
	    return System.getProperty("java.home");
	  }
	  
	  public static String javaFxVersion() {
	    return System.getProperty("javafx.version");
	  }  

	  public static String osVersion() {
	    return System.getProperty("os.version");
	  }

	  public static String osArch() {
	    return System.getProperty("os.arch");
	  }  

	  public static String osName() {
	    return System.getProperty("os.name");
	  }  
	  
	  public static String userName() {
	    return System.getProperty("user.name");
	  }  

	  public static String userLanguage() {
	    return System.getProperty("user.language");
	  }  
	  
	  public static String userHome() {
	    return System.getProperty("user.home");
	  }    

	  public static String userTimezone() {
	    return System.getProperty("user.timezone");
	  }    
	  
	  public static String tmpDir() {
	    return System.getProperty("java.io.tmpdir");
	  }  
	}

