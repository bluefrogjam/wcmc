package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.action;

import java.util.Collection;
import java.util.List;

import org.jdom2.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.Describeable;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;

/**
 * executes the methods
 * @author wohlgemuth
 *
 */
public interface Action extends Describeable{

	/**
	 * 
	 * @param configuration configuration for this action
	 * @param rawdata the rawdata
	 * @param id
	 * @param destiantionIds
	 * @param sop
	 */
	public void run(Element configuration, Source rawdata, Source sop);

	/**
	 * returns the folder where the result is supposed to be stored
	 * 
	 * @return
	 */
	public String getFolder();

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
	 * sets the column to use
	 * @param column
	 */
	public void setColumn(String column);

	/**
	 * all transform instrutions in this sop
	 * @param transformInstructions
	 */
	public void setTransformInstructions(List<Element> transformInstructions);
	
}
