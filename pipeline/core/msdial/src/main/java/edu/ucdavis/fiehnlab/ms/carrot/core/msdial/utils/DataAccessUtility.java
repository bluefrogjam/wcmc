package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.IonMode;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.Peak;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakAreaBean;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.lcms.PeakDetectionResult;

import java.util.ArrayList;
import java.util.List;

public class DataAccessUtility {

    /**
     * Get a simple EIC from a given list of spectra
     *
     * @param spectrumList
     * @param focusedMass
     * @param massSliceWidth
     * @param rtBegin
     * @param rtEnd
     * @param ionMode
     * @return
     */
    public static List<double[]> getMS1PeakList(List<Feature> spectrumList, double focusedMass, double massSliceWidth, double rtBegin, double rtEnd, IonMode ionMode) {

        List<double[]> peakList = new ArrayList<>();

        for (int i = 0; i < spectrumList.size(); i++) {
            Feature spectrum = spectrumList.get(i);

            // Filter by msLevel, ion mode and retention time
            if (spectrum.associatedScan().get().msLevel() > 1 || !spectrum.ionMode().get().mode().equals(ionMode.mode()) || spectrum.retentionTimeInMinutes() < rtBegin) {
                continue;
            }
            if (spectrum.retentionTimeInMinutes() > rtEnd) {
                break;
            }

            double sum = 0;
            double maxIntensityMz = Double.MIN_VALUE;
            double maxMass = focusedMass;

            List<Ion> massSpectrum = TypeConverter.getJavaIonList(spectrum);
            int startIndex = getMs1StartIndex(focusedMass, massSliceWidth, massSpectrum);

            for (int j = startIndex; j < massSpectrum.size(); j++) {
                if (massSpectrum.get(j).mass() < focusedMass - massSliceWidth) {
                    continue;
                } else if (focusedMass - massSliceWidth <= massSpectrum.get(j).mass() && massSpectrum.get(j).mass() <= focusedMass + massSliceWidth) {
                    sum += massSpectrum.get(j).intensity();

                    if (maxIntensityMz < massSpectrum.get(j).intensity()) {
                        maxIntensityMz = massSpectrum.get(j).intensity();
                        maxMass = massSpectrum.get(j).mass();
                    }
                } else if (massSpectrum.get(j).mass() > focusedMass + massSliceWidth) {
                    break;
                }
            }

            peakList.add(new double[]{i, spectrum.retentionTimeInMinutes(), maxMass, sum});
        }

        return peakList;
    }

    /**
     * Get the scan range (min/max m/z) for a given list of spectra
     *
     * @param spectrumList
     * @param ionMode
     * @return
     */
    public static double[] getMS1ScanRange(List<Feature> spectrumList, IonMode ionMode) {
        double minMz = Double.MAX_VALUE;
        double maxMz = Double.MIN_VALUE;

        System.out.println("(getMS1ScanRange) Spectrum count: " + spectrumList.size());
        if (spectrumList.size() <= 0) {
            return new double[]{0, 0};
        }

        for (Feature spectrum : spectrumList) {
            // Filter by msLevel and ion mode
            if (spectrum.associatedScan().get().msLevel() != 1 || !spectrum.ionMode().get().mode().equals(ionMode.mode())) {
                continue;
            }

            for (Ion ion : TypeConverter.getJavaIonList(spectrum)) {
                if (ion.mass() < minMz) {
                    minMz = ion.mass();
                } else if (ion.mass() > maxMz) {
                    maxMz = ion.mass();
                }
            }
        }

        return new double[] {minMz, maxMz};
    }


    /**
     * @param targetMass
     * @param tolerance
     * @param spectrum
     * @return
     */
    public static int getMs1StartIndex(double targetMass, double tolerance, List<Ion> spectrum) {
        return getStartIndexForTargetMass(targetMass, spectrum, tolerance);
    }

    /**
     * @param targetMass
     * @param spectrum
     * @return
     */
    public static int getMs2StartIndex(double targetMass, List<Ion> spectrum) {
        return getStartIndexForTargetMass(targetMass, spectrum, 0);
    }

    /**
     * Finds the start of the chromatographic peak for the selected targetMass
     * within the optional MS1 tolerance window (in no more than 10 steps)
     * NOTE: for more accurate results the counter can be raised up to 15
     *
     * @param focusedMass
     * @param spectrum
     * @param tolerance   (optional)
     * @return
     */
    private static int getStartIndexForTargetMass(double focusedMass, List<Ion> spectrum, double tolerance) {
        if (spectrum.isEmpty()) {
            return 0;
        }

        double targetMass = focusedMass - tolerance;
        int startIndex = 0, endIndex = spectrum.size() - 1;
        int counter = 0;

        if (targetMass > spectrum.get(endIndex).mass()) {
            return endIndex;
        }

        while (counter < 10) {
            if (spectrum.get(startIndex).mass() <= targetMass && targetMass < spectrum.get((startIndex + endIndex) / 2).mass()) {
                endIndex = (startIndex + endIndex) / 2;
            } else if (spectrum.get((startIndex + endIndex) / 2).mass() <= targetMass && targetMass < spectrum.get(endIndex).mass()) {
                startIndex = (startIndex + endIndex) / 2;
            }

            counter++;
        }

        return startIndex;
    }


    /**
     * Smooth a peak list with the specified smoothing method and level
     *
     * @param peaklist
     * @param smoothingMethod
     * @param smoothingLevel
     * @return
     */
    public static List<double[]> getSmoothedPeakArray(List<double[]> peaklist, String smoothingMethod, int smoothingLevel) {
        return Smoothing.smooth(peaklist, SmoothingMethod.valueOf(smoothingMethod.toUpperCase()), smoothingLevel);
    }

    /**
     *
     * @param peaklist
     * @param smoothingMethod
     * @param smoothingLevel
     * @return
     */
    public static List<Peak> getSmoothedPeaks(List<Peak> peaklist, String smoothingMethod, int smoothingLevel) {
        return Smoothing.smoothPeaks(peaklist, SmoothingMethod.valueOf(smoothingMethod.toUpperCase()), smoothingLevel);
    }



    public static PeakAreaBean getPeakAreaBean(PeakDetectionResult peakResult) {
        if (peakResult == null)
            return null;

        PeakAreaBean peak = new PeakAreaBean();
        peak.amplitudeOrderValue = peakResult.amplitudeOrderValue;
        peak.amplitudeScoreValue = peakResult.amplitudeScoreValue;
        peak.areaAboveBaseline = peakResult.areaAboveBaseline;
        peak.areaAboveZero = peakResult.areaAboveZero;
        peak.basePeakValue = peakResult.basePeakValue;
        peak.chargeNumber = 1;
        peak.gaussianSimilarityValue = peakResult.gaussianSimilarityValue;
        peak.idealSlopeValue = peakResult.idealSlopeValue;
        peak.intensityAtLeftPeakEdge = peakResult.intensityAtLeftPeakEdge;
        peak.intensityAtPeakTop = peakResult.intensityAtPeakTop;
        peak.intensityAtRightPeakEdge = peakResult.intensityAtRightPeakEdge;
        peak.peakID = peakResult.peakID;
        peak.peakPureValue = peakResult.peakPureValue;
        peak.rtAtLeftPeakEdge = peakResult.rtAtLeftPeakEdge;
        peak.rtAtPeakTop = peakResult.rtAtPeakTop;
        peak.rtAtRightPeakEdge = peakResult.rtAtRightPeakEdge;
        peak.scanNumberAtLeftPeakEdge = peakResult.scanNumAtLeftPeakEdge;
        peak.scanNumberAtPeakTop = peakResult.scanNumAtPeakTop;
        peak.scanNumberAtRightPeakEdge = peakResult.scanNumAtRightPeakEdge;
        peak.shapenessValue = peakResult.sharpnessValue;
        peak.symmetryValue = peakResult.symmetryValue;
        peak.normalizedValue = -1;
        peak.accurateMass = -1;
        peak.ms1LevelDataPointNumber = -1;
        peak.ms2LevelDataPointNumber = -1;
//		pab.alignedRetentionTime = -1;
//		pab.totalScore = -1;
//		pab.metaboliteName = "";
//		pab.adductIonName = "";
//		pab.libraryID = -1;
        peak.isotopeWeightNumber = -1;
        peak.isotopeParentPeakID = -1;
//		pab.adductParent = -1;
//		pab.rtSimilarityValue = -1;
//		pab.isotopeSimilarityValue = -1;
//		pab.massSpectraSimilarityValue = -1;
//		pab.reverseSearchSimilarityValue = -1;
//		pab.presenseSimilarityValue = -1;
//		pab.adductIonAccurateMass = -1;
//		pab.adductIonXmer = -1;
//		pab.adductIonChargeNumber = -1;

        return peak;
    }
}
