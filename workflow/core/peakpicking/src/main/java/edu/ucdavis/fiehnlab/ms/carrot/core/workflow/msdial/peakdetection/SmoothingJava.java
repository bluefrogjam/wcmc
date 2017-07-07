package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.deconvolution.MsDialPeak;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.SmoothingMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by diego on 8/31/2016.
 */
public class SmoothingJava {
	public static List<double[]> smooth(List<double[]> peakList, SmoothingMethod method, int level, boolean forDeconv) {
		switch (method) {
			case SIMPLE_MOVING_AVERAGE:
			case SAVITZKY_GOLAY_FILTER:
			case LOESS_FILTER:
			case LOWESS_FILTER:
			case BINOMIAL_FILTER:
				return new ArrayList<>();
			case LINEAR_WEIGHTED_MOVING_AVERAGE:
			default:
				return linearWeightedMovingAverage(peakList, level, forDeconv);
		}
	}

	public static List<MsDialPeak> smoothPeaks(List<MsDialPeak> peakList, SmoothingMethod method, int level, boolean forDeconv) {
		switch (method) {
			case SIMPLE_MOVING_AVERAGE:
			case SAVITZKY_GOLAY_FILTER:
			case LOESS_FILTER:
			case LOWESS_FILTER:
			case BINOMIAL_FILTER:
				return new ArrayList<>();
			case LINEAR_WEIGHTED_MOVING_AVERAGE:
			default:
				return linearWeightedMovingAverage(peakList.stream().map(p -> new double[]{p.scanNum, p.rtMin, p.mass, p.intensity}).collect(Collectors.toList()), level, forDeconv).stream().map(p -> new MsDialPeak((int) p[0], p[1], p[2], p[3])).collect(Collectors.toList());
		}
	}

	private static List<double[]> linearWeightedMovingAverage(List<double[]> peaklist, int level, boolean forDeconv) {

		double sum;
		List<double[]> smoothedPeaklist = new ArrayList<>();

		int lwmaNormalizationValue = level + 1;

		for (int i = 1; i <= level; i++) {
			lwmaNormalizationValue += i * 2;
		}

		for (int i = 0; i < peaklist.size(); i++) {
			double smoothedPeakIntensity = 0.0;
			sum = 0;

			for (int j = -level; j <= level; j++) {
				if (i + j < 0 || i + j > peaklist.size() - 1) {
					sum += peaklist.get(i)[3] * (level - Math.abs(j) + 1);
				} else {
					sum += peaklist.get(i + j)[3] * (level - Math.abs(j) + 1);
				}
			}

			smoothedPeakIntensity = sum / lwmaNormalizationValue;

			if (forDeconv) {
				smoothedPeaklist.add(new double[]{peaklist.get(i)[0], peaklist.get(i)[1], peaklist.get(i)[2], smoothedPeakIntensity});
			} else {
				smoothedPeaklist.add(new double[]{i, peaklist.get(i)[1], peaklist.get(i)[2], smoothedPeakIntensity});
			}

		}

		return smoothedPeaklist;
	}
}
