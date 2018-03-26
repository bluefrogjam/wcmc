/*
 * Created on 28.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv;

import java.util.Collection;


/**
 * @author wohlgemuth berechnet die standard abweichung
 */
public class StandardDeviation extends DeskriptiveMethod {
    /**
     *
     * @uml.property name="v"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    DeskriptiveMethod v = new RandomSamplingVariance();

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#getName()
     */
    public String getName() {
        return "Standard Deviation";
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#acceptZeros()
     */
    public boolean acceptZeros() {
        return false;
    }

    /**
     * berechnet die standard abweichung
     *
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#calculate(java.util.List)
     */
    public double calculate(Collection list) {
        return Math.sqrt(v.calculate(list));
    }
}
