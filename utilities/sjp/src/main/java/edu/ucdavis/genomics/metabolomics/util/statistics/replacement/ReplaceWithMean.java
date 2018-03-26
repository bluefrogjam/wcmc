/*
 * Created on Aug 21, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.replacement;

import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.Meanable;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.NonZeroMean;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;

import java.util.List;

/**
 * @author wohlgemuth
 * @version Aug 21, 2003 <br>
 *          BinBaseDatabase
 * @description ersetzt nullstellen in listen
 */
public class ReplaceWithMean implements ZeroReplaceable {
	/**
	 * @version Sep 4, 2003
	 * @author wohlgemuth <br>
	 * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.util.statistics.tools.ZeroReplaceable#replaceZeros(List)
	 */
	public List replaceZeros(List list) {
		Meanable mean = new NonZeroMean();
		double meanvalue = mean.calculate(list);

		for (int x = 0; x < list.size(); x++) {

			Object value = list.get(x);
			Object o = value;

			if (o instanceof FormatObject) {
				o = ((FormatObject) o).getValue();
			}

			if (o instanceof Number) {
				if (Math.abs(((Number) o).doubleValue() - 0) < 0.0001) {
					if (value instanceof FormatObject) {
						((FormatObject) value).setValue(meanvalue);
						list.set(x, value);
						
					} else {
						list.set(x, new Double(meanvalue));
					}
				}
			} else if (o instanceof String) {
				if (Math.abs(Double.parseDouble(o.toString()) - 0) < 0.0001) {
					if (value instanceof FormatObject) {
						((FormatObject) value).setValue(meanvalue);
						list.set(x, value);
						
					} else {
						list.set(x, new Double(meanvalue));
					}
				}
			}
		}

		return list;
	}

	@Override
	public String getDescription() {
		return "replaces zeros with the mean";
	}
}
