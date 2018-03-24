/*
 * <p>
 * Created on 28.03.2003 <br>
 * Filename ResultHandable.java
 * Projekt BinBaseDatabase
 *
 *
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.result;

import java.util.Map;

import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.Matchable;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.Diagnostics;
import edu.ucdavis.genomics.metabolomics.util.SQLable;


/**
 * <h3>
 * Title: ResultHandable
 * </h3>
 *
 * <p>
 * Author:      Gert Wohlgemuth <br>
 * Leader:      Dr. Oliver Fiehn <br>
 * Company:     Max Plank Institute for molecular plant physologie <br>
 * Contact:     wohlgemuth@mpimp-golm.mpg.de <br>
 * Version:     <br>
 * Description: Handles our ResultTyps and is called from the AlgorythmHandler

 * </p>
 */
public interface ResultHandler extends SQLable,Diagnostics {
    /**
     * did this resulthandler tried to generate a bin
     * @return
     */
    boolean isCreatedBin();

    /**
     * ist es erlaub neue bins zu generieren
     * @version Aug 6, 2003
     * @author wohlgemuth
     * <br>
     * @param value
     */
    void setNewBinAllowed(boolean value);

    boolean isNewBinAllowed();

    /**
     * gibt an ob ein reprocessing stattgefunden hat
     * @return
     */
    boolean isReprocessed();

    /**
     * we can assaign this bin
     *
     * @throws Exception
     */
    void assignBin(Map spectra) throws Exception;

    /**
     * we can discard this bin
     * @throws Exception
     *
     */
    void discardBin(Map spectra) throws Exception;

    /**
     * f?hrt ein flush
     *
     */
    void flush();

    /**
     * its a new bin
     * @throws Exception
     */
    void newBin(Map spectra) throws Exception;
    
    /**
     * sets the configuration properties
     * @param configuration
     */
    public void setConfiguration(Element configuration);
    
    /**
     * what are we using as matchable
     * @param matchable
     */
    public void setMatchable(Matchable matchable);
    
    public Matchable getMatchable();
}
