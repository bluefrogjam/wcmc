package edu.ucdavis.genomics.metabolomics.sjp.transform;

/**
 * converts DB5 RI's to the RTX5 RI's
 * @author wohlgemuth
 *
 */
public class AdamsDB5ToRTX5Transformator implements Transformer<Double>{

	/**
	 * applies a simple formula to transform the data
	 */
	public Double transform(Double toTransform) {
		return 0.9613 * toTransform + 131.24;
	}

}
