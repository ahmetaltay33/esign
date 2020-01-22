/**
 * 
 */
package tr.com.ahmetaltay.esign.dto;

import java.math.BigInteger;

import lombok.Data;

/**
 * @author Ahmet
 *
 */
public @Data class Certificate {
	public BigInteger SerialNumber;
	public String IssuerFull;
	public String IssuerCertificateName;
	public String IssuerOwner;
	public String IssuerOwnerUnit;
	public String IssuerCountry;
	public String SubjectFull;
	public String SubjectCertificateName;
	public String SubjectCountry;
	public String SubjectSerialNumber;
	public String SignatureAlgorithmName;
	public boolean IsCACertificate;
	public boolean IsMaliMuhurCertificate;
	public boolean IsOCSPSigningCertificate;
	public boolean IsQualifiedCertificate;
	public boolean IsSelfIssued;
	public boolean IsTimeStampingCertificate;
	public String Version;
}
