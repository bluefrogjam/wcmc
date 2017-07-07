package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.isotopeEstimation.IsotopeEstimator;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.*;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.utils.GCMSDataAccessUtility;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.utils.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by diego on 8/25/2016.
 */
@Component
@ComponentScan(basePackages = {"edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.utils"})
public class MsdialGCBasedPeakSpotting {
	private static Logger logger = LoggerFactory.getLogger(MsdialGCBasedPeakSpotting.class);

	@Autowired
	MSDialPreProcessingProperties mSDialPreProcessingProperties = null;

	@Autowired
	TypeConverter converter = null;

	/**
	 * @param spectrumList the sample file that holds the list of <RawSpectrum>
	 * @param properties   List of peak picking properties as follows:
	 *                     massSliceWidth        bin size of mz scale where to look for peaks
	 *                     massResolution        Accurate or Nominal
	 *                     massRangeBegin        mass value at which to start looking for peaks
	 *                     massRangeEnd          mass value at which to stop looking for peaks
	 *                     retentionTimeBegin    rt value at which to start looking for peaks
	 *                     retentionTimeEnd      rt value at which to stop looking for peaks
	 *                     smoothingMethod       method used to smooth the chromatogram
	 *                     smoothingLevel        how much smoothing is needed
	 *                     minimumDataPoints     minimum number of scans for a peak to be considered as such
	 *                     minimumAmplitude      minimum intensity for a peak to be considered as such
	 *                     amplitudeNoiseFactor  the peak has to be this many times bigger than the local noise
	 *                     slopeNoiseFactor      ??
	 *                     peaktopNoiseFactor    ??
	 *                     averagePeakWidth      average ammount of scans in a peak
	 *                     backgroundSubtraction substract the background? true or false
	 *                     massAccuracy          minimum different in mass to be considered different
	 *                     dataType              ProfileData or CentroidData
	 *                     amplitudeCutoff       minimum fragment intensity for centroiding
	 * @return detected peak areas
	 */
	public List<PeakAreaBean> getPeakSpots(List<RawSpectrum> spectrumList, MSDialPreProcessingProperties properties) {
		logger.debug("peakpicking...");

		List<double[]> ms1peaklist;
		List<List<PeakAreaBean>> detectedPeaksList = new ArrayList<>();
		List<PeakAreaBean> detectedPeaks;

		double[] mzRange = GCMSDataAccessUtility.getMs1ScanRange(spectrumList, properties.ionMode);
		double startMass = mzRange[0];
		double endMass = mzRange[1];
		logger.trace("Scan range: [" + startMass + ", " + endMass + "]");

		double focusedMass = startMass, massStep = properties.massSliceWidth, sliceWidth = properties.massSliceWidth;
		if (properties.massResolution.equals(MassResolution.NOMINAL.toString())) {
			focusedMass = (int) focusedMass;
			massStep = 1.0F;
			sliceWidth = 0.5F;
		}

		while (focusedMass < endMass) {
			if (focusedMass < properties.massRangeBegin) {
				focusedMass += massStep;
				continue;
			}
			if (focusedMass > properties.massRangeEnd) break;

			logger.trace("spectrumList size: " + spectrumList.size());
			logger.trace("focusedMass: " + focusedMass);
			ms1peaklist = GCMSDataAccessUtility.getMs1SlicePeaklist(spectrumList, focusedMass, sliceWidth, properties.retentionTimeBegin, properties.retentionTimeEnd, properties.ionMode);
			if (ms1peaklist.size() == 0) {
				focusedMass += massStep;
				continue;
			}

			detectedPeaks = getPeakAreaBeanList(spectrumList, ms1peaklist, focusedMass, SmoothingMethod.valueOf(properties.smoothingMethod.toUpperCase()), properties.smoothingLevel,
					properties.minimumDataPoints, properties.minimumAmplitude, properties.amplitudeNoiseFactor,
					properties.slopeNoiseFactor, properties.peaktopNoiseFactor, properties.averagePeakWidth);
			if (detectedPeaks == null || detectedPeaks.size() == 0) {
				focusedMass += massStep;
				continue;
			}
			logger.trace("detected (" + focusedMass + "): " + detectedPeaks.size());

			detectedPeaks = filterPeaksByRawChromatogram(ms1peaklist, detectedPeaks);
			if (detectedPeaks == null || detectedPeaks.size() == 0) {
				focusedMass += massStep;
				continue;
			}
			logger.trace("filtered (" + focusedMass + "): " + detectedPeaks.size());

			detectedPeaks = getBackgroundSubtractPeaks(detectedPeaks, ms1peaklist, properties.backgroundSubtraction);
			if (detectedPeaks == null || detectedPeaks.size() == 0) {
				focusedMass += massStep;
				continue;
			}
			logger.trace("de-noised (" + focusedMass + "): " + detectedPeaks.size());

			if (properties.massResolution.toUpperCase().equals("accurate".toUpperCase())) {
				detectedPeaks = removePeakAreaBeanRedundancy(detectedPeaksList, detectedPeaks, massStep);
				if (detectedPeaks == null || detectedPeaks.size() == 0) {
					focusedMass += massStep;
					continue;
				}
				logger.trace("not-redundant (" + focusedMass + "): " + detectedPeaks.size());
			}

			detectedPeaksList.add(detectedPeaks);
			focusedMass += massStep;
		}

		detectedPeaks = getCombinedPeakAreaBeanList(detectedPeaksList);
		detectedPeaks = getPeakAreaBeanProperties(spectrumList, detectedPeaks, properties);

		IsotopeEstimator.setIsotopeInformation(detectedPeaks, properties.massAccuracy, MassResolution.valueOf(properties.massResolution.toUpperCase()));
		logger.trace("final detected: " + detectedPeaks.size());

		return detectedPeaks;
	}

	/**
	 * @param spectrumList
	 * @param ms1peaklist
	 * @param focusedMass
	 * @param smoothingMethod
	 * @param smoothingLevel
	 * @param minimumDataPoints
	 * @param minimumAmplitude
	 * @param amplitudeNoiseFactor
	 * @param slopeNoiseFactor
	 * @param peaktopNoiseFactor
	 * @param averagePeakWidth
	 * @return
	 */
	private static List<PeakAreaBean> getPeakAreaBeanList(List<RawSpectrum> spectrumList, List<double[]> ms1peaklist, double focusedMass, SmoothingMethod smoothingMethod, int smoothingLevel,
	                                                      double minimumDataPoints, double minimumAmplitude, double amplitudeNoiseFactor, double slopeNoiseFactor, double peaktopNoiseFactor, int averagePeakWidth) {

		List<double[]> smoothedPeaklist = GCMSDataAccessUtility.getSmoothedPeakArray(ms1peaklist, smoothingMethod, smoothingLevel);

		List<PeakDetectionResult> detectedPeaks = DifferentialBasedPeakDetection.detectGCPeaks(smoothedPeaklist, minimumDataPoints, minimumAmplitude,
				amplitudeNoiseFactor, slopeNoiseFactor, peaktopNoiseFactor, averagePeakWidth);

		if (detectedPeaks == null || detectedPeaks.size() == 0) return null;

		List<PeakAreaBean> peakAreaBeanList = new ArrayList<>();

		for (PeakDetectionResult detectedPeak : detectedPeaks) {
			if (detectedPeak.intensityAtPeakTop <= 0) continue;

			PeakAreaBean peakAreaBean = GCMSDataAccessUtility.getDetectedPeakArea(detectedPeak);
			peakAreaBean.accurateMass = ms1peaklist.get(detectedPeak.scanNumAtPeakTop)[2];
			peakAreaBean.ms1LevelDatapointNumber = (int) ms1peaklist.get(detectedPeak.scanNumAtPeakTop)[0];
			peakAreaBeanList.add(peakAreaBean);
		}

		return peakAreaBeanList;
	}

	/**
	 * @param ms1peaklist
	 * @param detectedPeakAreas
	 * @return
	 */
	private static List<PeakAreaBean> filterPeaksByRawChromatogram(List<double[]> ms1peaklist, List<PeakAreaBean> detectedPeakAreas) {
		List<PeakAreaBean> newPeakAreas = new ArrayList<>();

		logger.trace("size of ms1pl: " + ms1peaklist.size() + " detectedPeaks: " + detectedPeakAreas.size());
		for (PeakAreaBean peak : detectedPeakAreas) {
			logger.trace("Filtering scan#: " + peak.scanNumberAtPeakTop);
			int scanNum = peak.scanNumberAtPeakTop;
			if (scanNum - 1 < 0 || scanNum + 1 > ms1peaklist.size() - 1) continue;
			if (ms1peaklist.get(scanNum - 1)[3] <= 0 || ms1peaklist.get(scanNum + 1)[3] <= 0)
				continue;

			newPeakAreas.add(peak);
		}

		return newPeakAreas;
	}

	/**
	 * @param detectedPeakAreas
	 * @param ms1peaklist
	 * @param backgroundSubtraction
	 * @return
	 */
	private static List<PeakAreaBean> getBackgroundSubtractPeaks(List<PeakAreaBean> detectedPeakAreas, List<double[]> ms1peaklist, boolean backgroundSubtraction) {
		if (!backgroundSubtraction) return detectedPeakAreas;

		int counterThreshold = 4;
		List<PeakAreaBean> sPeakAreaList = new ArrayList<>();

		for (PeakAreaBean peakArea : detectedPeakAreas) {
			int peakTop = peakArea.scanNumberAtPeakTop;
			int peakLeft = peakArea.scanNumberAtLeftPeakEdge;
			int peakRight = peakArea.scanNumberAtRightPeakEdge;
			int trackingNumber = 10 * (peakRight - peakLeft);
			if (trackingNumber > 50) trackingNumber = 50;

			double ampDiff = Math.max(peakArea.intensityAtPeakTop - peakArea.intensityAtLeftPeakEdge, peakArea.intensityAtPeakTop - peakArea.intensityAtRightPeakEdge);
			int counter = 0;

			double spikeMax = -1, spikeMin = -1;
			for (int i = peakLeft - trackingNumber; i <= peakLeft; i++) {
				if (i - 1 < 0) continue;

				if (ms1peaklist.get(i - 1)[3] < ms1peaklist.get(i)[3] && ms1peaklist.get(i)[3] > ms1peaklist.get(i + 1)[3]) {
					spikeMax = ms1peaklist.get(i)[3];
				} else if (ms1peaklist.get(i - 1)[3] > ms1peaklist.get(i)[3] && ms1peaklist.get(i)[3] < ms1peaklist.get(i + 1)[3]) {
					spikeMin = ms1peaklist.get(i)[3];
				}

				if (spikeMax != -1 && spikeMin != -1) {
					double noise = 0.5 * Math.abs(spikeMax - spikeMin);
					if (noise * 3 > ampDiff) counter++;
					spikeMax = -1;
					spikeMin = -1;
				}
			}

			for (int i = peakRight; i <= peakRight + trackingNumber; i++) {
				if (i + 1 > ms1peaklist.size() - 1) break;

				if (ms1peaklist.get(i - 1)[3] < ms1peaklist.get(i)[3] && ms1peaklist.get(i)[3] > ms1peaklist.get(i + 1)[3]) {
					spikeMax = ms1peaklist.get(i)[3];
				} else if (ms1peaklist.get(i - 1)[3] > ms1peaklist.get(i)[3] && ms1peaklist.get(i)[3] < ms1peaklist.get(i + 1)[3]) {
					spikeMin = ms1peaklist.get(i)[3];
				}

				if (spikeMax != -1 && spikeMin != -1) {
					double noise = 0.5 * Math.abs(spikeMax - spikeMin);
					if (noise * 3 > ampDiff) counter++;
					spikeMax = -1;
					spikeMin = -1;
				}
			}

			if (counter < counterThreshold) sPeakAreaList.add(peakArea);
		}
		return sPeakAreaList;
	}

	/**
	 * @param detectedPeakAreasList
	 * @param detectedPeakAreas
	 * @param massStep
	 * @return
	 */
	private static List<PeakAreaBean> removePeakAreaBeanRedundancy(List<List<PeakAreaBean>> detectedPeakAreasList, List<PeakAreaBean> detectedPeakAreas, double massStep) {
		if (detectedPeakAreasList == null || detectedPeakAreasList.size() == 0) return detectedPeakAreas;

		List<PeakAreaBean> parentPeakAreaBeanList = detectedPeakAreasList.get(detectedPeakAreasList.size() - 1);

		for (int i = 0; i < detectedPeakAreas.size(); i++) {
			for (int j = 0; j < parentPeakAreaBeanList.size(); j++) {
				if (Math.abs(parentPeakAreaBeanList.get(j).accurateMass - detectedPeakAreas.get(i).accurateMass) > (float) massStep)
					continue;

				if (detectedPeakAreas.get(i).scanNumberAtPeakTop - 1 <= parentPeakAreaBeanList.get(j).scanNumberAtPeakTop && parentPeakAreaBeanList.get(j).scanNumberAtPeakTop <= detectedPeakAreas.get(i).scanNumberAtPeakTop + 1) {
					if (detectedPeakAreas.get(i).intensityAtPeakTop > parentPeakAreaBeanList.get(j).intensityAtPeakTop) {
						parentPeakAreaBeanList.remove(j);
						break;
					} else {
						detectedPeakAreas.remove(i);
						i--;
						break;
					}
				}
			}
			if (parentPeakAreaBeanList.size() == 0) return detectedPeakAreas;
			if (detectedPeakAreas.size() == 0) return null;
		}
		return detectedPeakAreas;
	}

	/**
	 * @param detectedPeakAreasList
	 * @return
	 */
	private static List<PeakAreaBean> getCombinedPeakAreaBeanList(List<List<PeakAreaBean>> detectedPeakAreasList) {
		List<PeakAreaBean> combinedPeakAreaBeanList = new ArrayList<>();

		for (List<PeakAreaBean> detectedPeakAreas : detectedPeakAreasList) {
			if (detectedPeakAreas.size() == 0) continue;
			for (PeakAreaBean detectedPeakArea : detectedPeakAreas)
				combinedPeakAreaBeanList.add(detectedPeakArea);
		}

		return combinedPeakAreaBeanList;
	}


	private static List<PeakAreaBean> getPeakAreaBeanProperties(List<RawSpectrum> spectrumList, List<PeakAreaBean> peakAreaBeanList, MSDialPreProcessingProperties properties) {
		peakAreaBeanList = peakAreaBeanList.stream().sorted(Comparator.comparing(PeakAreaBean::rtAtPeakTop).thenComparing(PeakAreaBean::accurateMass)).collect(Collectors.toList());

		for (int i = 0; i < peakAreaBeanList.size(); i++) {
			peakAreaBeanList.get(i).peakID = i;
			setIsotopicIonInformation(spectrumList, peakAreaBeanList.get(i), properties);
		}

		peakAreaBeanList = peakAreaBeanList.stream().sorted(Comparator.comparing(PeakAreaBean::intensityAtPeakTop)).collect(Collectors.toList());

		if (peakAreaBeanList.size() - 1 > 0)
			for (int i = 0; i < peakAreaBeanList.size(); i++) {
				peakAreaBeanList.get(i).amplitudeScoreValue = (i / (double) (peakAreaBeanList.size() - 1));
			}

		peakAreaBeanList = peakAreaBeanList.stream().sorted(Comparator.comparing(PeakAreaBean::peakID)).collect(Collectors.toList());
		return peakAreaBeanList;
	}

	private static void setIsotopicIonInformation(List<RawSpectrum> spectrumList, PeakAreaBean detectedPeakArea, MSDialPreProcessingProperties properties) {
		int specID = detectedPeakArea.ms1LevelDatapointNumber;
		double massTolerance = properties.massAccuracy;

		if (MassResolution.valueOf(properties.massResolution.toUpperCase()) == MassResolution.NOMINAL) {
			massTolerance = 0.5;
		}

		List<Ion> spectrum = GCMSDataAccessUtility.getCentroidMassSpectra(spectrumList, specID, massTolerance, properties);
		double precursorMz = detectedPeakArea.accurateMass;
		int startID = GCMSDataAccessUtility.getMs1StartIndex(precursorMz, massTolerance, spectrum);

		double ms1IsotopicIonM1PeakHeight = 0.0, ms1IsotopicIonM2PeakHeight = 0.0;

		for (int i = startID; i < spectrum.size(); i++) {
			if (spectrum.get(i).mass <= precursorMz - 0.00632 - massTolerance) continue;
			if (spectrum.get(i).mass >= precursorMz + 2.00671 + 0.005844 + massTolerance) break;

			if (spectrum.get(i).mass > precursorMz + 1.00335 - 0.00632 - massTolerance && spectrum.get(i).mass < precursorMz + 1.00335 + 0.00292 + massTolerance)
				ms1IsotopicIonM1PeakHeight += spectrum.get(i).intensity;
			else if (spectrum.get(i).mass > precursorMz + 2.00671 - 0.01264 - massTolerance && spectrum.get(i).mass < precursorMz + 2.00671 + 0.00584 + massTolerance)
				ms1IsotopicIonM2PeakHeight += spectrum.get(i).intensity;
		}

		detectedPeakArea.ms1IsotopicIonM1PeakHeight = ms1IsotopicIonM1PeakHeight;
		detectedPeakArea.ms1IsotopicIonM2PeakHeight = (float) ms1IsotopicIonM2PeakHeight;
	}

	public List<PeakAreaBean> detectPeaks(List<RawSpectrum> rawSpectra, MSDialPreProcessingProperties properties) {

		List<PeakAreaBean> peaks = getPeakSpots(rawSpectra, properties);
		logger.debug(String.format("Detected %d peaks", peaks.size()));

		return peaks;
	}
}
