/*
 * Created on 05.06.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.Matchable;


/**
 * @author wohlgemuth
 * is needed because there is a design mistake with the matching and so we need this to fix it. it gets really annoying and I must rewrite this stuff sometimes...
 */
public interface Methodable {
    /**
     * defines if the matching is allowed or not
     * @param value
     *
     * @uml.property name="correctionFailed"
     */
    void setCorrectionFailed(boolean value);

    /**
     * defines if the matching is allowed when the corretion failed
     * @return
     *
     * @uml.property name="correctionFailed"
     */
    boolean isCorrectionFailed();

    /**
     * gibt das matching objekt zur?ck
     * @return
     */
    Matchable getMatchable();

    /**
     * @version Aug 6, 2003
     * @author wohlgemuth
     * <br>
     * is it generally allowed that samples can generate bins, default is true
     */
    void setNewBinAllowed(boolean value);

    /**
     * setzt die sample id
     * @param id
     */
    void setSampleId(int id);

    /**
     * f?hrt die klasse aus
     * @throws Exception 
     *
     */
    void run() throws Exception;
}
