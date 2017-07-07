package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.deconvolution;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection.SmoothingJava;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.*;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.utils.LCMSDataAccessUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by diego on 7/21/2016.
 */
public class DataDependentDevconvolution {

	/**
	 * Entry point for the deconvolution
	 * analog to the writeMS2DecResult on SpectralDeconvolution.cs
	 *
	 * @param rawData                    the raw data (Collection<RDAM_Spectrum>)
	 * @param detectedPeaks              list of detected peakAreas coming from peakpicking (Collection<PeakAreaBean>)
	 * @param dataTypeMS2                the data type of MS2 data (Centroided | Profile)
	 * @param centroidMs2Tolerance
	 * @param centroidMs1Tolerance
	 * @param removeAfterPrecursor
	 * @param peakDetectionBasedCentroid
	 * @param amplitudeCutoff            data above this level is considered a peak
	 * @param ionMode                    + or -
	 * @param smoothingMethod
	 * @param smoothingLevel
	 * @return
	 */
	public static List<MS2DecResult> getMS2DecResult(List<RawSpectrum> rawData, List<PeakAreaBean> detectedPeaks, MSDataType dataTypeMS2,
	                                                 double centroidMs2Tolerance, double centroidMs1Tolerance,
	                                                 boolean removeAfterPrecursor, boolean peakDetectionBasedCentroid,
	                                                 double amplitudeCutoff, char ionMode,
	                                                 SmoothingMethod smoothingMethod, int smoothingLevel) {
		List<MS2DecResult> deconvolutionResults = new ArrayList<>();

		for (PeakAreaBean currentPeak : detectedPeaks) {
			deconvolutionResults.add(getMS2DecResultOnDataDependentAcquisition(rawData, currentPeak, dataTypeMS2, centroidMs2Tolerance, centroidMs1Tolerance, removeAfterPrecursor, peakDetectionBasedCentroid, amplitudeCutoff, ionMode, smoothingMethod, smoothingLevel));
		}

		return deconvolutionResults;
	}

	/**
	 * @param rawData
	 * @param currentPeak
	 * @param dataTypeMS2
	 * @param centroidMs2Tolerance
	 * @param centroidMs1Tolerance
	 * @param removeAfterPrecursor
	 * @param peakDetectionBasedCentroid
	 * @param amplitudeCutoff
	 * @param ionMode
	 * @param smoothingMethod
	 * @param smoothingLevel
	 * @return
	 */
	private static MS2DecResult getMS2DecResultOnDataDependentAcquisition(List<RawSpectrum> rawData, PeakAreaBean currentPeak, MSDataType dataTypeMS2,
	                                                                      double centroidMs2Tolerance, double centroidMs1Tolerance,
	                                                                      boolean removeAfterPrecursor, boolean peakDetectionBasedCentroid,
	                                                                      double amplitudeCutoff, char ionMode,
	                                                                      SmoothingMethod smoothingMethod, int smoothingLevel) {
		MS2DecResult ms2DecResult = new MS2DecResult();
		ms2DecResult.ms1IsotopicIonM1PeakHeight = currentPeak.ms1IsotopicIonM1PeakHeight;
		ms2DecResult.ms1IsotopicIonM2PeakHeight = currentPeak.ms1IsotopicIonM2PeakHeight;
		ms2DecResult.peakTopRetentionTime = currentPeak.rtAtPeakTop;
		ms2DecResult.ms1AccurateMass = currentPeak.accurateMass;
		ms2DecResult.ms2DecPeakArea = -1;
		ms2DecResult.ms2DecPeakHeight = -1;
		ms2DecResult.ms1PeakHeight = currentPeak.intensityAtPeakTop;
		ms2DecResult.uniqueMs = -1;

		// no MS2 data
		if (currentPeak.ms2LevelDatapointNumber < 0) {
			ms2DecResult.spectrum = new ArrayList<>();
			ms2DecResult.peaklistList.add(new ArrayList<>());
		} else {    // MS2 data
			double startRt = currentPeak.rtAtPeakTop - (currentPeak.rtAtRightPeakEdge - currentPeak.rtAtLeftPeakEdge);
			double endRt = currentPeak.rtAtPeakTop + (currentPeak.rtAtRightPeakEdge - currentPeak.rtAtLeftPeakEdge);
			double precursorMz = currentPeak.accurateMass;
			double productMz;

			List<Ion> centroidedSpectra = LCMSDataAccessUtility.getCentroidedMassSpectra(rawData, dataTypeMS2, currentPeak.ms2LevelDatapointNumber, centroidMs2Tolerance, peakDetectionBasedCentroid);

			//TODO: check if results are sorted by mass to skip next line.
			Collections.sort(centroidedSpectra, Comparator.comparing(Ion::mass));

			if (centroidedSpectra.size() != 0) {
				for (Ion theIon : centroidedSpectra) {
					productMz = theIon.mass;

					if (removeAfterPrecursor && currentPeak.accurateMass + 0.2 < productMz) continue;
					if (amplitudeCutoff > theIon.intensity) continue;

					List<double[]> ms2Peaklist = LCMSDataAccessUtility.getMs2Peaklist(rawData, precursorMz, productMz, startRt, endRt, ionMode, centroidMs2Tolerance, centroidMs1Tolerance);
					ms2Peaklist.add(0, new double[]{0, startRt, productMz, 0});
					ms2Peaklist.add(new double[]{0, endRt, productMz, 0});
					ms2Peaklist = SmoothingJava.smooth(ms2Peaklist, smoothingMethod, smoothingLevel, true);

					ms2DecResult.peaklistList.add(ms2Peaklist);
					ms2DecResult.spectrum.add(theIon);
				}

				if (ms2DecResult.spectrum.size() == 0) {
					ms2DecResult = getMS2DecResultByOnlyMs1Information(currentPeak);
				}
			} else {
				ms2DecResult = getMS2DecResultByOnlyMs1Information(currentPeak);
			}
		}

		return ms2DecResult;
	}

	private static MS2DecResult getMS2DecResultByOnlyMs1Information(PeakAreaBean currentPeak) {
		MS2DecResult ms2DecResult = new MS2DecResult();

		ms2DecResult.ms1IsotopicIonM1PeakHeight = currentPeak.ms1IsotopicIonM1PeakHeight;
		ms2DecResult.ms1IsotopicIonM2PeakHeight = currentPeak.ms1IsotopicIonM2PeakHeight;
		ms2DecResult.peakTopRetentionTime = currentPeak.rtAtPeakTop;
		ms2DecResult.ms1AccurateMass = currentPeak.accurateMass;
		ms2DecResult.ms1PeakHeight = currentPeak.intensityAtPeakTop;
		ms2DecResult.uniqueMs = -1;
		ms2DecResult.ms2DecPeakArea = -1;
		ms2DecResult.ms2DecPeakHeight = -1f;
		ms2DecResult.spectrum = new ArrayList<>();
		ms2DecResult.baseChromatogram = new ArrayList<>();
		ms2DecResult.modelMasses = new ArrayList<>();
		ms2DecResult.peaklistList.add(new ArrayList<>());

		return ms2DecResult;
	}
}
