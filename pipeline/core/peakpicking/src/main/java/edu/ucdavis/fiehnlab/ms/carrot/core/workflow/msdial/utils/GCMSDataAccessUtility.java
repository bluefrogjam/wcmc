package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.utils;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.deconvolution.MsDialPeak;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection.MSDialPreProcessingProperties;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection.PeakDetectionResult;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection.SmoothingJava;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by diego on 8/11/2016.
 */
public class GCMSDataAccessUtility extends DataAccessUtility {
	private static Logger logger = LoggerFactory.getLogger(GCMSDataAccessUtility.class);

	/**
	 * @param spectrumList
	 * @param focusedMass
	 * @param massSliceWidth
	 * @param rtBegin
	 * @param rtEnd
	 * @param ionMode
	 * @return
	 */
	public static List<double[]> getMs1SlicePeaklist(List<RawSpectrum> spectrumList, double focusedMass, double massSliceWidth, double rtBegin, double rtEnd, char ionMode) {

		List<double[]> peaklist = new ArrayList<>(spectrumList.size());

		int startIndex = 0;
		double sum = 0, maxIntensityMz, maxMass;

		for (RawSpectrum spectrum : spectrumList.stream()
				.filter(s -> s.msLevel == 1)
				.filter(s -> s.polarity == ionMode)
				.filter(s -> s.rtMin>= rtBegin && s.rtMin < rtEnd)
				.collect(Collectors.toList())) {

			List<Ion> massSpectrum = spectrum.spect;

			sum = 0;
			maxIntensityMz = Double.MIN_VALUE;
			maxMass = focusedMass;
			startIndex = getMs1StartIndex(focusedMass, massSliceWidth, massSpectrum);

			for (int j = startIndex; j < massSpectrum.size(); j++) {
				if (massSpectrum.get(j).mass < focusedMass - massSliceWidth) {
					continue;
				} else if (focusedMass - massSliceWidth <= massSpectrum.get(j).mass && massSpectrum.get(j).mass < focusedMass + massSliceWidth) {
					sum += massSpectrum.get(j).intensity;
					if (maxIntensityMz < massSpectrum.get(j).intensity) {
						maxIntensityMz = massSpectrum.get(j).intensity;
						maxMass = massSpectrum.get(j).mass;
					}
				} else if (massSpectrum.get(j).mass >= focusedMass + massSliceWidth) {
					break;
				}
			}

			if (spectrumList.get(0).scanNum == 0) {
				peaklist.add(new double[]{spectrum.scanNum, spectrum.rtMin, maxMass, sum});
			} else {
				peaklist.add(new double[]{spectrum.scanNum - 1, spectrum.rtMin, maxMass, sum});
			}
		}

		return peaklist;
	}

	public static double[] getMs1ScanRange(List<RawSpectrum> spectrumList, char ionmode) {
		double minMz = Double.MAX_VALUE;
		double maxMz = Double.MIN_VALUE;

		List<RawSpectrum> filteredSpec = spectrumList.stream()
				.filter(n -> n.msLevel == 1)
				.filter(n -> n.polarity == ionmode)
				.collect(Collectors.toList());

		logger.trace("\tfiltered specs: " + filteredSpec.size());

		for (int i = 0; i < spectrumList.size(); i++) {
			if (spectrumList.get(i).mzLow < minMz) minMz = spectrumList.get(i).mzLow;
			if (spectrumList.get(i).mzHigh > maxMz) maxMz = spectrumList.get(i).mzHigh;
		}

		logger.trace(String.format("\tFound minMz: %.5f, maxMz: %.5f", minMz, maxMz));

		return new double[]{minMz, maxMz};
	}

	/**
	 * @param targetMass
	 * @param ms1Tolerance
	 * @param massSpectra
	 * @return
	 */
	public static int getMs1StartIndex(double targetMass, double ms1Tolerance, List<Ion> massSpectra) {
		return getStartIndexForTargetMass(targetMass, massSpectra, ms1Tolerance);
	}


	/**
	 * @param targetScan
	 * @param detectedPeaks
	 * @return
	 */
	public static int getMs1StartIndex(int targetScan, List<PeakAreaBean> detectedPeaks) {
		int startIndex = 0, endIndex = detectedPeaks.size() - 1;

		int counter = 0;
		while (counter < 5) {
			if (detectedPeaks.get(startIndex).scanNumberAtPeakTop <= targetScan && targetScan < detectedPeaks.get((startIndex + endIndex) / 2).scanNumberAtPeakTop) {
				endIndex = (startIndex + endIndex) / 2;
			} else if (detectedPeaks.get((startIndex + endIndex) / 2).scanNumberAtPeakTop <= targetScan && targetScan < detectedPeaks.get(endIndex).scanNumberAtPeakTop) {
				startIndex = (startIndex + endIndex) / 2;
			}
			counter++;
		}
		return startIndex;
	}

	public static List<MsDialPeak> getSmoothedPeaklist(List<MsDialPeak> peaklist, MSDialPreProcessingProperties properties) {

		return SmoothingJava.smoothPeaks(peaklist, SmoothingMethod.valueOf(properties.smoothingMethod.toUpperCase()), properties.smoothingLevel, true);
	}

	public static List<double[]> getSmoothedPeakArray(List<double[]> peaklist, SmoothingMethod smoothingMethod, int smoothingLevel) {
		//called from peakpeaking
		return SmoothingJava.smooth(peaklist, smoothingMethod, smoothingLevel, false);
	}

	/**
	 * @param spectrumList
	 * @param msScanPoint
	 * @param massBin
	 * @return
	 */
	public static List<Ion> getCentroidMassSpectra(List<RawSpectrum> spectrumList, int msScanPoint, double massBin, MSDialPreProcessingProperties properties) {
		if (msScanPoint < 0) return new ArrayList<>();

		List<Ion> spectra = new ArrayList<>();
		List<Ion> massSpectra = spectrumList.get(msScanPoint).spect;

		for (Ion ion : massSpectra) {
			if (ion.mass < properties.massRangeBegin) continue;
			if (ion.mass > properties.massRangeEnd) continue;
			spectra.add(new Ion(ion.mass, ion.intensity));
		}

		if (MSDataType.valueOf(properties.dataType.toUpperCase()) == MSDataType.CENTROID) {
			return spectra.stream().filter(n -> n.intensity > properties.amplitudeCutoff).collect(Collectors.toList());
		}

		if (spectra.size() == 0) return new ArrayList<>();

		List<Ion> centroidedSpectra = SpectralCentroiding.centroid(spectra, massBin, properties.amplitudeCutoff);

		if (centroidedSpectra != null && centroidedSpectra.size() != 0)
			return centroidedSpectra;
		else
			return spectra;
	}



	public static PeakAreaBean getDetectedPeakArea(PeakDetectionResult peakResult) {
		if (peakResult == null) return null;

		PeakAreaBean pab = new PeakAreaBean();
		pab.amplitudeOrderValue = peakResult.amplitudeOrderValue;
		pab.amplitudeScoreValue = peakResult.amplitudeScoreValue;
		pab.areaAboveBaseline = peakResult.areaAboveBaseline;
		pab.areaAboveZero = peakResult.areaAboveZero;
		pab.basePeakValue = peakResult.basePeakValue;
		pab.gaussianSimilarityValue = peakResult.gaussianSimilarityValue;
		pab.idealSlopeValue = peakResult.idealSlopeValue;
		pab.intensityAtLeftPeakEdge = peakResult.intensityAtLeftPeakEdge;
		pab.intensityAtPeakTop = peakResult.intensityAtPeakTop;
		pab.intensityAtRightPeakEdge = peakResult.intensityAtRightPeakEdge;
		pab.peakID = peakResult.peakID;
		pab.peakPureValue = peakResult.peakPureValue;
		pab.rtAtLeftPeakEdge = peakResult.rtAtLeftPeakEdge;
		pab.rtAtPeakTop = peakResult.rtAtPeakTop;
		pab.rtAtRightPeakEdge = peakResult.rtAtRightPeakEdge;
		pab.scanNumberAtLeftPeakEdge = peakResult.scanNumAtLeftPeakEdge;
		pab.scanNumberAtPeakTop = peakResult.scanNumAtPeakTop;
		pab.scanNumberAtRightPeakEdge = peakResult.scanNumAtRightPeakEdge;
		pab.sharpenessValue = peakResult.sharpnessValue;
		pab.symmetryValue = peakResult.symmetryValue;
		pab.normalizedValue = -1;
		pab.accurateMass = -1;
		pab.ms1LevelDatapointNumber = -1;
		pab.ms2LevelDatapointNumber = -1;
//		pab.alignedRetentionTime = -1;
//		pab.totalScore = -1;
//		pab.metaboliteName = "";
//		pab.adductIonName = "";
//		pab.libraryID = -1;
//		pab.isotopeWeightNumber = -1;
//		pab.isotopeParentPeakID = -1;
//		pab.adductParent = -1;
//		pab.rtSimilarityValue = -1;
//		pab.isotopeSimilarityValue = -1;
//		pab.massSpectraSimilarityValue = -1;
//		pab.reverseSearchSimilarityValue = -1;
//		pab.presenseSimilarityValue = -1;
//		pab.adductIonAccurateMass = -1;
//		pab.adductIonXmer = -1;
//		pab.adductIonChargeNumber = -1;

		return pab;
	}
}
