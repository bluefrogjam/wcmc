package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.peakpicking;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.MSDialProcessingProperties;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakAreaBean;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakDetectionResult;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.LCMSDataAccessUtility;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.SmoothingMethod;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DataDependentPeakSpotting {

    private static Logger logger = LoggerFactory.getLogger(DataDependentPeakSpotting.class);

    /**
     * @param spectrumList the sample file that holds the list of MSSpectra
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
    public List<PeakAreaBean> getPeaks(List<Feature> spectrumList, MSDialProcessingProperties properties) {

        logger.info("Starting peak spotting...");
        logger.debug("spectra: " + spectrumList.size());

        List<double[]> peakList;
        List<List<PeakAreaBean>> detectedPeaksList = new ArrayList<>();
        List<PeakAreaBean> detectedPeaks;

        double[] mzRange = LCMSDataAccessUtility.getMS1ScanRange(spectrumList, properties.ionMode);
        double startMass = mzRange[0];
        double endMass = mzRange[1];
        logger.debug("Scan range: [" + startMass + ", " + endMass + "]");

        double focusedMass = startMass, massStep = properties.massSliceWidth;

        while (focusedMass < endMass) {
            if (focusedMass < properties.massRangeBegin) {
                focusedMass += massStep;
                continue;
            } else  if (focusedMass > properties.massRangeEnd) {
                break;
            }

            logger.debug("focusedMass: " + focusedMass);

            // Get EIC chromatogram
            peakList = LCMSDataAccessUtility.getMS1PeakList(spectrumList, focusedMass, properties.massSliceWidth,
                    properties.retentionTimeBegin, properties.retentionTimeEnd, properties.ionMode);
            logger.debug("EIC(" + focusedMass + "): " + peakList.size());

            if (peakList.isEmpty()) {
                focusedMass += massStep;
                continue;
            }

            // Get peak detection result
            detectedPeaks = getPeakAreaBeanList(spectrumList, peakList, focusedMass,
                    SmoothingMethod.valueOf(properties.smoothingMethod.toUpperCase()), properties);
            logger.debug("PeakDetection(" + focusedMass + "): " + detectedPeaks.size());

            if (detectedPeaks.isEmpty()) {
                focusedMass += massStep;
                continue;
            }

            // Filter noise peaks considering smoothing effects
            detectedPeaks = filterPeaksByRawChromatogram(peakList, detectedPeaks);
            logger.debug("SmoothingFilter(" + focusedMass + "): " + detectedPeaks.size());

            if (detectedPeaks.isEmpty()) {
                focusedMass += massStep;
                continue;
            }

            // Filtering noise peaks considering baseline effects
            detectedPeaks = getBackgroundSubtractPeaks(detectedPeaks, peakList, properties.backgroundSubtraction);
            logger.debug("BackgroundSubtract(" + focusedMass + "): " + detectedPeaks.size());

            if (detectedPeaks.isEmpty()) {
                focusedMass += massStep;
                continue;
            }

            // Removing peak spot redundancies among slices
            detectedPeaks = removePeakAreaBeanRedundancy(detectedPeaksList, detectedPeaks, massStep);

            if (!detectedPeaks.isEmpty()) {
                detectedPeaksList.add(detectedPeaks);
            }

            focusedMass += massStep;
        }

        detectedPeaks = getCombinedPeakAreaBeanList(detectedPeaksList);
        detectedPeaks = getPeakAreaBeanProperties(detectedPeaks, spectrumList, properties);

        logger.debug("Final detected: " + detectedPeaks.size());

        return detectedPeaks;
    }


    /**
     *
     * @param spectrumList
     * @param peakList
     * @param focusedMass
     * @param smoothingMethod
     * @param properties
     * @return
     */
    private List<PeakAreaBean> getPeakAreaBeanList(List<Feature> spectrumList, List<double[]> peakList, double focusedMass,
                                                   SmoothingMethod smoothingMethod, MSDialProcessingProperties properties) {


        List<double[]> smoothedPeakList = LCMSDataAccessUtility.getSmoothedPeakArray(peakList, smoothingMethod, properties.smoothingLevel);

        List<PeakDetectionResult> detectedPeaks = DifferentialBasedPeakDetection.detectPeaks(smoothedPeakList,
                properties.minimumDataPoints, properties.minimumAmplitude, properties.amplitudeNoiseFactor,
                properties.slopeNoiseFactor, properties.peaktopNoiseFactor);

        if (detectedPeaks.isEmpty()) {
            return Collections.emptyList();
        }

        List<PeakAreaBean> peakAreaBeanList = new ArrayList<>();
        boolean excludeChecker = false;

        for (PeakDetectionResult detectedPeak : detectedPeaks) {
            if (detectedPeak.intensityAtPeakTop <= 0)
                continue;

            // TODO: Hiroshi has added an excluded mass list checker, which has not been implemented as it is not essential to the peak peaking
            PeakAreaBean peakAreaBean = LCMSDataAccessUtility.getPeakAreaBean(detectedPeak);
            peakAreaBean.accurateMass = peakList.get(detectedPeak.scanNumAtPeakTop)[2];
            peakAreaBean.ms1LevelDataPointNumber = (int)peakList.get(detectedPeak.scanNumAtPeakTop)[0];
            peakAreaBean.ms2LevelDataPointNumber = LCMSDataAccessUtility.getMS2DatapointNumber(
                    (int)peakList.get(detectedPeak.scanNumAtLeftPeakEdge)[0], (int)peakList.get(detectedPeak.scanNumAtRightPeakEdge)[0],
                    (float)peakList.get(detectedPeak.scanNumAtPeakTop)[2], properties.centroidMS1Tolerance, spectrumList, properties.ionMode);
            peakAreaBeanList.add(peakAreaBean);
        }

        return peakAreaBeanList;
    }

    /**
     * @param peakList
     * @param detectedPeakAreas
     * @return
     */
    private static List<PeakAreaBean> filterPeaksByRawChromatogram(List<double[]> peakList, List<PeakAreaBean> detectedPeakAreas) {
        List<PeakAreaBean> newPeakAreas = new ArrayList<>();

        logger.trace("Peak list size: "+ peakList.size() +", Detected peaks size: "+ detectedPeakAreas.size());


        for (PeakAreaBean peak : detectedPeakAreas) {
            int scanNum = peak.scanNumberAtPeakTop;

            if (scanNum - 1 < 0 || scanNum + 1 > peakList.size() - 1)
                continue;

            if (peakList.get(scanNum - 1)[3] <= 0 || peakList.get(scanNum + 1)[3] <= 0)
                continue;

            logger.trace("Retaining scan #"+ peak.scanNumberAtPeakTop);
            newPeakAreas.add(peak);
        }

        return newPeakAreas;
    }

    /**
     * @param detectedPeakAreas
     * @param peakList
     * @param backgroundSubtraction
     * @return
     */
    private static List<PeakAreaBean> getBackgroundSubtractPeaks(List<PeakAreaBean> detectedPeakAreas, List<double[]> peakList, boolean backgroundSubtraction) {
        if (!backgroundSubtraction)
            return detectedPeakAreas;

        int counterThreshold = 4;
        List<PeakAreaBean> sPeakAreaList = new ArrayList<>();

        for (PeakAreaBean peakArea : detectedPeakAreas) {
            int peakTop = peakArea.scanNumberAtPeakTop;
            int peakLeft = peakArea.scanNumberAtLeftPeakEdge;
            int peakRight = peakArea.scanNumberAtRightPeakEdge;

            int trackingNumber = 10 * (peakRight - peakLeft);
            if (trackingNumber > 50)
                trackingNumber = 50;

            double ampDiff = Math.max(peakArea.intensityAtPeakTop - peakArea.intensityAtLeftPeakEdge, peakArea.intensityAtPeakTop - peakArea.intensityAtRightPeakEdge);
            int counter = 0;

            double spikeMax = -1, spikeMin = -1;

            for (int i = peakLeft - trackingNumber; i <= peakLeft; i++) {
                if (i - 1 < 0)
                    continue;

                if (peakList.get(i - 1)[3] < peakList.get(i)[3] && peakList.get(i)[3] > peakList.get(i + 1)[3]) {
                    spikeMax = peakList.get(i)[3];
                } else if (peakList.get(i - 1)[3] > peakList.get(i)[3] && peakList.get(i)[3] < peakList.get(i + 1)[3]) {
                    spikeMin = peakList.get(i)[3];
                }

                if (spikeMax != -1 && spikeMin != -1) {
                    double noise = 0.5 * Math.abs(spikeMax - spikeMin);
                    if (noise * 3 > ampDiff)
                        counter++;

                    spikeMax = -1;
                    spikeMin = -1;
                }
            }

            for (int i = peakRight; i <= peakRight + trackingNumber; i++) {
                if (i + 1 > peakList.size() - 1)
                    break;

                if (peakList.get(i - 1)[3] < peakList.get(i)[3] && peakList.get(i)[3] > peakList.get(i + 1)[3]) {
                    spikeMax = peakList.get(i)[3];
                } else if (peakList.get(i - 1)[3] > peakList.get(i)[3] && peakList.get(i)[3] < peakList.get(i + 1)[3]) {
                    spikeMin = peakList.get(i)[3];
                }

                if (spikeMax != -1 && spikeMin != -1) {
                    double noise = 0.5 * Math.abs(spikeMax - spikeMin);
                    if (noise * 3 > ampDiff) counter++;
                    spikeMax = -1;
                    spikeMin = -1;
                }
            }

            if (counter < counterThreshold)
                sPeakAreaList.add(peakArea);
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
        if (detectedPeakAreasList.isEmpty())
            return detectedPeakAreas;

        List<PeakAreaBean> parentPeakAreaBeanList = detectedPeakAreasList.get(detectedPeakAreasList.size() - 1);

        for (int i = 0; i < detectedPeakAreas.size(); i++) {
            for (int j = 0; j < parentPeakAreaBeanList.size(); j++) {
                if (Math.abs(parentPeakAreaBeanList.get(j).accurateMass - detectedPeakAreas.get(i).accurateMass) <= massStep * 0.5) {
                    boolean isOverlapped = isOverlapedChecker(parentPeakAreaBeanList.get(j), detectedPeakAreas.get(i));

                    if (isOverlapped)
                        continue;

                    double hwhm = ((parentPeakAreaBeanList.get(j).rtAtRightPeakEdge - parentPeakAreaBeanList.get(j).rtAtLeftPeakEdge) +
                            (detectedPeakAreas.get(i).rtAtRightPeakEdge - detectedPeakAreas.get(i).rtAtLeftPeakEdge)) * 0.25;

                    double tolerance = Math.min(hwhm, 0.03);

                    if (Math.abs(parentPeakAreaBeanList.get(j).rtAtPeakTop - detectedPeakAreas.get(i).rtAtPeakTop) <= tolerance) {
                        if (detectedPeakAreas.get(i).intensityAtPeakTop > parentPeakAreaBeanList.get(j).intensityAtPeakTop) {
                            parentPeakAreaBeanList.remove(j);
                            j--;
                            continue;
                        } else {
                            detectedPeakAreas.remove(i);
                            i--;
                            break;
                        }
                    }
                }
            }

            if (parentPeakAreaBeanList.isEmpty())
                return detectedPeakAreas;

            if (detectedPeakAreas.isEmpty())
                return Collections.emptyList();
        }

        return detectedPeakAreas;
    }

    /**
     *
     * @param peakA
     * @param peakB
     * @return
     */
    private static boolean isOverlapedChecker(PeakAreaBean peakA, PeakAreaBean peakB) {
        if (peakA.rtAtPeakTop > peakB.rtAtPeakTop) {
            if (peakA.rtAtLeftPeakEdge < peakB.rtAtPeakTop)
                return true;
        } else {
            if (peakA.rtAtLeftPeakEdge < peakB.rtAtPeakTop)
                return true;
        }

        return false;
    }

    /**
     * @param detectedPeaksList
     * @return
     */
    private static List<PeakAreaBean> getCombinedPeakAreaBeanList(List<List<PeakAreaBean>> detectedPeaksList) {
        List<PeakAreaBean> combinedPeakAreaBeanList = new ArrayList<>();
        Collections.addAll(detectedPeaksList);

        return combinedPeakAreaBeanList;
    }

    /**
     *
     * @param peakAreaBeanList
     * @param spectrumList
     * @param properties
     * @return
     */
    private static List<PeakAreaBean> getPeakAreaBeanProperties(List<PeakAreaBean> peakAreaBeanList, List<Feature> spectrumList, MSDialProcessingProperties properties) {

        peakAreaBeanList = peakAreaBeanList.stream()
                .sorted(Comparator.comparing(PeakAreaBean::rtAtPeakTop)
                .thenComparing(PeakAreaBean::accurateMass))
                .collect(Collectors.toList());

        for (int i = 0; i < peakAreaBeanList.size(); i++) {
            peakAreaBeanList.get(i).peakID = i;
            setIsotopicIonInformation(peakAreaBeanList.get(i), spectrumList, properties);
        }

        peakAreaBeanList = peakAreaBeanList.stream()
                .sorted(Comparator.comparing(PeakAreaBean::intensityAtPeakTop))
                .collect(Collectors.toList());

        if (peakAreaBeanList.size() - 1 > 0) {
            for (int i = 0; i < peakAreaBeanList.size(); i++) {
                peakAreaBeanList.get(i).amplitudeScoreValue = ((double)i / (peakAreaBeanList.size() - 1));
            }
        }

        return peakAreaBeanList.stream()
                .sorted(Comparator.comparing(PeakAreaBean::peakID))
                .collect(Collectors.toList());
    }


    private static void setIsotopicIonInformation(PeakAreaBean peakAreaBean, List<Feature> spectrumList, MSDialProcessingProperties properties) {

        int specID = peakAreaBean.ms1LevelDataPointNumber;
        double massTolerance = properties.massAccuracy;

        List<Ion> spectrum = TypeConverter.getJavaIonList(spectrumList.get(specID));
        double precursorMz = peakAreaBean.accurateMass;
        int startID = LCMSDataAccessUtility.getMs1StartIndex(precursorMz, massTolerance, spectrum);

        double ms1IsotopicIonM1PeakHeight = 0.0, ms1IsotopicIonM2PeakHeight = 0.0;

        for (int i = startID; i < spectrum.size(); i++) {
            if (spectrum.get(i).mass() <= precursorMz - 0.00632 - massTolerance)
                continue;

            if (spectrum.get(i).mass() >= precursorMz + 2.00671 + 0.005844 + massTolerance)
                break;

            if (spectrum.get(i).mass() > precursorMz + 1.00335 - 0.00632 - massTolerance && spectrum.get(i).mass() < precursorMz + 1.00335 + 0.00292 + massTolerance) {
                ms1IsotopicIonM1PeakHeight += spectrum.get(i).intensity();
            } else if (spectrum.get(i).mass() > precursorMz + 2.00671 - 0.01264 - massTolerance && spectrum.get(i).mass() < precursorMz + 2.00671 + 0.00584 + massTolerance) {
                ms1IsotopicIonM2PeakHeight += spectrum.get(i).intensity();
            }
        }

        peakAreaBean.ms1IsotopicIonM1PeakHeight = ms1IsotopicIonM1PeakHeight;
        peakAreaBean.ms1IsotopicIonM2PeakHeight = ms1IsotopicIonM2PeakHeight;
    }
}
