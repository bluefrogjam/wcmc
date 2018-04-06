package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.replacement;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.ResultDataFile;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods.CorrectionMethod;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods.correction.CorrectionCache;
import edu.ucdavis.genomics.metabolomics.binbase.bci.Configurator;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.database.ConnectionFactory;
import edu.ucdavis.genomics.metabolomics.util.database.SimpleConnectionFactory;
import edu.ucdavis.genomics.metabolomics.util.math.CombinedRegression;
import edu.ucdavis.genomics.metabolomics.util.math.Regression;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ContentObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.HeaderFormat;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.SampleObject;

import java.sql.Connection;
import java.util.Properties;

public class ReplaceWithQuantIntensityBasedOnRICurve extends
		OldReplaceWithQuantIntensity3 {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SampleObject<String> currentSample = null;

	private Regression regression = new CombinedRegression(5);
	private boolean regressionFailed = false;

	public String getFolder() {
		return "riCurve";
	}

	public Regression getRegression() {
		return regression;
	}

	/**
	 * returns the retention index for a given bin
	 * 
	 * @param bin
	 * @param sample
	 * @return
	 * @throws BinBaseException
	 * @throws NumberFormatException
	 */
	protected double getRetentionIndexForBin(HeaderFormat<String> bin,
			SampleObject<String> sample) throws NumberFormatException,
			BinBaseException {
		return Double.parseDouble(bin.getAttributes().get("retention_index")
				.toString());
	}

	@Override
	protected double getRetentionTimeForBin(ContentObject<Double> object)
			throws NumberFormatException, BinBaseException {
		return getRetentionTimeBasedOnCorrectionCurve(object);
	}

	/**
	 * calculates the retention time for the given bin, based on the internal ri curve
	 * @param object
	 * @return
	 * @throws BinBaseException
	 */
	protected double getRetentionTimeBasedOnCorrectionCurve(
			ContentObject<Double> object) throws BinBaseException {
		getLogger().debug(
				"calculating retention time with regression for "
						+ object.getAttributes().get("id"));
		if (isRegressionFailed()) {
			throw new RegressionFailedException("sorry the regression failed!");
		}
		HeaderFormat<String> bin = getFile().getBin(
				Integer.parseInt(object.getAttributes().get("id").toString()));
		double retentionIndex = getRetentionIndexForBin(bin, currentSample);
		double assumed = regression.getY(retentionIndex);

		if (getLogger().isDebugEnabled()) {
			getLogger().debug("defined ri: " + retentionIndex);
			getLogger().debug("assumed retention time: " + assumed);
			getLogger().debug(
					"average retention time: "
							+ getFile().getAverageRetentionTimeForBin(
									Integer.parseInt(object.getAttributes()
											.get("id").toString())));
			getLogger().debug(
					"average retention time day: "
							+ getFile().getAverageRetentionTimeForBin(
									Integer.parseInt(object.getAttributes()
											.get("id").toString()),
									currentSample.getValue()));

			getLogger().debug("used sample: " + currentSample.getValue());
		}
		return assumed;
	}

	public boolean isRegressionFailed() {
		return regressionFailed;
	}


	@Override
	protected void preReplaclement(ResultDataFile file,
			SampleObject<String> sample) {
		this.currentSample = sample;
		getLogger().debug("generate calibration curve for sample " + sample.value);
		ConnectionFactory factory = null;
		Connection connection = null;

		try {

			try {
				CorrectionCache cache = new CorrectionCache();

				factory = ConnectionFactory.createFactory();

				Properties p = Configurator.getDatabaseService()
						.getProperties();
				p.setProperty(SimpleConnectionFactory.KEY_USERNAME_PROPERTIE,
						this.getFile().getDatabase());
				factory.setProperties(p);
				connection = factory.getConnection();

				cache.setConnection(connection);
				int id = Integer.parseInt(sample.getAttributes().get("id"));

				int correctionId = cache.getCorrectionId(id);

				if (cache.isCached(correctionId) == false) {
					getLogger().warn(
							"this sample is not cached! This should not be!");

					CorrectionMethod method = new CorrectionMethod();
					method.setConnection(connection);
					method.setNewBinAllowed(false);
					method.setSampleId(id);
					method.run();

					correctionId = cache.getCorrectionId(id);
				}

				regression = cache.getRTvsRIRegression(correctionId);
				regressionFailed = false;
				getLogger().debug(regression.toString());

			} catch (Exception e) {
				getLogger().error(e.getMessage(), e);
				regressionFailed = true;
			}
		} finally {
			try {
				factory.close(connection);
			} catch (Exception e) {
				// only problems should be nullpointers which we can ignore
			}
		}
	}

}
