package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.AlgorythmHandler;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter.MassSpecFilter;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter.MassSpecModifier;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.result.ResultHandler;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.Diagnostics;

/**
 * <h3>
 * Title: Matchable</h3>
 * 
 * <p>
 * Author: Gert Wohlgemuth <br>
 * Leader: Dr. Oliver Fiehn <br>
 * Company: Max Plank Institute for molecular plant physologie <br>
 * Contact: wohlgemuth@mpimp-golm.mpg.de <br>
 * Version: <br>
 * Description: Interface for all Matching Algorythms
 * 
 * </p>
 */
public interface Matchable extends Diagnostics {
	/**
	 * DOCUMENT ME!
	 */
	int ASSIGNED_BIN = 1;

	/**
	 * DOCUMENT ME!
	 */
	int DISCARD_BIN = 2;

	/**
	 * DOCUMENT ME!
	 */
	int ERROR = -1;

	/**
	 * DOCUMENT ME!
	 */
	int MISSING_PARAMETER = -2;

	/**
	 * DOCUMENT ME!
	 */
	int NEW_BIN = 0;

	/**
	 * DOCUMENT ME!
	 */
	int NEW_RANGE = 3;

	/**
	 * 
	 * @param handler
	 *            to calculate massspecs
	 */
	void setAlgorythmHandler(AlgorythmHandler handler);

	/**
	 * returns the assigned massspecs
	 * 
	 * @return
	 */
	Collection<Map<String, Object>> getAssigned();

	/**
	 * returns all unknowns
	 * @return
     */
	Collection<Map<String, Object>> getUnknowns();

		/**
         * returns the used bins of the implemantation
         *
         * @return
         */
	Collection<Map<String, Object>> getBins();

	void setConfig(Element config);

	void setConnection(Connection c);

	/**
	 * resulthandler for this sample
	 * 
	 * @param handler
	 */
	void setResultHandler(ResultHandler handler);

	/**
	 * sets the sample id
	 * 
	 */
	void setSampleId(int id);

	/**
	 * clear unused variables
	 * 
	 */
	void flush();

	/**
	 * starts the calculation
	 */
	int run();

	/**
	 * a simple filter to filter bins without changing queries
	 * 
	 * @param binFilter
	 */
	public void setBinFilter(MassSpecFilter binFilter);

	/**
	 * a simple filter to filter unknown without changing queries
	 * 
	 * @param binFilter
	 */
	public void setUnknownFilter(MassSpecFilter unknownFilter);

	/**
	 * used algorithm handler
	 * 
	 * @return
	 */
	public AlgorythmHandler getAlgorythemHandler();

	/**
	 * adds a mass spec modifier
	 * 
	 * @param modifier
	 */
	public void addMassSpecModifier(MassSpecModifier modifier);

	/**
	 * removes a mass spec modifier
	 * 
	 * @param modifier
	 */
	public void removeMassSpecModifier(MassSpecModifier modifier);
}
