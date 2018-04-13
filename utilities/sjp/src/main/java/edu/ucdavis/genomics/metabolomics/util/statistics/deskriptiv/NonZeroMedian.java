/*
 * Created on 29.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv;


/**
 * @author wohlgemuth
 */
public class NonZeroMedian extends Median {
    /**
     * Creates a new NonZeroMedian object.
     */
    public NonZeroMedian() {
        this.zeros = false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return "Median - without Zeros";
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#acceptZeros()
     */
    public boolean acceptZeros() {
        return false;
    }
}
