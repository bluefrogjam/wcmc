/*
 * Created on Aug 20, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv;



/**
 * @author wohlgemuth
 * @version Aug 20, 2003
 * <br>
 * BinBaseDatabase
 * @description
 */
public abstract class Meanable extends DeskriptiveMethod {
    /**
     * sollen werte = null in die Berechnung mit einbezogen werden
     * @version Aug 21, 2003
     * @author wohlgemuth
     * <br>
     * @param value
     */
    public abstract void enableZeros(boolean value);
}
