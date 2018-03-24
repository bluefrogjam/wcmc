/*
 * Created on May 5, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.result;

import java.util.Map;

/**
 * other sql statementz
 * 
 * @author wohlgemuth
 * @version May 5, 2006
 * 
 */
public class ProblematicResultHandler extends DatabaseResultHandler {

	/**
	 * cant create new bins
	 * 
	 * @author wohlgemuth
	 * @version May 5, 2006
	 * @see DatabaseResultHandler#newBin(Map)
	 */
	public void newBin(Map spectra) throws Exception {
		this.getValues(spectra);
		getDiagnosticsService()
				.diagnosticActionFailed(
						id,
						bin_id,
						this.getClass(),
						"create new bin",
						"this handlers implementation is not allowed to create new bins",
						new Object[] {});

	}

}
