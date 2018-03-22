package edu.ucdavis.genomics.metabolomics.sjp.transform;

/**
 * used to transform an entry
 * @author wohlgemuth
 *
 */
public interface Transformer<O> {

	/**
	 * trnasforms the given object
	 * @param toTransform
	 * @return
	 */
	public O transform(O toTransform);
}
