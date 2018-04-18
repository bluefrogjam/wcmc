package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.peakpicking.gcms;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.MSDialProcessingProperties;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.peakpicking.PeakSpotting;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.AccuracyType;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakAreaBean;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.lcms.PeakDetectionResult;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.DataAccessUtility;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.SpectralCentroiding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GCMSPeakSpotting extends PeakSpotting {

    private static Logger logger = LoggerFactory.getLogger(GCMSPeakSpotting.class);

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
    public List<PeakAreaBean> getPeakSpots(List<Feature> spectrumList, MSDialProcessingProperties properties) {
        logger.info("Starting peak spotting...");

        List<double[]> peakList;
        List<List<PeakAreaBean>> detectedPeaksList = new ArrayList<>();
        List<PeakAreaBean> detectedPeaks;

        double[] mzRange = DataAccessUtility.getMS1ScanRange(spectrumList, properties.ionMode);
        double startMass = mzRange[0];
        double endMass = mzRange[1];

        float focusedMass = (float) startMass,
                massStep = (float) properties.massSliceWidth,
                sliceWidth = (float) properties.massSliceWidth;

        logger.debug(String.format("Scan range: [%.5f, %.5f] === Focused mass: %.5f", startMass, endMass, focusedMass));

        if (properties.accuracyType == AccuracyType.NOMINAL) {
            focusedMass = (int) focusedMass;
            massStep = 1.0f;
            sliceWidth = 0.5f;
        }

        while (focusedMass < endMass) {
            if (focusedMass < properties.massRangeBegin) {
                focusedMass += massStep;
                continue;
            } else if (focusedMass > properties.massRangeEnd) {
                break;
            }

            // Get EIC chromatogram
            peakList = DataAccessUtility.getMS1PeakList(spectrumList, focusedMass, sliceWidth, properties.retentionTimeBegin, properties.retentionTimeEnd, properties.ionMode);

            if (peakList.isEmpty()) {
                focusedMass += massStep;
                continue;
            }

            // Get peak detection result
            detectedPeaks = getPeakAreaBeanList(peakList, properties);

            if (detectedPeaks.isEmpty()) {
                focusedMass += massStep;
                continue;
            }

            // Filter noise peaks considering smoothing effects
            detectedPeaks = filterPeaksByRawChromatogram(peakList, detectedPeaks);

            if (detectedPeaks.isEmpty()) {
                focusedMass += massStep;
                continue;
            }

            // Filtering noise peaks considering baseline effects
            detectedPeaks = getBackgroundSubtractPeaks(detectedPeaks, peakList, properties.backgroundSubtraction);

            if (detectedPeaks.isEmpty()) {
                focusedMass += massStep;
                continue;
            }

            // Removing peak spot redundancies among slices
            if (properties.accuracyType == AccuracyType.ACCURATE) {
                detectedPeaks = removePeakAreaBeanRedundancy(detectedPeaksList, detectedPeaks, massStep, 0.025);

                if (detectedPeaks.isEmpty()) {
                    focusedMass += massStep;
                    continue;
                }
            }

            detectedPeaksList.add(detectedPeaks);
            focusedMass += massStep;
        }

        logger.debug("pre-Final detected peak count: " + detectedPeaksList.size());

        detectedPeaks = getCombinedPeakAreaBeanList(detectedPeaksList);
        logger.debug("pos-combined detected peak count: " + detectedPeaks.size());

        detectedPeaks = getPeakAreaBeanProperties(detectedPeaks, spectrumList, properties);

        logger.debug("Final detected peak count: " + detectedPeaks.size());

        return detectedPeaks;
    }

    /**
     * @param peakList
     * @param properties
     * @return
     */
    private List<PeakAreaBean> getPeakAreaBeanList(List<double[]> peakList,  MSDialProcessingProperties properties) {

        List<double[]> smoothedPeakList = DataAccessUtility.getSmoothedPeakArray(peakList, properties.smoothingMethod, properties.smoothingLevel);

        List<PeakDetectionResult> detectedPeaks = GCMSDifferentialBasedPeakDetection.detectPeaks(smoothedPeakList,
            properties.minimumDataPoints, properties.minimumAmplitude, properties.amplitudeNoiseFactor,
            properties.slopeNoiseFactor, properties.averagePeakWidth, properties.peaktopNoiseFactor);

        if (detectedPeaks.isEmpty()) {
            return new ArrayList<>();
        }

        List<PeakAreaBean> peakAreaBeanList = new ArrayList<>();

        for (PeakDetectionResult detectedPeak : detectedPeaks) {
            if (detectedPeak.intensityAtPeakTop <= 0)
                continue;

            PeakAreaBean peakAreaBean = DataAccessUtility.getPeakAreaBean(detectedPeak);
            peakAreaBean.accurateMass = peakList.get(detectedPeak.scanNumAtPeakTop)[2];
            peakAreaBean.ms1LevelDataPointNumber = (int) peakList.get(detectedPeak.scanNumAtPeakTop)[0];
            peakAreaBeanList.add(peakAreaBean);
        }

        return peakAreaBeanList;
    }

    /**
     *
     * @param peakAreaBean
     * @param spectrumList
     * @param properties
     */
    public void setIsotopicIonInformation(PeakAreaBean peakAreaBean, List<Feature> spectrumList, MSDialProcessingProperties properties) {

        int specID = peakAreaBean.ms1LevelDataPointNumber;
        double massTolerance = properties.accuracyType == AccuracyType.NOMINAL ? 0.5 : properties.massAccuracy;
        double precursorMz = peakAreaBean.accurateMass;

        List<Ion> spectrum = SpectralCentroiding.getGCMSCentroidedSpectrum(spectrumList, properties.dataType, specID, massTolerance,
                properties.amplitudeCutoff, properties.massRangeBegin, properties.massRangeEnd);

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
