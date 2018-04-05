/*
 * Created on Jun 30, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.combiner;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import edu.ucdavis.genomics.metabolomics.util.statistics.data.ColumnCombiner;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.Max;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.CombinedObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.NullObject;

/**
 * returns the max value
 * 
 * @author wohlgemuth
 * @version Jun 30, 2006
 */
public class CombineByMax extends ColumnCombiner {
	private Max max = new Max();

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public Object doWork(List data) {
		double m = max.calculate(data);
		
		if (data.get(max.getIndex()) != null) {
			Map attributes = ((FormatObject) data.get(max.getIndex())).getAttributes();
			FormatObject result = null;

			if (m == 0) {
				// logger.debug("create new null object");
				result = new NullObject<Double>(m, attributes);
			}
			else {
				// logger.debug("create new combined object");
				result = new CombinedObject<Double>(m, attributes);
			}
			
			return result;
		}
		return null;
	}

}
