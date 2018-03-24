/*
 * Created on 01.10.2004
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.Matchable;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.SimpleAlgorythmHandler;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.SimpleMatching;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.result.ProblematicResultHandler;

/**
 * @author wohlgemuth
 */
public class GitterMethode extends AbstractMethod {

	private Matchable secondaeryMatch;

	/**
	 * Creates a new GitterMethode object.
	 */
	public GitterMethode() {
		try {
			this.setMatchable((Matchable) Class.forName(CONFIG.getElement("class.matching").getAttributeValue("value")).newInstance());
			this.secondaeryMatch = (Matchable) Class.forName(CONFIG.getElement("class.matching").getAttributeValue("value")).newInstance();
		} catch (Exception e) {
			logger.error("failed to set defined class for matching use default implemantation", e);
			this.setMatchable(new SimpleMatching());
			this.secondaeryMatch = new SimpleMatching();
		}
	}
 
	/**
	 * 
	 * @author wohlgemuth
	 * @version Nov 3, 2005
	 * @see edu.ucdavis.genomics.metabolomics.util.SQLObject#prepareStatements()
	 */
	protected void prepareStatements() throws Exception {
		super.prepareStatements();
		
		
	}

	/**
	 * DOCUMENT ME!
	 */
	protected void start() {
		super.start();

		/**
		 * a simplistic alternative algorithm used to annotated massspecs we missed in the first run
		 */
		SimpleAlgorythmHandler handler = new SimpleAlgorythmHandler();
		handler.setConnection(this.getConnection());
		logger.info("starting secondaery matching...");
		this.secondaeryMatch.setAlgorythmHandler(handler);
		
		
		this.getResultHandler().setNewBinAllowed(false);
		ProblematicResultHandler r = new ProblematicResultHandler();
		r.setConnection(this.getConnection());
		
		this.secondaeryMatch.setResultHandler(r);
		this.secondaeryMatch.setSampleId(this.sampleId);
		this.secondaeryMatch.setConnection(this.getConnection());
		this.secondaeryMatch.run();

		logger.info("done with secondaery matching...");

	}

}
