/*
 * Created on 29.07.2004
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
 * berechnet das minimum einer probe
 */
public class Min extends DeskriptiveMethod {
    /**
     *
     * @uml.property name="logger"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#getName()
     */
    public String getName() {
        return "min";
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#acceptZeros()
     */
    public boolean acceptZeros() {
        return true;
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#calculate(java.util.List)
     */
    public double calculate(Collection list) {
        double min = 0;
        Iterator it = list.iterator();
        int count = 0;

        while (it.hasNext() == true) {
            Object v = it.next();
            
            Object o = v;
            if(o instanceof FormatObject){
            	o = ((FormatObject)o).getValue();
            }

            if (o instanceof String) {
                try {
                    double value = Double.parseDouble((String) o);

                    if (count == 0) {
                        min = value;
                    } else {
                        if (value < min) {
                            min = value;
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.error(e.getMessage(), e);
                }
            } else if (o instanceof Number) {
                double value = ((Number) o).doubleValue();

                if (count == 0) {
                    min = value;
                } else {
                    if (value < min) {
                        min = value;
                    }
                }
            } else {
                throw new WrongTypeOfValueException(
                    "value has not the right class, is a " +
                    o.getClass().getName());
            }

            count++;
        }

        return min;
    }
}
