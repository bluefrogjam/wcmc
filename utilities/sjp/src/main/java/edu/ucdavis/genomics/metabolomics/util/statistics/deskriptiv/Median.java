/*
 * Created on 29.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv;

import edu.ucdavis.genomics.metabolomics.exception.WrongTypeOfValueException;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;

import java.util.Collections;
import java.util.List;
import java.util.Vector;


/**
 * @author wohlgemuth
 */
public class Median extends Mean {
    /**
     * @uml.property name="mean"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    Mean mean = new Mean();

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#getName()
     */
    public String getName() {
        return "median";
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#acceptZeros()
     */
    public boolean acceptZeros() {
        return true;
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.Meanable#calculate(List)
     */
    public double calculate(List array) {
        if (array.isEmpty()) {
            return -1;
        }

        double median = 0;
        List list;

        if (zeros == false) {
            list = new Vector();

            for (int i = 0; i < array.size(); i++) {
                Object v = array.get(i);

                Object o = v;
                if (o instanceof FormatObject) {
                    o = ((FormatObject) o).getValue();
                }

                if (o instanceof String) {
                    double d = Double.parseDouble(o.toString());

                    if (Math.abs(d - 0) < 0.0001) {
                    } else {
                        list.add(new Double(d));
                    }
                } else if (o instanceof Number) {
                    double d = ((Number) o).doubleValue();

                    if (Math.abs(d - 0) < 0.0001) {
                    } else {
                        list.add(new Double(d));
                    }
                } else {
                    throw new WrongTypeOfValueException(
                        "value has not the right class, is a " +
                            o.getClass().getName());
                }
            }
        } else {
            list = array;
        }

        Collections.sort(list);

        if ((list.size() % 2) == 0) {
            Object a = list.get(list.size() / 2);
            Object b = list.get(list.size() / (2 + 1));
            List temp = new Vector();
            temp.add(a);
            temp.add(b);

            median = super.calculate(temp);
        } else {
            Object o = list.get(((list.size() + 1) / 2) - 1);

            if (o instanceof String) {
                try {
                    median = Double.parseDouble((String) o);
                } catch (NumberFormatException e) {
                    logger.error(e.getMessage(), e);
                }
            } else if (o instanceof Number) {
                median = ((Number) o).doubleValue();
            } else {
                throw new WrongTypeOfValueException(
                    "value has not the right class, is a " +
                        o.getClass().getName());
            }
        }

        return median;
    }
}
