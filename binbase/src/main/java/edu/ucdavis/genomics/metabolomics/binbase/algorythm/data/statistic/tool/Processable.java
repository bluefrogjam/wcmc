package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.tool;

import java.util.Collection;

import org.jdom2.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.ResultDataFile;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.Describeable;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;

/**
 * does something with a datafile
 * 
 * @author wohlgemuth
 */
public interface Processable extends Describeable {

	/**
	 * process the datafile as specified
	 * 
	 * @param datafile
	 * @return
	 * @throws BinBaseException
	 */
	public DataFile process(ResultDataFile datafile, Element configuration)
			throws BinBaseException;

	/**
	 * simple processing version
	 * @param datafile
	 * @param configuration
	 * @return
	 * @throws BinBaseExcpetion
	 */
	public DataFile simpleProcess(DataFile datafile, Element configuration) throws BinBaseException;

	/**
	 * returns the folder where the result is supposed to be stored
	 * 
	 * @return
	 */
	public String getFolder();

	/**
	 * returns the default identifier for this processable, which will be used
	 * to calculate the final filename
	 * 
	 * @return
	 */
	public String getFileIdentifier();

	/**
	 * the current folder we are in
	 * 
	 * @param folder
	 */
	public void setCurrentFolder(String folder);

	/**
	 * the current internal id
	 * 
	 * @param id
	 */
	public void setCurrentId(String id);

	/**
	 * destination ids
	 * 
	 * @param destinationIds
	 */
	public void setDestinationIds(Collection<String> destinationIds);

	/**
	 * should the result be written to the result directory
	 * 
	 * @return
	 */
	public boolean writeResultToFile();
}
