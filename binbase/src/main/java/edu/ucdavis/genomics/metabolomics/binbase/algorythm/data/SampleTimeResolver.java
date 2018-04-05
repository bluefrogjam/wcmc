package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data;

import java.io.Serializable;

import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;

public interface SampleTimeResolver extends Serializable {

	/**
	 * calculates the time for ur sample
	 * @param sample
	 * @return
	 * @throws BinBaseException 
	 */
	public long resolveTime(String sample) throws BinBaseException;
}
