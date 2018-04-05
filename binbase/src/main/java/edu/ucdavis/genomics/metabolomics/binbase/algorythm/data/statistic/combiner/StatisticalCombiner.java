/*
 * Created on Nov 2, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.combiner;

import java.util.List;
import java.util.Map;

import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.ColumnCombiner;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.DeskriptiveMethod;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.CombinedObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.NullObject;

public class StatisticalCombiner extends ColumnCombiner {
	private DeskriptiveMethod method = null;

	@Override
	public Object doWork(List data) {

		double m = method.calculate(data);
		Map attributes = ((FormatObject) data.get(0)).getAttributes();
		FormatObject result = null;

		System.err.println(attributes);
		if (m == 0) {
			result = new NullObject<Double>(m,attributes);
		} else {
			result = new CombinedObject<Double>(m,attributes);
		}
		
		return result;
	}

	@Override
	public boolean isConfigNeeded() {
		return true;
	}

	@Override
	protected void configure(Element e) throws RuntimeException {
		try {
			if (e.getAttribute("class") == null) {
				throw new ConfigurationException("need attribute \"class\"");
			}
			String classname = e.getAttribute("class").getValue();
			method = (DeskriptiveMethod) Class.forName(classname).newInstance();
		
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
