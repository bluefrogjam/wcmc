package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.peakpicking.lcms;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.MSDialProcessingProperties;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.peakpicking.PeakSpotting;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakAreaBean;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.lcms.PeakDetectionResult;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.DataAccessUtility;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.LCMSDataAccessUtility;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DataDependentPeakSpotting extends PeakSpotting {

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

        List<double[]> peakList;
        List<List<PeakAreaBean>> detectedPeaksList = new ArrayList<>();
        List<PeakAreaBean> detectedPeaks;

        double[] mzRange = DataAccessUtility.getMS1ScanRange(spectrumList, properties.ionMode);
        double startMass = mzRange[0];
        double endMass = mzRange[1];

        float focusedMass = (float) startMass, massStep = (float) properties.massSliceWidth;
        logger.debug(String.format("Scan range: [%.5f, %.5f] === Focused mass: %.5f", startMass, endMass, focusedMass));

        while (focusedMass < endMass) {
            if (focusedMass < properties.massRangeBegin) {
                focusedMass += massStep;
                continue;
            } else if (focusedMass > properties.massRangeEnd) {
                break;
            }

            // Get EIC chromatogram
            peakList = DataAccessUtility.getMS1PeakList(spectrumList, focusedMass, properties.massSliceWidth,
                properties.retentionTimeBegin, properties.retentionTimeEnd, properties.ionMode);

            if (peakList.isEmpty()) {
                focusedMass += massStep;
                continue;
            }
//            logger.trace("EIC(" + String.format("%.5f", focusedMass) + "): " + peakList.size());

            // Get peak detection result
            detectedPeaks = getPeakAreaBeanList(spectrumList, peakList, properties);

            if (detectedPeaks.isEmpty()) {
                focusedMass += massStep;
                continue;
            }
//            logger.trace("PeakDetection(" + String.format("%.5f", focusedMass) + "): " + detectedPeaks.size());

            // Filter noise peaks considering smoothing effects
            detectedPeaks = filterPeaksByRawChromatogram(peakList, detectedPeaks);

            if (detectedPeaks.isEmpty()) {
                focusedMass += massStep;
                continue;
            }
//            logger.trace("SmoothingFilter(" + String.format("%.5f", focusedMass) + "): " + detectedPeaks.size());

            // Filtering noise peaks considering baseline effects
            detectedPeaks = getBackgroundSubtractPeaks(detectedPeaks, peakList, properties.backgroundSubtraction);

            if (detectedPeaks.isEmpty()) {
                focusedMass += massStep;
                continue;
            }
//            logger.trace("BackgroundSubtract(" + String.format("%.5f", focusedMass) + "): " + detectedPeaks.size());

            // Removing peak spot redundancies among slices
            detectedPeaks = removePeakAreaBeanRedundancy(detectedPeaksList, detectedPeaks, massStep, 0.03);

            if (!detectedPeaks.isEmpty()) {
                detectedPeaksList.add(detectedPeaks);
            }
//            logger.trace("RemoveRedundancy(" + String.format("%.5f", focusedMass) + "): " + detectedPeaks.size());

            focusedMass += massStep;
        }

        logger.debug("pre-Final detected peak count: " + detectedPeaksList.size());
//        printDoublePeakList(detectedPeaksList);

        detectedPeaks = getCombinedPeakAreaBeanList(detectedPeaksList);
        logger.debug("pos-combined detected peak count: " + detectedPeaks.size());

        detectedPeaks = getPeakAreaBeanProperties(detectedPeaks, spectrumList, properties);
        logger.debug("Final detected peak count: " + detectedPeaks.size());

        return detectedPeaks;
    }

    /**
     * @param spectrumList
     * @param peakList
     * @param properties
     * @return
     */
    private List<PeakAreaBean> getPeakAreaBeanList(List<Feature> spectrumList, List<double[]> peakList, MSDialProcessingProperties properties) {

        List<double[]> smoothedPeakList = DataAccessUtility.getSmoothedPeakArray(peakList, properties.smoothingMethod, properties.smoothingLevel);

        List<PeakDetectionResult> detectedPeaks = LCMSDifferentialBasedPeakDetection.detectPeaks(smoothedPeakList,
            properties.minimumDataPoints, properties.minimumAmplitude, properties.amplitudeNoiseFactor,
            properties.slopeNoiseFactor, properties.peaktopNoiseFactor);

        if (detectedPeaks.isEmpty()) {
            return new ArrayList<>();
        }

        List<PeakAreaBean> peakAreaBeanList = new ArrayList<>();

        for (PeakDetectionResult detectedPeak : detectedPeaks) {
            if (detectedPeak.intensityAtPeakTop <= 0)
                continue;

            // TODO: Hiroshi has added an excluded mass list checker, which has not been implemented as it is not essential to the peak peaking
            PeakAreaBean peakAreaBean = DataAccessUtility.getPeakAreaBean(detectedPeak);
            peakAreaBean.accurateMass = peakList.get(detectedPeak.scanNumAtPeakTop)[2];
            peakAreaBean.ms1LevelDataPointNumber = (int) peakList.get(detectedPeak.scanNumAtPeakTop)[0];
            peakAreaBean.ms2LevelDataPointNumber = LCMSDataAccessUtility.getMS2DatapointNumber(
                (int) peakList.get(detectedPeak.scanNumAtLeftPeakEdge)[0], (int) peakList.get(detectedPeak.scanNumAtRightPeakEdge)[0],
                (float) peakList.get(detectedPeak.scanNumAtPeakTop)[2], properties.centroidMS1Tolerance, spectrumList, properties.ionMode);
            peakAreaBeanList.add(peakAreaBean);
        }

        return peakAreaBeanList;
    }

    /**
     * @param peakAreaBeanList
     * @param spectrumList
     * @param properties
     * @return
     */
    private List<PeakAreaBean> getPeakAreaBeanProperties(List<PeakAreaBean> peakAreaBeanList, List<Feature> spectrumList, MSDialProcessingProperties properties) {

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
                peakAreaBeanList.get(i).amplitudeScoreValue = ((double) i / (peakAreaBeanList.size() - 1));
            }
        }

        return peakAreaBeanList.stream()
                .sorted(Comparator.comparing(PeakAreaBean::peakID))
                .collect(Collectors.toList());
    }

    private void setIsotopicIonInformation(PeakAreaBean peakAreaBean, List<Feature> spectrumList, MSDialProcessingProperties properties) {

        int specID = peakAreaBean.ms1LevelDataPointNumber;
        double massTolerance = properties.centroidMS1Tolerance;

        List<Ion> spectrum = TypeConverter.getJavaIonList(spectrumList.get(specID));
        double precursorMz = peakAreaBean.accurateMass;
        int startID = DataAccessUtility.getMs1StartIndex(precursorMz, massTolerance, spectrum);

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
