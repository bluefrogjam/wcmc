/*
 * Created on Nov 18, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.replacement;

import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.NonZeroMin;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;

import java.util.List;
import java.util.Vector;

/**
 * replace zero values with the min values
 *
 * @author wohlgemuth
 * @version Nov 18, 2005
 */
public class ReplaceWithMin implements ZeroReplaceable {

    private static final long serialVersionUID = 1L;

    /**
     * @author wohlgemuth
     * @version Nov 18, 2005
     * @see edu.ucdavis.genomics.metabolomics.util.statistics.replacement.ZeroReplaceable#replaceZeros(List)
     */
    public List replaceZeros(List list) {
        NonZeroMin min = new NonZeroMin();
        System.out.println(list);
        double minvalue = min.calculate(list);
        List result = new Vector(list.size());

        for (int x = 0; x < list.size(); x++) {
            result.add(null);
            Object value = list.get(x);
            Object o = value;

            if (o instanceof FormatObject) {
                o = ((FormatObject) o).getValue();
            }

            if (o instanceof Number) {
                if (Math.abs(((Number) o).doubleValue() - 0) < 0.0001) {
                    if (value instanceof FormatObject) {
                        ((FormatObject) value).setValue(minvalue);
                        result.set(x, value);
                    } else {
                        result.set(x, new Double(minvalue));
                    }
                } else {
                    result.set(x, list.get(x));
                }
            } else if (o instanceof String) {
                if (Math.abs(Double.parseDouble(o.toString()) - 0) < 0.0001) {
                    if (value instanceof FormatObject) {

                        ((FormatObject) value).setValue(minvalue);
                        result.set(x, value);
                    } else {
                        result.set(x, new Double(minvalue));
                    }
                } else {
                    result.set(x, list.get(x));
                }
            }
        }

        return result;
    }

    @Override
    public String getDescription() {
        return "replaces zeros with the min of this list value";
    }

}
