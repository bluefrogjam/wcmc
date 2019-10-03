package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.deconvolution.lcms;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.MSDialProcessingProperties;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.isotope.IsotopeEstimator;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.Peak;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakAreaBean;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.lcms.MS2DeconvolutionResult;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.DataAccessUtility;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.LCMSDataAccessUtility;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.SpectralCentroiding;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.TypeConverter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SpectralDeconvolution {

    public List<MS2DeconvolutionResult> getMS2Deconvolution(List<? extends Feature> spectrumList, List<PeakAreaBean> detectedPeaks, MSDialProcessingProperties properties) {

        List<MS2DeconvolutionResult> deconvolutionResults = new ArrayList<>();

        for (PeakAreaBean detectedPeak : detectedPeaks) {

            MS2DeconvolutionResult ms2DeconvolutionResult = dataDependentMS2Deconvolution(spectrumList, detectedPeak, properties);

            // Replaces deisotopingForMSMSSpectra call
            new IsotopeEstimator().msmsIsotopeRecognition(ms2DeconvolutionResult.ms2Spectrum, detectedPeak.chargeNumber, properties);

            deconvolutionResults.add(ms2DeconvolutionResult);
        }

        return deconvolutionResults;
    }

    /**
     *
     * @param spectrumList
     * @param detectedPeak
     * @param properties
     */
    private MS2DeconvolutionResult dataDependentMS2Deconvolution(List<? extends Feature> spectrumList, PeakAreaBean detectedPeak, MSDialProcessingProperties properties) {

        // create deconvolution result and store raw MS1 spectrum
        MS2DeconvolutionResult ms2DeconvolutionResult = new MS2DeconvolutionResult(detectedPeak);
        ms2DeconvolutionResult.rawMS1Spectrum = TypeConverter.getJavaIonList(spectrumList.get(detectedPeak.ms1LevelDataPointNumber));

        List<Ion> ms1Spectrum = new ArrayList<>(SpectralCentroiding.getLCMSCentroidedSpectrum(spectrumList, properties.dataType,
            detectedPeak.ms1LevelDataPointNumber, properties.centroidMS1Tolerance, properties.peakDetectionBasedCentroid));

        if (detectedPeak.ms2LevelDataPointNumber == -1) {
            // No MS2 data
            ms2DeconvolutionResult.peakListList.add(new ArrayList<>());
        } else {
            // MS2 data available
            ms2DeconvolutionResult.rawMS2Spectrum = TypeConverter.getJavaIonList(spectrumList.get(detectedPeak.ms2LevelDataPointNumber));

            double startRt = detectedPeak.rtAtPeakTop - (detectedPeak.rtAtRightPeakEdge - detectedPeak.rtAtLeftPeakEdge);
            double endRt = detectedPeak.rtAtPeakTop + (detectedPeak.rtAtRightPeakEdge - detectedPeak.rtAtLeftPeakEdge);
            double precursorMz = detectedPeak.accurateMass;

            List<Ion> centroidedSpectrumList = new ArrayList<>(SpectralCentroiding.getLCMSCentroidedSpectrum(spectrumList, properties.dataType, detectedPeak.ms2LevelDataPointNumber, properties.centroidMS2Tolerance, properties.peakDetectionBasedCentroid));
            List<Ion> centroidedSpectrum = new ArrayList<>(centroidedSpectrumList);
            centroidedSpectrum.sort(Comparator.comparing(Ion::mass));

            if (!centroidedSpectrum.isEmpty()) {
                for (Ion ion : centroidedSpectrum) {
                    if (properties.removeAfterPrecursor && detectedPeak.accurateMass + properties.keptIsotopeRange < ion.mass())
                        continue;
                    if (properties.amplitudeCutoff > ion.intensity())
                        continue;

                    List<double[]> ms2PeakList = LCMSDataAccessUtility.getMS2Peaklist(spectrumList, precursorMz, ion.mass(),
                        startRt, endRt, properties.ionMode, properties.centroidMS1Tolerance, properties.centroidMS2Tolerance);

                    ms2PeakList.add(0, new double[]{0, startRt, ion.mass(), 0});
                    ms2PeakList.add(new double[]{0, endRt, ion.mass(), 0});

                    ms2PeakList = DataAccessUtility.getSmoothedPeakArray(ms2PeakList, properties.smoothingMethod, properties.smoothingLevel);

                    List<Ion> smoothedMS2PeakList = new ArrayList<>();

                    for (double[] aPeak : ms2PeakList) {
                        smoothedMS2PeakList.add(new Ion(aPeak[2], (float)aPeak[3]));
                    }

                    ms2DeconvolutionResult.peakListList.add(smoothedMS2PeakList);
                    ms2DeconvolutionResult.ms2Spectrum.add(new Peak(ion.mass(), ion.intensity()));
                }

                if (ms2DeconvolutionResult.ms2Spectrum.isEmpty()) {
                    ms2DeconvolutionResult = new MS2DeconvolutionResult(detectedPeak);
                }
            } else {
                ms2DeconvolutionResult = new MS2DeconvolutionResult(detectedPeak);
            }
        }

        ms2DeconvolutionResult.ms1Spectrum = ms1Spectrum;

        return ms2DeconvolutionResult;
    }
}
