package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.anotation;

import java.util.Map;

import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.Diagnostics;

/**
 * a standard binbase fitler definition
 * @author wohlgemuth
 *
 */
public interface Filter extends Diagnostics{
	/**
	 * the filter method
	 * @param bin the bin we want to compare to
	 * @param unknown the massspec we want to compare to 
	 * @return true if the unknown match the bin, false if the unknown does not matches the bin
	 * @throws MatchingException 
	 */
	public boolean compareTo(Map<String, Object> bin, Map<String, Object> unknown) throws MatchingException;

	/**
	 * why was this spectra rejected
	 * @return
	 */
	public String getReasonForRejection();
	
	public void setReasonForRejection(String reason);
}
