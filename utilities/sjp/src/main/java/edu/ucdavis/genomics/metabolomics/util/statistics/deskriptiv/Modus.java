/*
 * Created on 29.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv;

import edu.ucdavis.genomics.metabolomics.exception.WrongTypeOfValueException;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;

import java.util.*;


/**
 * @author wohlgemuth
 * berechnet den modus
 */
public class Modus extends DeskriptiveMethod {
    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.deskriptiv.DeskriptiveMethod#getName()
     */
    public String getName() {
        return "modus";
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
        Map map = new HashMap();
        double modus = 0;

        Iterator it = list.iterator();

        while (it.hasNext()) {
            Object v = it.next();

            Object o = v;
            if (o instanceof FormatObject) {
                o = ((FormatObject) o).getValue();
            }

            if (map.containsKey(o)) {
                ((Collection) map.get(o)).add(o);
            } else {
                Collection temp = new Vector();
                temp.add(o);
                map.put(o, temp);
            }
        }

        Iterator itx = map.keySet().iterator();

        int max = 0;
        Object id = null;

        while (itx.hasNext()) {
            Object o = itx.next();
            int count = ((Collection) map.get(o)).size();

            if (count > max) {
                max = count;
                id = o;
            }
        }

        if (id instanceof String) {
            modus = Double.parseDouble(id.toString());
        } else if (id instanceof Number) {
            modus = ((Number) id).doubleValue();
        } else {
            throw new WrongTypeOfValueException(
                "value has not the right class, is a " +
                    id.getClass().getName());
        }

        return modus;
    }
}
