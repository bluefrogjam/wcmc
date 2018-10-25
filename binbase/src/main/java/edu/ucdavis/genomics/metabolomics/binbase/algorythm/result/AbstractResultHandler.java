/*
 * Created on 04.06.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.result;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.Matchable;
import edu.ucdavis.genomics.metabolomics.util.SQLObject;


/**
 * @author wohlgemuth
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class AbstractResultHandler extends SQLObject
    implements ResultHandler {
    /**
     * DOCUMENT ME!
     */
    protected boolean allowNewBin = true;

    private Matchable matchable;
    
    public Matchable getMatchable() {
		return matchable;
	}

	public void setMatchable(Matchable matchable) {
		this.matchable = matchable;
	}

	/**
     * @version Aug 6, 2003
     * @author wohlgemuth
     * <br>
     * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.util.transform.abstracthandler.binlib.algorythm.result.ResultHandler#setNewBinAllowed(boolean)
     */
    public void setNewBinAllowed(boolean value) {
        this.allowNewBin = value;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isNewBinAllowed() {
        return this.allowNewBin;
    }

    /**
     * @see ResultHandler#isReprocessed()
     */
    public boolean isReprocessed() {
        return false;
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.util.transform.abstracthandler.algorythm.binlib.algorythm.matching.ResultHandler#assaignBin()
     */
    public void assignBin(java.util.Map spectra) throws Exception {
        logger.debug("assign bin at this implementation not suported");
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.util.transform.abstracthandler.algorythm.binlib.algorythm.matching.ResultHandler#discardBin()
     */
    public void discardBin(java.util.Map spectra) throws Exception {
        logger.debug("discard bin at this implementation not suported");
    }

    /**
     * DOCUMENT ME!
     */
    public void flush() {
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.util.transform.abstracthandler.algorythm.binlib.algorythm.matching.ResultHandler#newBin()
     */
    public void newBin(java.util.Map spectra) throws Exception {
        logger.debug("new bin at this implementation not suported");
    }
}
