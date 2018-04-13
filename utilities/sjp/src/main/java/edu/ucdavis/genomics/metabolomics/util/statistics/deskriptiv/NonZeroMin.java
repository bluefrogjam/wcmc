/*
 * Created on 29.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv;

import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;

import java.util.Collection;

/**
 * @author wohlgemuth
 */
public class NonZeroMin extends Min {
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

    /**
     * DOCUMENT ME!
     *
     * @param list DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    @Override
    public double calculate(Collection list) {
        double min = Double.MAX_VALUE;

        for (Object o : list) {

            if (o instanceof FormatObject) {
                o = ((FormatObject) o).getValue();
            }

            try {
                if (o != null) {
                    double value = Double.parseDouble(o.toString());

                    if (value < min) {

                        if (value != 0) {
                            min = value;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                logger.error(e.getMessage(), e);
            }

        }

        if (min == Double.MAX_VALUE) {
            min = Double.NaN;
        }
        return min;
    }
}
