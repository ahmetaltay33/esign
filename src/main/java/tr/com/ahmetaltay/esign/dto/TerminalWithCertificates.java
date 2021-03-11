package tr.com.ahmetaltay.esign.dto;

import java.util.List;

import lombok.Data;

/**
 * @author ahmet
 *
 */
public @Data class TerminalWithCertificates extends Terminal {
	public List<Certificate> Certificates;
}
