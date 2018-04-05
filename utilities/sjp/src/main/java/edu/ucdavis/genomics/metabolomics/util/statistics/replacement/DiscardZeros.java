/*
 * Created on 29.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.replacement;

import edu.ucdavis.genomics.metabolomics.exception.WrongTypeOfValueException;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;

import java.util.List;
import java.util.Vector;


/**
 * @author wohlgemuth entfernt automatisch alle nullstellen aus der liste. Dadurch wird diese dementsprechend kleiner
 */
public class DiscardZeros implements ZeroReplaceable {
    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.replacement.ZeroReplaceable#replaceZeros(List)
     */
    public List replaceZeros(List array) {
        List list;

        list = new Vector();

        for (int i = 0; i < array.size(); i++) {
            Object value = array.get(i);
            Object o = value;
            if (o instanceof FormatObject) {
                o = ((FormatObject) o).getValue();
            }
            if (o instanceof String) {
                double d = Double.parseDouble(o.toString());

                if (Math.abs(d - 0) < 0.0001) {
                } else {
                    list.add(value);
                }
            } else if (o instanceof Number) {
                double d = ((Number) o).doubleValue();

                if (Math.abs(d - 0) < 0.0001) {
                } else {
                    list.add(value);
                }
            } else {
                throw new WrongTypeOfValueException(
                    "value has not the right class, is a " +
                        o.getClass().getName());
            }
        }

        return list;
    }

    @Override
    public String getDescription() {
        return "removes any of the zeros from the given list";
    }
}
