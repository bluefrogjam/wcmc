/*
 * Created on 29.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv;


/**
 * @author wohlgemuth
 * berechnet den mittelwert und ignoriert die nullstellen
 */
public class NonZeroMean extends Mean {
    /**
     * Creates a new NonZeroMean object.
     */
    public NonZeroMean() {
        this.zeros = false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return super.getName() + " - without zeros";
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#acceptZeros()
     */
    public boolean acceptZeros() {
        return false;
    }
}
