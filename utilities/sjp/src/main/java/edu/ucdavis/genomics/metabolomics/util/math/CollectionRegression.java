package edu.ucdavis.genomics.metabolomics.util.math;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * advanced regression model which is based of a collection of regressions and
 * tries to use the best regression for the given value
 * 
 * @author wohlgemuth
 * 
 */
public class CollectionRegression implements Regression {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<Regression> regressions = new Vector<Regression>();

	public CollectionRegression() {
	}

	public void addRegression(Regression regression) {
		this.regressions.add(regression);
	}

	@Override
	public double[] getCoeffizent() {
		Collection<Double> data = new Vector<Double>();

		for (Regression r : regressions) {
			for (double d : r.getCoeffizent()) {
				data.add(d);
			}
		}
		return ArrayUtils.toPrimitive(data.toArray(new Double[0]));
	}

	@Override
	public void setData(double[] x, double[] y) {

	}

	@Override
	public double[] getXData() {
		Collection<Double> xData = new Vector<Double>();

		for (Regression r : regressions) {
			for (double d : r.getXData()) {
				xData.add(d);
			}
		}
		return ArrayUtils.toPrimitive(xData.toArray(new Double[0]));
	}

	@Override
	public double[] getYData() {
		Collection<Double> yData = new Vector<Double>();

		for (Regression r : regressions) {
			for (double d : r.getYData()) {
				yData.add(d);
			}
		}
		return ArrayUtils.toPrimitive(yData.toArray(new Double[0]));
	}

	@Override
	public double getY(double x) {

		// let's just start with the first regression
		Regression best = regressions.iterator().next();

		for (Regression r : regressions) {
			logger.info("checking for: " + x + " for curve with calibrations: " + ArrayUtils.toString(r.getYData())+"/"+ArrayUtils.toString(r.getXData()));

			double max = r.getXData()[r.getXData().length - 1];
			double min = r.getXData()[0];

			logger.info("max is: " + max);
			logger.info("min is: " + min);
			
			// if max is smaller than x and min is larger than x this means we
			// should be able to use this regression
			if (x > min && x <= max) {
				logger.info("should be applied now...");
				best = r;
			}
		}
		
		logger.info("using for: " + x + " curve with calibrations: " + ArrayUtils.toString(best.getYData())+"/"+ArrayUtils.toString(best.getXData()));
		return best.getY(x);
	}

	@Override
	public String[] getFormulas() {
		Collection<String> data = new Vector<String>();

		for (Regression r : regressions) {
			for (String d : r.getFormulas()) {
				data.add(d);
			}
		}
		return (data.toArray(new String[0]));
	}

}
