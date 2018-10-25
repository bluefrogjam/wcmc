package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.baseline;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api.ChromatogramReader;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api.ChromatogramType;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api.DataPoint;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api.Range;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api.Spectrum;
import org.slf4j.LoggerFactory;

public class BaselineCorrector {
	/** Logger */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/** Whether or not to bin the chromatograms by m/z */
	private final boolean useBins;

	/** Define the chromatogram bin width in m/z units */
	private final double binWidth;

	/** Type of baseline correction to perform */
	private final ChromatogramType chromatogramType;

	/** Asymmetric least squares smoothing method */
	private AsymmetricLeastSquares asymBaseline;

	/** Parsed spectra from the given data file */
	private List<Spectrum> spectra;

	/**
	 * 
	 * @param file
	 * @param smoothing
	 * @param asymmetry
	 * @param useBins
	 * @param binWidth
	 * @param chromatogramType
	 */
	public BaselineCorrector(File file, double smoothing, double asymmetry,
			boolean useBins, double binWidth, ChromatogramType chromatogramType)
			throws IOException {

		this(smoothing, asymmetry, useBins, binWidth, chromatogramType,
				new ChromatogramReader().readChromatogram(file
						.getAbsolutePath()));
	}

	/**
	 * alternative constructor
	 * 
	 * @param smoothing
	 * @param asymmetry
	 * @param useBins
	 * @param binWidth
	 * @param chromatogramType
	 * @param spectra
	 * @throws IOException
	 */
	public BaselineCorrector(double smoothing, double asymmetry,
			boolean useBins, double binWidth,
			ChromatogramType chromatogramType, List<Spectrum> spectra) throws IOException {

		this.useBins = useBins;
		this.binWidth = binWidth;
		this.chromatogramType = chromatogramType;

		// Initialize the asymmetric least squares smoothing
		asymBaseline = new AsymmetricLeastSquares(smoothing, asymmetry);

		// Read mass spectra from a cdf file
		this.spectra = spectra;
	}

	/**
	 *
	 */
	public void correctBaselines() {
		// Compute mzRange over all spectra
		logger.info("Computing m/z range...");
		Range mzRange = new Range(spectra.get(0).getMzRange());

		for (Spectrum s : spectra)
			mzRange.extendRange(s.getMzRange());

		// Compute the number of m/z bins to use
		int numBins = useBins ? (int) Math.ceil(mzRange.getSize() / binWidth)
				: 1;

		// Build chromatograms
		logger.info("Building chomatograms...");
		double[][] chromatogram = buildChromatograms(spectra, mzRange, numBins);

		// Calculate baselines in place (overwriting chromatograms) to save
		// memory
		logger.info("Calculating baselines...");

		for (int i = 0; i < numBins; i++) {
			asymBaseline.setChromatogram(chromatogram[i]);
			double[] baseline = asymBaseline.getBaseline();

			if (chromatogramType == ChromatogramType.BASE_PEAK) {
				for (int j = 0; j < spectra.size(); j++)
					chromatogram[i][j] = baseline[j];
			}

			// Normalize baseline for TIC chromatogram
			else if (chromatogramType == ChromatogramType.TIC) {
				for (int j = 0; j < spectra.size(); j++) {
					if (chromatogram[i][j] != 0.0)
						chromatogram[i][j] = baseline[j] / chromatogram[i][j];
				}
			}
		}

		// Subtract baselines
		logger.info("Subtracting baselines...");

		for (int i = 0; i < spectra.size(); i++) {
			Spectrum spectrum = spectra.get(i);

			DataPoint[] dataPoints = spectrum.getDataPoints();
			DataPoint[] newDataPoints = new DataPoint[dataPoints.length];

			for (int j = 0; j < dataPoints.length; j++) {
				DataPoint p = dataPoints[j];
				int bin = mzRange.binNumber(numBins, p.getMZ());
				final double baselineIntenstity = chromatogram[bin][i];

				if (chromatogramType == ChromatogramType.BASE_PEAK)
					newDataPoints[j] = baselineIntenstity <= 0.0 ? new DataPoint(
							p) : new DataPoint(p.getMZ(), Math.max(0.0,
							p.getIntensity() - baselineIntenstity));

				else if (chromatogramType == ChromatogramType.TIC)
					newDataPoints[j] = baselineIntenstity <= 0.0 ? new DataPoint(
							p) : new DataPoint(p.getMZ(), Math.max(0.0,
							p.getIntensity() * (1.0 - baselineIntenstity)));
			}

			spectrum.setDataPoints(newDataPoints);
		}
	}

	/**
	 * 
	 * @param spectra
	 * @return
	 */
	private double[][] buildChromatograms(List<Spectrum> spectra,
			Range mzRange, int numBins) {
		// Return an empty chromatogram if the list of spectra is empty
		if (spectra.size() == 0)
			return new double[0][0];

		// Create chromatograms.
		double[][] chromatograms = new double[numBins][spectra.size()];

		for (int i = 0; i < spectra.size(); i++) {
			Spectrum spectrum = spectra.get(i);

			// Process data points
			for (DataPoint p : spectrum.getDataPoints()) {
				int bin = mzRange.binNumber(numBins, p.getMZ());

				if (chromatogramType == ChromatogramType.BASE_PEAK)
					chromatograms[bin][i] = Math.max(chromatograms[bin][i],
							p.getIntensity());
				else if (chromatogramType == ChromatogramType.TIC)
					chromatograms[bin][i] += p.getIntensity();
			}
		}

		return chromatograms;
	}

	/**
	 * 
	 * @return
	 */
	public List<Spectrum> getSpectra() {
		return spectra;
	}

}