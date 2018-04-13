/*
 * Created on 29.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv;

import edu.ucdavis.genomics.metabolomics.exception.WrongTypeOfValueException;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;


/**
 * @author wohlgemuth
 * berechnet den maximal wert einer liste
 */
public class Max extends DeskriptiveMethod {
    /**
     * @uml.property name="logger"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    Logger logger = LoggerFactory.getLogger(getClass());

    int index;

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#getName()
     */
    public String getName() {
        return "max";
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
        double max = 0;
        index = 0;
        Iterator it = list.iterator();
        int i = 0;

        while (it.hasNext() == true) {
            Object v = it.next();

            Object o = v;
            if (o instanceof FormatObject) {
                o = ((FormatObject) o).getValue();
            }

            if (o instanceof String) {
                try {
                    double value = Double.parseDouble((String) o);

                    if (value > max) {
                        max = value;
                        index = i;
                    }
                } catch (NumberFormatException e) {
                    logger.error(e.getMessage(), e);
                }
            } else if (o instanceof Number) {
                double value = ((Number) o).doubleValue();

                if (value > max) {
                    max = value;
                    index = i;
                }
            } else if (o == null) {
                double value = 0;

                if (value > max) {
                    max = value;
                    index = i;
                }
            } else {
                throw new WrongTypeOfValueException(
                    "value has not the right class, is a " +
                        o.getClass().getName());
            }

            i++;
        }

        return max;
    }

    public int getIndex() {
        return index;
    }
}
