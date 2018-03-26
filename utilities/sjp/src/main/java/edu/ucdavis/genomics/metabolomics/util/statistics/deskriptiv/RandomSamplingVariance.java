/*
 * Created on 28.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv;

import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;

import edu.ucdavis.genomics.metabolomics.exception.WrongTypeOfValueException;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;
import org.slf4j.LoggerFactory;


/**
 * @author wohlgemuth
 */
public class RandomSamplingVariance extends DeskriptiveMethod {
    Logger logger = LoggerFactory.getLogger(StandardDeviation.class);

    /**
     *
     * @uml.property name="mean"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    Mean mean = new Mean();

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#getName()
     */
    public String getName() {
        return "Random Sampling Variance";
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
        double mean = this.mean.calculate(list);

        double result = 0;
        Iterator it = list.iterator();

        while (it.hasNext() == true) {
            Object v = it.next();
            
            Object o = v;
            if(o instanceof FormatObject){
            	o = ((FormatObject)o).getValue();
            }

            double value = 0;

            if (o instanceof String) {
                try {
                    value = Double.parseDouble((String) o);
                } catch (NumberFormatException e) {
                    logger.error(e.getMessage(), e);
                }
            } else if (o instanceof Number) {
                value = ((Number) o).doubleValue();
            } else {
                throw new WrongTypeOfValueException(
                    "value has not the right class, is a " +
                    o.getClass().getName());
            }

            value = ((value - mean) * (value - mean));
            result = result + value;
        }

        return (1 / ((double) list.size() - 1)) * result;
    }
}
