/*
 * Created on 29.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv;

import java.util.Collection;


/**
 * @author wohlgemuth
 * berechnet den variations koeffizienten
 */
public class VariationCoefficient extends DeskriptiveMethod {
    /**
     * @uml.property name="mean"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    Mean mean = new Mean();

    /**
     * @uml.property name="stdev"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    StandardDeviation stdev = new StandardDeviation();

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#getName()
     */
    public String getName() {
        return "variation coefficient";
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#acceptZeros()
     */
    public boolean acceptZeros() {
        return false;
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#calculate(java.util.List)
     */
    public double calculate(Collection list) {
        return stdev.calculate(list) / mean.calculate(list);
    }
}
