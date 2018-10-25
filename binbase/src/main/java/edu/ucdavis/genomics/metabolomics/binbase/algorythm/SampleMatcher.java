package edu.ucdavis.genomics.metabolomics.binbase.algorythm;

import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;

/**
 * simplistic sample matching interface as preparation to move core parts of the algorithm over to a multiprocessor based version
 * @author wohlgemuth
 *
 */
public interface SampleMatcher {

	/**
	 * adds a sample to calculate
	 * @param sampleId
	 */
	public void addSampleToCalculate(Integer sampleId);
	
	/**
	 * the workload of this matcher
	 * @return
	 */
	public Integer getWorkLoad();
	
	/**
	 * matches all samples  which are registered
	 * @throws BinBaseException 
	 */
	public void matchSamples() throws BinBaseException;
	
	/**
	 * connection identifier
	 * @param name
	 */
	public void setConnectionIdentifier(String name);
}
