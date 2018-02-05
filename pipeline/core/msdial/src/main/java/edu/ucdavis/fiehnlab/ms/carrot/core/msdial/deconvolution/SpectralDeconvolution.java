package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.deconvolution;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.MSDialProcessingProperties;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.isotope.IsotopeEstimator;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MS2DecResult;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.Peak;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakAreaBean;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.LCMSDataAccessUtility;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.SmoothingMethod;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.SpectralCentroiding;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SpectralDeconvolution {

    public List<MS2DecResult> getMS2Deconvolution(List<Feature> spectrumList, List<PeakAreaBean> detectedPeaks, MSDialProcessingProperties properties) {

        List<MS2DecResult> deconvolutionResults = new ArrayList<>();

        for (PeakAreaBean detectedPeak : detectedPeaks) {

            MS2DecResult ms2DecResult = dataDependentMS2Deconvolution(spectrumList, detectedPeak, properties);

            // Replaces deisotopingForMSMSSpectra call
            new IsotopeEstimator().msmsIsotopeRecognition(ms2DecResult.spectrum, properties.maxTraceNumber, detectedPeak.chargeNumber, properties.centroidMS2Tolerance);

            deconvolutionResults.add(ms2DecResult);
        }

        return deconvolutionResults;
    }

    /**
     *
     * @param spectrumList
     * @param detectedPeak
     * @param properties
     */
    private MS2DecResult dataDependentMS2Deconvolution(List<Feature> spectrumList, PeakAreaBean detectedPeak, MSDialProcessingProperties properties) {

        MS2DecResult ms2DecResult = new MS2DecResult(detectedPeak);

        if (detectedPeak.ms2LevelDataPointNumber == -1) {
            // No MS2 data
            ms2DecResult.peakListList.add(new ArrayList<>());
        } else {
            // MS2 data available
            double startRt = detectedPeak.rtAtPeakTop - (detectedPeak.rtAtRightPeakEdge - detectedPeak.rtAtLeftPeakEdge);
            double endRt = detectedPeak.rtAtPeakTop + (detectedPeak.rtAtRightPeakEdge - detectedPeak.rtAtLeftPeakEdge);
            double precursorMz = detectedPeak.accurateMass;

            List<Ion> centroidedSpectrum = SpectralCentroiding.getCentroidSpectrum(spectrumList, properties.dataType, detectedPeak.ms2LevelDataPointNumber, properties.centroidMS2Tolerance, properties.peakDetectionBasedCentroid);
            centroidedSpectrum.sort(Comparator.comparing(Ion::mass));

            if (!centroidedSpectrum.isEmpty()) {
                for (Ion ion: centroidedSpectrum) {
                    if (properties.removeAfterPrecursor && detectedPeak.accurateMass + properties.keptIsotopeRange < ion.mass())
                        continue;
                    if (properties.amplitudeCutoff > ion.intensity())
                        continue;

                    List<double[]> ms2PeakList = LCMSDataAccessUtility.getMS2Peaklist(spectrumList, precursorMz, ion.mass(),
                            startRt, endRt, properties.ionMode, properties.centroidMS1Tolerance, properties.centroidMS2Tolerance);

                    ms2PeakList.add(0, new double[] { 0, startRt, ion.mass(), 0 });
                    ms2PeakList.add(new double[] { 0, endRt, ion.mass(), 0 });

                    ms2PeakList = LCMSDataAccessUtility.getSmoothedPeakArray(ms2PeakList,
                            SmoothingMethod.valueOf(properties.smoothingMethod.toUpperCase()), properties.smoothingLevel);

                    List<Ion> smoothedMS2PeakList = new ArrayList<>();

                    for (int i = 0; i < ms2PeakList.size(); i++) {
                        smoothedMS2PeakList.add(new Ion(ms2PeakList.get(i)[2], ms2PeakList.get(i)[3]));
                    }

                    ms2DecResult.peakListList.add(smoothedMS2PeakList);
                    ms2DecResult.spectrum.add(new Peak(ion.mass(), ion.intensity()));
                }

                if (ms2DecResult.spectrum.isEmpty()) {
                    ms2DecResult = new MS2DecResult(detectedPeak);
                }
            } else {
                ms2DecResult = new MS2DecResult(detectedPeak);
            }
        }

        return ms2DecResult;
    }
}
