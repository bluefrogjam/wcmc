/*
 * Created on 28.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv;

import edu.ucdavis.genomics.metabolomics.util.statistics.Statistics;

import java.util.Collection;


/**
 * @author wohlgemuth dient allgemein zum berechnen von statistischen
 *         informationen welche
 */
public abstract class DeskriptiveMethod{
    /**
     * Creates a new DeskriptiveMethod object.
     */
    public DeskriptiveMethod() {
        if (!Statistics.DESCRIPTIVE_METHODS.contains(this)) {
            Statistics.DESCRIPTIVE_METHODS.add(this);
        }
    }

    /**
     * f?hrt die berechnung durch
     *
     * @param list
     *            eine liste
     * @return das ergebniss
     */
    public abstract double calculate(Collection list);


    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#getName()
     */
    public String getName() {
        return this.getClass().getName();
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#acceptZeros()
     */
    public boolean acceptZeros() {
        return false;
    }
}
