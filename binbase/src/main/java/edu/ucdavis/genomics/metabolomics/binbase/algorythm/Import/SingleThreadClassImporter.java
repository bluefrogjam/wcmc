/*
 * Created on Nov 15, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.output.XLS;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods.CorrectionMethod;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods.GitterMethode;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods.Methodable;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.CalculateBinAvability;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.SpectraConversionException;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.Result;
import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.exception.ValidationException;
import edu.ucdavis.genomics.metabolomics.util.io.source.DatabaseSourceFactoryImpl;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.io.source.SourceFactory;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.SimpleDatafile;

/**
 * provides a single threaded class importer
 * 
 * @author wohlgemuth
 * @version Nov 15, 2005
 */
public class SingleThreadClassImporter extends ClassImporter {

	/**
	 * used to calculate the bin avability for a given class
	 */
	private CalculateBinAvability avability = new CalculateBinAvability();

	/**
	 * sets a flag for the sample
	 */
	private PreparedStatement finishSample;

	private PreparedStatement getSpectraIfForBinIdStatement;

	/**
	 * @author wohlgemuth
	 * @version Nov 15, 2005
	 * @see ClassImporter#importSample(edu.ucdavis.genomics.metabolomics.binbase.bci.server.types.ExperimentSample[],
	 *      String)
	 */
	protected int[] importSample(BinBaseExperimentImportSample[] samples,
			String classname) throws Exception {

		int sampleIds[] = new int[samples.length];

		Importer importer = new Importer();
		importer.setConnection(getConnection());

		for (int i = 0; i < samples.length; i++) {
			importSingleSample(samples[i], classname, importer, sampleIds, i);
		}

		return sampleIds;
	}

	/**
	 * imports a single sample
	 * 
	 * @param samples
	 * @param classname
	 * @param sampleIds
	 * @param i
	 * @throws Exception
	 */
	void importSingleSample(final BinBaseExperimentImportSample sample,
			String classname, Importer importer, final int sampleIds[],
			final int current) throws Exception {
		Thread.currentThread().setName(
				"importing: " + sample.getName() + "/" + classname);

		int sampleId = 0;
		try {

			sampleId = importer.importData(sample, classname);
			sample.setImported(true);

			if (logger.isDebugEnabled()) {
				logger.debug("created id is: " + sampleId + " for sample: "
						+ sample.getName());
			}
		} catch (SpectraConversionException e) {
			logger.error("file was invalid for sample: " + sample.getName());
		} catch (Exception e) {
			logger.error("some exception occured: " + e.getMessage(), e);
		}

		sampleIds[current] = sampleId;
		if (sampleId <= 0) {
			throw new Exception(
					"something went wrong, generated sample id can't be <= 0!");
		}
	}

	/**
	 * @author wohlgemuth
	 * @version Nov 16, 2005
	 * @throws Exception
	 * @see ClassImporter#correctSamples(int[])
	 */
	protected final void correctSamples(BinBaseExperimentImportSample[] samples)
			throws Exception {
		List<BinBaseExperimentImportSample> failed = new ArrayList<BinBaseExperimentImportSample>(
				samples.length);

		doCorrection(samples, failed);

		// send report out for samples which failed the correction
		if (failed.size() > 0) {
			logger.debug("failed samples: " + failed);
			logger.debug("count of samples: " + samples.length);

			double ratio = Double.parseDouble(CONFIG
					.getValue("import.correctionFailedNotification"));
			double current = 1 - (double) failed.size()
					/ (double) samples.length;

			if (current < ratio) {
				StringBuffer failedSamples = new StringBuffer();
				// now we send the report
				for (BinBaseExperimentImportSample s : failed) {
					failedSamples.append(s.getName() + " - " + s.getSampleId()
							+ " - " + s.getId());
					failedSamples.append("\n");
				}
				failedSamples.append("\n\n");
				failedSamples.append("class was: " + getCurrentClass() + "\n");

				failedSamples
						.append("please investigate the cause and calculate the class again in case of error\n");

			}
		}

	}

	/**
	 * corects our samples
	 * 
	 * @param samples
	 * @param failed
	 * @param failedSamples
	 * @return
	 * @throws Exception
	 */
	protected void doCorrection(BinBaseExperimentImportSample[] samples,
			List<BinBaseExperimentImportSample> failed) throws Exception {
		Methodable method = new CorrectionMethod();
		method.setConnection(this.getConnection());

		for (BinBaseExperimentImportSample sample : samples) {
			if (correctSingleSample(sample, method)) {
				failed.add(sample);
			}

		}
	}

	/**
	 * corrects a single sample
	 * 
	 * @param failed
	 * @param failedSamples
	 * @param sample
	 * @return
	 * @throws Exception
	 */
	protected final boolean correctSingleSample(
			final BinBaseExperimentImportSample sample,
			final Methodable correction) throws Exception {

		Thread.currentThread().setName(
				"correcting: " + sample.getName() + "/"
						+ getCurrentClass().getId());
		correction.setSampleId(sample.getSampleId());
		correction.setNewBinAllowed(false);
		correction.run();
		sample.setCorrected(true);
		if (correction.isCorrectionFailed()) {
			return true;

		}
		return false;
	}

	/**
	 * @author wohlgemuth
	 * @version Nov 16, 2005
	 * @throws Exception
	 * @see ClassImporter#matchSamples(int[])
	 */
	protected void matchSamples(BinBaseExperimentImportSample[] samples)
			throws Exception {
		Methodable method = new GitterMethode();
		method.setConnection(this.getConnection());

		for (BinBaseExperimentImportSample sample : samples) {

			doMatch(sample, method);
		}
	}

	/**
	 * matches a single sample
	 * 
	 * @param sample
	 * @param method
	 * @throws Exception
	 */
	protected final void doMatch(BinBaseExperimentImportSample sample,
			Methodable method) throws Exception {
		Thread.currentThread().setName(
				"matching: " + sample.getName() + "/"
						+ getCurrentClass().getId());

		method.setSampleId(sample.getSampleId());
		method.setNewBinAllowed(false);

		if (this.getMassSpecFilter() != null) {
			method.getMatchable().setBinFilter(getMassSpecFilter());
		}
		method.run();
		sample.setMatched(true);

	}

	/**
	 * @author wohlgemuth
	 * @version Nov 16, 2005
	 * @throws SQLException
	 * @see ClassImporter#deleteWrongBins(int[],
	 *      double)
	 */
	protected void deleteWrongBins(String classname, int[] binIds, double ratio)
			throws SQLException {

		logger.info("generating similarity matrix for all generated bins before deletion");
		//generateSimilarityMatrixForAllBins(classname);
		
		logger.info("done");
		for (int i = 0; i < binIds.length; i++) {
			double calculatedRatio = avability.calculatePercentual(binIds[i],
					classname, CalculateBinAvability.NOT_FINISHED);

			// get spectra id for bin

			int spectraId = getSpectraIdForBin(binIds[i]);

			logger.debug("calculated ration is: " + calculatedRatio
					+ " and must be " + ratio);
			if (calculatedRatio < ratio) {
				logger.debug("delete bin because it did not reach the defined ratios "
						+ binIds[i]);

				getDiagnosticsService()
						.diagnosticAction(
								spectraId,
								binIds[i],
								this.getClass(),
								"detect wrong bins",
								"bin was deleted because the calculated ratio was to low",
								new Result("bin was removed"),
								new Object[] { calculatedRatio, ratio });
				this.deleteBin(binIds[i]);
			} else {
				getDiagnosticsService()
						.diagnosticAction(
								spectraId,
								binIds[i],
								this.getClass(),
								"detect wrong bins",
								"bin was kept because the calculated ratio was high enough",
								new Result("bin was kept"),
								new Object[] { calculatedRatio, ratio });

			}
		}
		logger.info("finished deleting of wrongly generated bins");
	}

	/**
	 * generates a similairty matrix of bins, mostly used to diagnose bin
	 * generation issues
	 * 
	 * @param classname
	 */
	protected void generateSimilarityMatrixForAllBins(String classname) {
		if (this.isNewBinsAllowed()) {
			// generates a similarity matrix of the bins before we remove them
			try {

				FileOutputStream out = new FileOutputStream(new File(classname
						+ "_bin_generation_matrix.xls"));
				PreparedStatement statement = null;
				try {
					statement = this
							.getConnection()
							.prepareStatement(
									"select distinct a.bin_id, b.bin_id,a.spectra_id,b.spectra_id, a.uniquemass,b.uniquemass, calculateSimilarity(a.spectra,b.spectra), abs(a.retention_index - b.retention_index) as ridiff from bin a, bin b where calculateSimilarity(a.spectra,b.spectra) > 700 and abs(a.retention_index - b.retention_index) < 4000 and a.bin_id != b.bin_id order by ridiff");

					ResultSet result = statement.executeQuery();
					DataFile file = new SimpleDatafile();
					file.addEmptyColumn("binId first");
					file.addEmptyColumn("binId second");
					file.addEmptyColumn("spectra id first");
					file.addEmptyColumn("spectra id second");
					file.addEmptyColumn("uniquemass first");
					file.addEmptyColumn("uniquemass second");
					file.addEmptyColumn("similarity");
					file.addEmptyColumn("retention index difference");

					while (result.next()) {
						List<Object> row = new ArrayList<Object>();
						row.add(result.getInt(1));
						row.add(result.getInt(2));
						row.add(result.getInt(3));
						row.add(result.getInt(4));
						row.add(result.getInt(5));
						row.add(result.getInt(6));
						row.add(result.getDouble(7));
						row.add(result.getInt(8));

						file.addRow(row);
					}

					result.close();

					XLS xls = new XLS();
					xls.write(out, file);

					out.close();
					out.flush();

				} finally {
					statement.close();
				}
			} catch (Exception e) {
				logger.error("ignore: " + e.getMessage(), e);
			}
		}
	}

	private int getSpectraIdForBin(int i) throws SQLException {

		getSpectraIfForBinIdStatement.setInt(1, i);
		ResultSet set = getSpectraIfForBinIdStatement.executeQuery();
		set.next();

		int spectraId = set.getInt(1);
		set.close();

		return spectraId;
	}

	/**
	 * @author wohlgemuth
	 * @version Nov 16, 2005
	 * @see edu.ucdavis.genomics.metabolomics.util.SQLObject#prepareVariables()
	 */
	protected void prepareVariables() throws Exception {
		super.prepareVariables();
		this.avability.setConnection(this.getConnection());
	}

	/**
	 * @author wohlgemuth
	 * @version Nov 17, 2005
	 * @throws ConfigurationException
	 * @throws ValidationException
	 * @see ClassImporter#validateSources(edu.ucdavis.genomics.metabolomics.binbase.bci.server.types.ExperimentSample[])
	 */
	protected void validateSources(BinBaseExperimentImportSample[] samples)
			throws ConfigurationException, ValidationException {
		for (BinBaseExperimentImportSample sample : samples) {
			vaildateSingleSource(sample);
		}
	}

	protected final void vaildateSingleSource(
			BinBaseExperimentImportSample sample)
			throws ConfigurationException, ValidationException {
		Thread.currentThread().setName(
				"validating: " + sample.getName() + "/"
						+ getCurrentClass().getId());

		Source source;
		Map map = new HashMap();
		map.put("CONNECTION", this.getConnection());

		if (sample.getName().indexOf(':') > -1) {
			logger.debug("old style of sample name provided, replace : with _");
			sample.setName(sample.getName().replaceAll(":", "_"));
		}

		source = SourceFactory.newInstance(
				DatabaseSourceFactoryImpl.class.getName()).createSource(
				sample.getName(), map);

		sample.setValidated(true);
		if (!source.exist()) {
			throw new ValidationException("source with identifier: "
					+ source.getSourceName()
					+ " does'nt exist, please try again later!");
		}
	}

	/**
	 * @author wohlgemuth
	 * @version Mar 24, 2006
	 * @throws SQLException
	 * @see ClassImporter#finishSample(int)
	 */
	protected void finishSample(BinBaseExperimentImportSample sample)
			throws SQLException {
		logger.info("declare sample as finished: " + sample);
		this.finishSample.setInt(1, sample.getSampleId());
		int result = this.finishSample.executeUpdate();
		logger.info("updated: " + result + " sampels and set them to finished!");
	}

	protected void prepareStatements() throws Exception {
		super.prepareStatements();
		this.finishSample = this.getConnection().prepareStatement(
				SQL_CONFIG.getValue(CLASS + ".sample"));
		this.getSpectraIfForBinIdStatement = this
				.getConnection()
				.prepareStatement("select spectra_id from bin where bin_id = ?");

	}

	@Override
	protected void postmatchSamples(BinBaseExperimentImportSample[] samples)
			throws Exception {
		Methodable method = new GitterMethode();
		method.setConnection(this.getConnection());

		for (BinBaseExperimentImportSample sample : samples) {

			doPostMatch(sample, method);
		}
	}

	protected final void doPostMatch(BinBaseExperimentImportSample sample,
			Methodable method) throws Exception {
		Thread.currentThread().setName(
				"post matching: " + sample.getName() + "/"
						+ getCurrentClass().getId());

		method.setSampleId(sample.getSampleId());
		method.setNewBinAllowed(false);

		if (this.getMassSpecFilter() != null) {
			method.getMatchable().setBinFilter(getMassSpecFilter());
		}
		method.run();
		sample.setPostMatched(true);
	}
}
