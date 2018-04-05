/*
 * <p>
 * Created on 28.03.2003 <br>
 * Filename SubSettingHandler.java
 * Projekt BinBaseDatabase
 *
 *
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm;

import java.sql.Connection;
import java.util.Map;

import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.Diagnostics;
import edu.ucdavis.genomics.metabolomics.util.SQLable;

/**
 * defines methods to compare 2 massspecs
 * @author wohlgemuth
 * @version Apr 19, 2006
 *
 */
public interface AlgorythmHandler extends SQLable, Diagnostics{

    /**
     * compares to massspecs
     * @author wohlgemuth
     * @version Apr 19, 2006
     * @param bin
     * @param spectra
     * @param configuration configuration of this bin
     * @return true = identical
     */
    boolean compare(Map<String, Object> bin, Map<String, Object> spectra, Element configuration);
    
    /**
     * provides a connection in case we need it
     */
    public void setConnection(Connection connection);
}
