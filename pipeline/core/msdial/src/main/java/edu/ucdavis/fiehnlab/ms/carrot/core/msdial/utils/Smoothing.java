package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils;

import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.Peak;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Smoothing {

    /**
     * Hiroshi:
     * Now I'm preparing six smoothing methods but do not use LowessFilter and LowessFilter since I do not test them yet.
     * These methods will return the list of array, i.e. chromatogram information. Each array includes peak information as [0]scan number [1]retention time [2]m/z [3]intensity.
     * The first argument of all smoothing methods should be raw chromatogram (list of array as described above.).
     * The second argument of all smoothing methods is the number of data points which are used for the smoothing.
     *
     * @param peakList
     * @param method
     * @param smoothingLevel
     * @return
     */
    public static List<double[]> smooth(List<double[]> peakList, SmoothingMethod method, int smoothingLevel) {
        switch (method) {
            case SIMPLE_MOVING_AVERAGE:
            case SAVITZKY_GOLAY_FILTER:
            case LOESS_FILTER:
            case LOWESS_FILTER:
            case BINOMIAL_FILTER:
            case LINEAR_WEIGHTED_MOVING_AVERAGE:
            default:
                return linearWeightedMovingAverage(peakList, smoothingLevel);
        }
    }

    /**
     *
     * @param peakList
     * @param method
     * @param smoothingLevel
     * @return
     */
    public static List<Peak> smoothPeaks(List<Peak> peakList, SmoothingMethod method, int smoothingLevel) {
        List<double[]> doublePeakList = peakList.stream()
                .map(p -> new double[] {p.scanNumber, p.retentionTime, p.mz, p.intensity})
                .collect(Collectors.toList());

        return smooth(doublePeakList, method, smoothingLevel).stream()
                .map(p -> new Peak((int)p[0], p[1], p[2], p[3]))
                .collect(Collectors.toList());
    }

    private static List<double[]> linearWeightedMovingAverage(List<double[]> peakList, int smoothingLevel) {

        List<double[]> smoothedPeakList = new ArrayList<>();

        int lwmaNormalizationValue = smoothingLevel + 1;

        for (int i = 1; i <= smoothingLevel; i++) {
            lwmaNormalizationValue += i * 2;
        }


        for (int i = 0; i < peakList.size(); i++) {
            double sum = 0.0;

            for (int j = -smoothingLevel; j <= smoothingLevel; j++) {
                if (i + j < 0 || i + j > peakList.size() - 1) {
                    sum += peakList.get(i)[3] * (smoothingLevel - Math.abs(j) + 1);
                } else {
                    sum += peakList.get(i + j)[3] * (smoothingLevel - Math.abs(j) + 1);
                }
            }

            double smoothedPeakIntensity = sum / lwmaNormalizationValue;
            smoothedPeakList.add(new double[]{i, peakList.get(i)[1], peakList.get(i)[2], smoothedPeakIntensity});
        }

        return smoothedPeakList;
    }
}
