package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.anotation;

import java.util.Map;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.anotation.logger.PrefixedLogger;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsService;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsServiceFactory;

/**
 * basic filter all classes should inherit from it
 * 
 * @author wohlgemuth
 * 
 */
public abstract class BasicFilter implements Filter {
	private String reason;

	public String getReasonForRejection() {
		return reason;
	}

	public void setReasonForRejection(String reason) {
		this.reason = reason;
	}

	String bin = "";

	protected PrefixedLogger logger = PrefixedLoggerFactory.getLogger(getClass());

	/**
	 * so we can log on what bin we are working with
	 */
	public final boolean compareTo(Map<String, Object> binmap,
			Map<String, Object> unknown) throws MatchingException {
		try {
			if (logger.isDebugEnabled()) {
				this.bin = binmap.get("name").toString();
				logger.setLoggingPrefix(bin, binmap.get("spectra_id"), unknown
						.get("spectra_id").toString());
			}
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
		}
		return compare(binmap, unknown);
	}

	/**
	 * compares a to b
	 * 
	 * @param bin
	 * @param unknown
	 * @return
	 * @throws MatchingException
	 */
	protected abstract boolean compare(Map<String, Object> bin,
			Map<String, Object> unknown) throws MatchingException;

	@Override
	public final String toString() {
		return getClass().getSimpleName();
	}

	protected boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	@Override
	public DiagnosticsService getDiagnosticsService() {
		return service;
	}

	private DiagnosticsService service = DiagnosticsServiceFactory
			.newInstance().createService();
}
