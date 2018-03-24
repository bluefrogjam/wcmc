/*
 * Created on Jan 20, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.bci.export;

import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.io.dest.Destination;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;

/**
 * is the definition of a method to export data
 * 
 * @author wohlgemuth
 * @version Jan 20, 2006
 * 
 */
public interface ExportService {
	/**
	 * exports the given experiment
	 * 
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @param exp
	 * @return string containing url to the file
	 */
	public String export(String name, String setupXID) throws BinBaseException;

	/**
	 * exports the experiment by the internal experiment id
	 * 
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @param experimentID
	 *            the internal id of the result
	 * @return string containing url to the file
	 * @throws BinBaseException
	 */
	public String export(String name, int experimentID) throws BinBaseException;

	/**
	 * exports the given experiment and transforms it by
	 * the given sop
	 * 
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @param exp
	 * @param sop the sop which should be used for transformations
	 * @return string containing url to the file
	 */
	public String export(String name, String setupXID, Source sop) throws BinBaseException;

	/**
	 * exports the experiment by the internal experiment id and transforms it by
	 * the given sop
	 * 
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @param experimentID
	 *            the internal id of the result
	 * @param sop the sop which should be used for transformations
	 * @return string containing url to the file
	 * @throws BinBaseException
	 */
	public String export(String name, int experimentID, Source sop, Destination destination) throws BinBaseException;
	public String export(String name, int experimentID, Source sop, Destination destination, boolean overwrite) throws BinBaseException;

}
