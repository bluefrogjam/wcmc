/*
 * Created on Aug 20, 2003
 *
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
 * @version Aug 20, 2003
 * <br>
 * BinBaseDatabase
 * @description
 */
public class Mean extends Meanable {
    /**
     *
     * @uml.property name="logger"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    Logger logger = LoggerFactory.getLogger(Mean.class);
    boolean zeros = true;

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#getName()
     */
    public String getName() {
        return "Arithmetical Mean";
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#acceptZeros()
     */
    public boolean acceptZeros() {
        return true;
    }

    /**
     * berechnet das arimethrische mittel
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.Meanable#calculate(java.util.List)
     */
    public double calculate(Collection list) {
        double sum = 0;
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
                    sum = sum + value;

                    if (Math.abs(value - 0) < 0.0001) {
                        if (this.zeros == true) {
                            count++;
                        }
                    } else {
                        count++;
                    }
                } catch (NumberFormatException e) {
                    logger.error(e.getMessage(), e);
                }
            } else if (o instanceof Number) {
                double value = ((Number) o).doubleValue();
                sum = sum + value;

                if (Math.abs(value - 0) < 0.0001) {
                    if (this.zeros == true) {
                        count++;
                    }
                } else {
                    count++;
                }
            } else {
                throw new WrongTypeOfValueException(
                    "value has not the right class, is a " +
                    o.getClass().getName());
            }
        }

        return sum / count;
    }

    /**
     * @version Aug 21, 2003
     * @author wohlgemuth
     * <br>
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.Meanable#enableZeros(boolean)
     */
    public void enableZeros(boolean value) {
        this.zeros = value;
    }
}
