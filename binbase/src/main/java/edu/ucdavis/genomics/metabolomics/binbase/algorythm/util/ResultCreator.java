package edu.ucdavis.genomics.metabolomics.binbase.algorythm.util;

/**
 * used to create a result in the database
 */
public interface ResultCreator {

	/**
	 * creates the result description
	 * 
	 * @param exp
	 * @throws Exception
	 */
	public abstract int createResultDefinition(Experiment exp) throws Exception;

	/**
	 * removes the result description
	 * 
	 * @param exp
	 * @throws Exception
	 */
	public abstract void dropResult(int resultId) throws Exception;

	/**
	 * makes sure that the experiment is actually ready for export
	 * 
	 * @param exp
	 * @return
	 * @throws Exception
	 */
	public abstract boolean readyForExport(Experiment exp) throws Exception;
}
