package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.math.BasicMathematics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by diego on 7/21/16.
 */
public class DifferentialBasedPeakDetection {
	private static Logger logger = LoggerFactory.getLogger(DifferentialBasedPeakDetection.class);
	private static final double[] firstDiffCoeff = new double[]{-0.2, -0.1, 0, 0.1, 0.2};
	private static final double[] secondDiffCoeff = new double[]{0.14285714, -0.07142857, -0.1428571, -0.07142857, 0.14285714};

	public static List<DetectedPeak> detectLCPeaks(List<Peak> peaklist,
	                                               double minimumDatapointCriteria, double minimumAmplitudeCriteria,
	                                               double amplitudeNoiseFactor, double slopeNoiseFactor, double peaktopNoiseFactor) {
		List<DetectedPeak> peakDetectionResults = new ArrayList<>();

		//Differential calculation
		List<Double> firstDiffPeaklist = new ArrayList<>();
		List<Double> secondDiffPeaklist = new ArrayList<>();
		double firstDiff;
		double secondDiff;
		double maxFirstDiff = Double.MIN_VALUE;
		double maxSecondDiff = Double.MIN_VALUE;
		double maxAmplitudeDiff = Double.MIN_VALUE;
		int halfDatapoint = (firstDiffCoeff.length / 2);
		int peakID = 0;

		for (int i = 0; i < peaklist.size(); i++) {
			if (i < halfDatapoint) {
				firstDiffPeaklist.add(0D);
				secondDiffPeaklist.add(0D);
				continue;
			}
			if (i >= peaklist.size() - halfDatapoint) {
				firstDiffPeaklist.add(0D);
				secondDiffPeaklist.add(0D);
				continue;
			}

			firstDiff = secondDiff = 0;
			for (int j = 0; j < firstDiffCoeff.length; j++) {
				firstDiff += firstDiffCoeff[j] * peaklist.get(i + j - halfDatapoint).sumIntensity();
				secondDiff += secondDiffCoeff[j] * peaklist.get(i + j - halfDatapoint).sumIntensity();
			}
			firstDiffPeaklist.add(firstDiff);
			secondDiffPeaklist.add(secondDiff);

			if (Math.abs(firstDiff) > maxFirstDiff) {
				maxFirstDiff = Math.abs(firstDiff);
			}
			if (secondDiff < 0 && maxSecondDiff < -1 * secondDiff) {
				maxSecondDiff = -1 * secondDiff;
			}
			if (Math.abs(peaklist.get(i).sumIntensity() - peaklist.get(i - 1).sumIntensity()) > maxAmplitudeDiff) {
				maxAmplitudeDiff = Math.abs(peaklist.get(i).sumIntensity() - peaklist.get(i - 1).sumIntensity());
			}
		}

		//Noise estimate
		List<Double> amplitudeNoiseCandidate = new ArrayList<>();
		List<Double> slopeNoiseCandidate = new ArrayList<>();
		List<Double> peaktopNoiseCandidate = new ArrayList<>();
		double amplitudeNoise;
		double slopeNoise;
		double peaktopNoise;
		double amplitudeNoiseThresh = maxAmplitudeDiff * 0.05;
		double slopeNoiseThresh = maxFirstDiff * 0.05;
		double peaktopNoiseThresh = maxSecondDiff * 0.05;

		for (int i = 2; i < peaklist.size() - 2; i++) {
			if (Math.abs(peaklist.get(i + 1).sumIntensity() - peaklist.get(i).sumIntensity()) < amplitudeNoiseThresh && Math.abs(peaklist.get(i + 1).sumIntensity() - peaklist.get(i).sumIntensity()) > 0) {
				amplitudeNoiseCandidate.add(Math.abs(peaklist.get(i + 1).sumIntensity() - peaklist.get(i).sumIntensity()));
			}
			if (Math.abs(firstDiffPeaklist.get(i)) < slopeNoiseThresh && Math.abs(firstDiffPeaklist.get(i)) > 0) {
				slopeNoiseCandidate.add(Math.abs(firstDiffPeaklist.get(i)));
			}
			if (secondDiffPeaklist.get(i) < 0 && Math.abs(secondDiffPeaklist.get(i)) < peaktopNoiseThresh && Math.abs(secondDiffPeaklist.get(i)) > 0) {
				peaktopNoiseCandidate.add(Math.abs(secondDiffPeaklist.get(i)));
			}
		}
		if (amplitudeNoiseCandidate.size() == 0) {
			amplitudeNoise = 0.0001;
		} else {
			amplitudeNoise = BasicMathematics.BrokenMedian(amplitudeNoiseCandidate);
//			amplitudeNoise = BasicMathematics.Median(amplitudeNoiseCandidate);
		}
		if (slopeNoiseCandidate.size() == 0) {
			slopeNoise = 0.0001;
		} else {
			slopeNoise = BasicMathematics.BrokenMedian(slopeNoiseCandidate);
//			slopeNoise = BasicMathematics.Median(slopeNoiseCandidate);
		}
		if (peaktopNoiseCandidate.size() == 0) {
			peaktopNoise = 0.0001;
		} else {
			peaktopNoise = BasicMathematics.BrokenMedian(peaktopNoiseCandidate);
//			peaktopNoise = BasicMathematics.Median(peaktopNoiseCandidate);
		}

		//Search peaks
		List<PeakCandidate> datapoints;
		double peakTopIntensity;
		double peakHwhm;
		double peakHalfDiff;
		double peakFivePercentDiff;
		double leftSharpenessValue;
		double rightSharpenessValue;
		double gaussianSigma;
		double gaussianNormalize;
		double gaussianArea;
		double gaussinaSimilarityValue;
		double gaussianSimilarityLeftValue;
		double gaussianSimilarityRightValue;
		double realAreaAboveZero;
		double realAreaAboveBaseline;
		double leftPeakArea;
		double rightPeakArea;
		double idealSlopeValue;
		double nonIdealSlopeValue;
		double symmetryValue;
		double basePeakValue;
		double peakPureValue;
		int peaktopCheckPoint;
		int peakTopId = -1;
		int peakHalfId = -1;
		int leftPeakFivePercentId = -1;
		int rightPeakFivePercentId = -1;
		int leftPeakHalfId = -1;
		int rightPeakHalfId = -1;

		boolean peaktopCheck = false;
		boolean infiniteLoopCheck = false;
		double infiniteLoopID = 0.0;

		for (int i = 0; i < peaklist.size(); i++) {
			if (i >= peaklist.size() - 1 - minimumDatapointCriteria) break;
			//1. Left edge criteria
			if (firstDiffPeaklist.get(i) > slopeNoise * slopeNoiseFactor && firstDiffPeaklist.get(i + 1) > slopeNoise * slopeNoiseFactor) {
				datapoints = new ArrayList<>();
				datapoints.add(new PeakCandidate(peaklist.get(i),
						firstDiffPeaklist.get(i),
						secondDiffPeaklist.get(i)));

				//search real left edge within 5 data points
//				new LeftEdgeFinder(peaklist, datapoints, i).invoke();
				for (int j = 0; j <= 5; j++) {
					if (i - j - 1 < 0) break;
					if (peaklist.get(i - j).sumIntensity() <= peaklist.get(i - j - 1).sumIntensity())
						break;
					if (peaklist.get(i - j).sumIntensity() > peaklist.get(i - j - 1).sumIntensity())
						datapoints.add(0, new PeakCandidate(peaklist.get(i - j - 1),
								firstDiffPeaklist.get(i - j - 1),
								secondDiffPeaklist.get(i - j - 1)));
				}

				//2. Right edge criteria
				peaktopCheck = false;
				peaktopCheckPoint = i;
				while (true) {
					if (i + 1 == peaklist.size() - 1) break;

					i++;
					datapoints.add(new PeakCandidate(peaklist.get(i), firstDiffPeaklist.get(i), secondDiffPeaklist.get(i)));
					if (!peaktopCheck && firstDiffPeaklist.get(i - 1) > 0 &&
							firstDiffPeaklist.get(i) < 0 &&
							secondDiffPeaklist.get(i) < -1 * peaktopNoise * peaktopNoiseFactor) {
						peaktopCheck = true;
						peaktopCheckPoint = i;
					}
					if (peaktopCheck && peaktopCheckPoint + 3 <= i - 1 &&
							firstDiffPeaklist.get(i - 1) > -1 * slopeNoise * slopeNoiseFactor &&
							firstDiffPeaklist.get(i) > -1 * slopeNoise * slopeNoiseFactor)
						break;
				}

				//Search real right edge within 5 data points
				boolean rightCheck = false;
				int trackcounter = 0;
				//case: wrong edge is in right of real edge
				if (!rightCheck) {
					for (int j = 0; j <= 5; j++) {
						if (i - j - 1 < 0) break;
						if (peaklist.get(i - j).sumIntensity() <= peaklist.get(i - j - 1).sumIntensity()) break;
						if (peaklist.get(i - j).sumIntensity() > peaklist.get(i - j - 1).sumIntensity()) {
							datapoints.remove(datapoints.size() - 1);
							rightCheck = true;
							trackcounter++;
						}
					}
					if (trackcounter > 0) {
						i -= trackcounter;
						if (infiniteLoopCheck && i == infiniteLoopID && i > peaklist.size() - 10) break;
						infiniteLoopCheck = true;
						infiniteLoopID = i;
					}
				}

				//case: wrong edge is in left of real edge
				if (!rightCheck) {
					for (int j = 0; j <= 5; j++) {
						if (i + j + 1 > peaklist.size() - 1) break;
						if (peaklist.get(i + j).sumIntensity() <= peaklist.get(i + j + 1).sumIntensity()) break;
						if (peaklist.get(i + j).sumIntensity() > peaklist.get(i + j + 1).sumIntensity()) {
							datapoints.add(new PeakCandidate(peaklist.get(i + j + 1),
									firstDiffPeaklist.get(i + j + 1),
									secondDiffPeaklist.get(i + j + 1)));
							rightCheck = true;
							trackcounter++;
						}
					}
					if (trackcounter > 0) i += trackcounter;
				}


				//3. Check minimum datapoint criteria
				if (datapoints.size() < minimumDatapointCriteria) continue;

				//4. Check peak criteria
				peakTopIntensity = Double.MIN_VALUE;
				peakTopId = -1;
				for (int j = 0; j < datapoints.size(); j++) {
					if (peakTopIntensity < datapoints.get(j).sumIntensity()) {
						peakTopIntensity = datapoints.get(j).sumIntensity();
						peakTopId = j;
					}
				}
				if (datapoints.get(peakTopId).sumIntensity() - datapoints.get(0).sumIntensity() < minimumAmplitudeCriteria ||
						datapoints.get(peakTopId).sumIntensity() - datapoints.get(datapoints.size() - 1).sumIntensity() < minimumAmplitudeCriteria ||
						datapoints.get(peakTopId).sumIntensity() - datapoints.get(0).sumIntensity() < amplitudeNoise * amplitudeNoiseFactor ||
						datapoints.get(peakTopId).sumIntensity() - datapoints.get(datapoints.size() - 1).sumIntensity() < amplitudeNoise * amplitudeNoiseFactor)
					continue;

				//5. Check HWHM criteria and calculate shapeness value, symmetry value, base peak value, ideal value, non ideal value
				idealSlopeValue = 0;
				nonIdealSlopeValue = 0;
				peakHalfDiff = Double.MAX_VALUE;
				peakFivePercentDiff = Double.MAX_VALUE;
				leftSharpenessValue = Double.MIN_VALUE;
				for (int j = peakTopId; j >= 0; j--) {
					if (peakHalfDiff > Math.abs((datapoints.get(peakTopId).sumIntensity() - datapoints.get(0).sumIntensity()) / 2 - (datapoints.get(j).sumIntensity() - datapoints.get(0).sumIntensity()))) {
						peakHalfDiff = Math.abs((datapoints.get(peakTopId).sumIntensity() - datapoints.get(0).sumIntensity()) / 2 - (datapoints.get(j).sumIntensity() - datapoints.get(0).sumIntensity()));
						leftPeakHalfId = j;
					}

					if (peakFivePercentDiff > Math.abs((datapoints.get(peakTopId).sumIntensity() - datapoints.get(0).sumIntensity()) / 5 - (datapoints.get(j).sumIntensity() - datapoints.get(0).sumIntensity()))) {
						peakFivePercentDiff = Math.abs((datapoints.get(peakTopId).sumIntensity() - datapoints.get(0).sumIntensity()) / 5 - (datapoints.get(j).sumIntensity() - datapoints.get(0).sumIntensity()));
						leftPeakFivePercentId = j;
					}

					if (j == peakTopId) continue;

					if (leftSharpenessValue < (datapoints.get(peakTopId).sumIntensity() - datapoints.get(j).sumIntensity()) / (peakTopId - j) / Math.sqrt(datapoints.get(peakTopId).sumIntensity()))
						leftSharpenessValue = (datapoints.get(peakTopId).sumIntensity() - datapoints.get(j).sumIntensity()) / (peakTopId - j) / Math.sqrt(datapoints.get(peakTopId).sumIntensity());

					if (datapoints.get(j).firstDiff() > 0)
						idealSlopeValue += Math.abs(datapoints.get(j).firstDiff());
					else
						nonIdealSlopeValue += Math.abs(datapoints.get(j).firstDiff());
				}

				peakHalfDiff = Double.MAX_VALUE;
				peakFivePercentDiff = Double.MAX_VALUE;
				rightSharpenessValue = Double.MIN_VALUE;
				for (int j = peakTopId; j <= datapoints.size() - 1; j++) {
					if (peakHalfDiff > Math.abs((datapoints.get(peakTopId).sumIntensity() - datapoints.get(datapoints.size() - 1).sumIntensity()) / 2 - (datapoints.get(j).sumIntensity() - datapoints.get(datapoints.size() - 1).sumIntensity()))) {
						peakHalfDiff = Math.abs((datapoints.get(peakTopId).sumIntensity() - datapoints.get(datapoints.size() - 1).sumIntensity()) / 2 - (datapoints.get(j).sumIntensity() - datapoints.get(datapoints.size() - 1).sumIntensity()));
						rightPeakHalfId = j;
					}

					if (peakFivePercentDiff > Math.abs((datapoints.get(peakTopId).sumIntensity() - datapoints.get(datapoints.size() - 1).sumIntensity()) / 5 - (datapoints.get(j).sumIntensity() - datapoints.get(datapoints.size() - 1).sumIntensity()))) {
						peakFivePercentDiff = Math.abs((datapoints.get(peakTopId).sumIntensity() - datapoints.get(datapoints.size() - 1).sumIntensity()) / 5 - (datapoints.get(j).sumIntensity() - datapoints.get(datapoints.size() - 1).sumIntensity()));
						rightPeakFivePercentId = j;
					}

					if (j == peakTopId) continue;

					if (rightSharpenessValue < (datapoints.get(peakTopId).sumIntensity() - datapoints.get(j).sumIntensity()) / (j - peakTopId) / Math.sqrt(datapoints.get(peakTopId).sumIntensity()))
						rightSharpenessValue = (datapoints.get(peakTopId).sumIntensity() - datapoints.get(j).sumIntensity()) / (j - peakTopId) / Math.sqrt(datapoints.get(peakTopId).sumIntensity());

					if (datapoints.get(j).firstDiff() < 0)
						idealSlopeValue += Math.abs(datapoints.get(j).firstDiff());
					else
						nonIdealSlopeValue += Math.abs(datapoints.get(j).firstDiff());
				}

				if (datapoints.get(0).sumIntensity() <= datapoints.get(datapoints.size() - 1).sumIntensity()) {
					gaussianNormalize = datapoints.get(peakTopId).sumIntensity() - datapoints.get(0).sumIntensity();
					peakHalfId = leftPeakHalfId;
					basePeakValue = Math.abs((datapoints.get(peakTopId).sumIntensity() - datapoints.get(datapoints.size() - 1).sumIntensity()) / (datapoints.get(peakTopId).sumIntensity() - datapoints.get(0).sumIntensity()));
				} else {
					gaussianNormalize = datapoints.get(peakTopId).sumIntensity() - datapoints.get(datapoints.size() - 1).sumIntensity();
					peakHalfId = rightPeakHalfId;
					basePeakValue = Math.abs((datapoints.get(peakTopId).sumIntensity() - datapoints.get(0).sumIntensity()) / (datapoints.get(peakTopId).sumIntensity() - datapoints.get(datapoints.size() - 1).sumIntensity()));
				}

				if (Math.abs(datapoints.get(peakTopId).retentionTimeInMinutes() - datapoints.get(leftPeakFivePercentId).retentionTimeInMinutes()) <= Math.abs(datapoints.get(peakTopId).retentionTimeInMinutes() - datapoints.get(rightPeakFivePercentId).retentionTimeInMinutes())) {
					symmetryValue = Math.abs(datapoints.get(peakTopId).retentionTimeInMinutes() - datapoints.get(leftPeakFivePercentId).retentionTimeInMinutes()) / Math.abs(datapoints.get(peakTopId).retentionTimeInMinutes() - datapoints.get(rightPeakFivePercentId).retentionTimeInMinutes());
				} else {
					symmetryValue = Math.abs(datapoints.get(peakTopId).retentionTimeInMinutes() - datapoints.get(rightPeakFivePercentId).retentionTimeInMinutes()) / Math.abs(datapoints.get(peakTopId).retentionTimeInMinutes() - datapoints.get(leftPeakFivePercentId).retentionTimeInMinutes());
				}

				peakHwhm = Math.abs(datapoints.get(peakHalfId).retentionTimeInMinutes() - datapoints.get(peakTopId).retentionTimeInMinutes());

				//6. calculate peak pure value (from gaussian area and real area)
				gaussianSigma = peakHwhm / Math.sqrt(2 * Math.log(2));
				gaussianArea = gaussianNormalize * gaussianSigma * Math.sqrt(2 * Math.PI) / 2;

				realAreaAboveZero = 0;
				leftPeakArea = 0;
				rightPeakArea = 0;
				for (int j = 0; j < datapoints.size() - 1; j++) {
					realAreaAboveZero += (datapoints.get(j).sumIntensity() + datapoints.get(j + 1).sumIntensity()) * (datapoints.get(j + 1).retentionTimeInMinutes() - datapoints.get(j).retentionTimeInMinutes()) * 0.5;
					if (j == peakTopId - 1) {
						leftPeakArea = realAreaAboveZero;
					} else if (j == datapoints.size() - 2) {
						rightPeakArea = realAreaAboveZero - leftPeakArea;
					}
				}
				realAreaAboveBaseline = realAreaAboveZero - (datapoints.get(0).sumIntensity() + datapoints.get(datapoints.size() - 1).sumIntensity()) * (datapoints.get(datapoints.size() - 1).retentionTimeInMinutes() - datapoints.get(0).retentionTimeInMinutes()) / 2;

				if (datapoints.get(0).sumIntensity() <= datapoints.get(datapoints.size() - 1).sumIntensity()) {
					leftPeakArea = leftPeakArea - datapoints.get(0).sumIntensity() * (datapoints.get(peakTopId).retentionTimeInMinutes() - datapoints.get(0).retentionTimeInMinutes());
					rightPeakArea = rightPeakArea - datapoints.get(0).sumIntensity() * (datapoints.get(datapoints.size() - 1).retentionTimeInMinutes() - datapoints.get(peakTopId).retentionTimeInMinutes());
				} else {
					leftPeakArea = leftPeakArea - datapoints.get(datapoints.size() - 1).sumIntensity() * (datapoints.get(peakTopId).retentionTimeInMinutes() - datapoints.get(0).retentionTimeInMinutes());
					rightPeakArea = rightPeakArea - datapoints.get(datapoints.size() - 1).sumIntensity() * (datapoints.get(datapoints.size() - 1).retentionTimeInMinutes() - datapoints.get(peakTopId).retentionTimeInMinutes());
				}

				if (gaussianArea >= leftPeakArea) gaussianSimilarityLeftValue = leftPeakArea / gaussianArea;
				else gaussianSimilarityLeftValue = gaussianArea / leftPeakArea;

				if (gaussianArea >= rightPeakArea) gaussianSimilarityRightValue = rightPeakArea / gaussianArea;
				else gaussianSimilarityRightValue = gaussianArea / rightPeakArea;

				gaussinaSimilarityValue = (gaussianSimilarityLeftValue + gaussianSimilarityRightValue) / 2;
				idealSlopeValue = (idealSlopeValue - nonIdealSlopeValue) / idealSlopeValue;

				if (idealSlopeValue < 0) idealSlopeValue = 0;

				peakPureValue = (basePeakValue + symmetryValue + gaussinaSimilarityValue) / 3;

				if (peakPureValue > 1) peakPureValue = 1;
				if (peakPureValue < 0) peakPureValue = 0;

				//7. Set peakInforamtion
				DetectedPeak result = new DetectedPeak(
						datapoints.get(0),
						peakID,
						datapoints.get(0).scanNumber(),
						datapoints.get(datapoints.size() - 1).scanNumber(),
						datapoints.get(peakTopId).scanNumber(),
						datapoints.get(0).sumIntensity(),
						datapoints.get(datapoints.size() - 1).sumIntensity(),
						datapoints.get(peakTopId).sumIntensity(),
						(realAreaAboveZero * 60),
						(realAreaAboveBaseline * 60),
						datapoints.get(0).retentionTimeInMinutes(),
						datapoints.get(datapoints.size() - 1).retentionTimeInMinutes(),
						datapoints.get(peakTopId).retentionTimeInMinutes(),
						peakPureValue,
						((leftSharpenessValue + rightSharpenessValue) / 2),
						gaussinaSimilarityValue,
						idealSlopeValue,
						basePeakValue,
						symmetryValue,
						-1,
						-1
				);

				peakDetectionResults.add(result);
				peakID++;
			}
		}

		return peakDetectionResults;
	}


	public static List<PeakDetectionResult> detectGCPeaks(List<double[]> peaklist,
	                                                      double minimumDatapointCriteria, double minimumAmplitudeCriteria,
	                                                      double amplitudeNoiseFactor, double slopeNoiseFactor, double peaktopNoiseFactor, int averagePeakWidth) {
		List<PeakDetectionResult> detectedPeaks = new ArrayList<>();

		//Differential calculation
		List<Double> firstDiffPeaklist = new ArrayList<>();
		List<Double> secondDiffPeaklist = new ArrayList<>();
		double firstDiff;
		double secondDiff;
		double maxFirstDiff = Double.MIN_VALUE;
		double maxSecondDiff = Double.MIN_VALUE;
		double maxAmplitudeDiff = Double.MIN_VALUE;
		int halfDatapoint = (firstDiffCoeff.length / 2);
		int peakID = 0;

		for (int i = 0; i < peaklist.size(); i++) {
			if (i < halfDatapoint) {
				firstDiffPeaklist.add(0D);
				secondDiffPeaklist.add(0D);
				continue;
			}
			if (i >= peaklist.size() - halfDatapoint) {
				firstDiffPeaklist.add(0D);
				secondDiffPeaklist.add(0D);
				continue;
			}

			firstDiff = secondDiff = 0;
			for (int j = 0; j < firstDiffCoeff.length; j++) {
				firstDiff += firstDiffCoeff[j] * peaklist.get(i + j - halfDatapoint)[3];
				secondDiff += secondDiffCoeff[j] * peaklist.get(i + j - halfDatapoint)[3];
			}
			firstDiffPeaklist.add(firstDiff);
			secondDiffPeaklist.add(secondDiff);

			if (Math.abs(firstDiff) > maxFirstDiff) {
				maxFirstDiff = Math.abs(firstDiff);
			}
			if (secondDiff < 0 && maxSecondDiff < -1 * secondDiff) {
				maxSecondDiff = -1 * secondDiff;
			}
			if (Math.abs(peaklist.get(i)[3] - peaklist.get(i - 1)[3]) > maxAmplitudeDiff) {
				maxAmplitudeDiff = Math.abs(peaklist.get(i)[3] - peaklist.get(i - 1)[3]);
			}
		}

		//Noise estimate
		List<Double> amplitudeNoiseCandidate = new ArrayList<>();
		List<Double> slopeNoiseCandidate = new ArrayList<>();
		List<Double> peaktopNoiseCandidate = new ArrayList<>();
		double amplitudeNoise;
		double slopeNoise;
		double peaktopNoise;
		double amplitudeNoiseThresh = maxAmplitudeDiff * 0.05;
		double slopeNoiseThresh = maxFirstDiff * 0.05;
		double peaktopNoiseThresh = maxSecondDiff * 0.05;

		for (int i = 2; i < peaklist.size() - 2; i++) {
			if (Math.abs(peaklist.get(i + 1)[3] - peaklist.get(i)[3]) < amplitudeNoiseThresh && Math.abs(peaklist.get(i + 1)[3] - peaklist.get(i)[3]) > 0) {
				amplitudeNoiseCandidate.add(Math.abs(peaklist.get(i + 1)[3] - peaklist.get(i)[3]));
			}
			if (Math.abs(firstDiffPeaklist.get(i)) < slopeNoiseThresh && Math.abs(firstDiffPeaklist.get(i)) > 0) {
				slopeNoiseCandidate.add(Math.abs(firstDiffPeaklist.get(i)));
			}
			if (secondDiffPeaklist.get(i) < 0 && Math.abs(secondDiffPeaklist.get(i)) < peaktopNoiseThresh && Math.abs(secondDiffPeaklist.get(i)) > 0) {
				peaktopNoiseCandidate.add(Math.abs(secondDiffPeaklist.get(i)));
			}
		}
		if (amplitudeNoiseCandidate.size() == 0) {
			amplitudeNoise = 0.0001;
		} else {
//			amplitudeNoise = BasicMathematics.BrokenMedian(amplitudeNoiseCandidate);
			amplitudeNoise = BasicMathematics.Median(amplitudeNoiseCandidate);
		}
		if (slopeNoiseCandidate.size() == 0) {
			slopeNoise = 0.0001;
		} else {
//			slopeNoise = BasicMathematics.BrokenMedian(slopeNoiseCandidate);
			slopeNoise = BasicMathematics.Median(slopeNoiseCandidate);
		}
		if (peaktopNoiseCandidate.size() == 0) {
			peaktopNoise = 0.0001;
		} else {
//			peaktopNoise = BasicMathematics.BrokenMedian(peaktopNoiseCandidate);
			peaktopNoise = BasicMathematics.Median(peaktopNoiseCandidate);
		}

		//Search peaks
		List<double[]> datapoints;
		double peakTopIntensity;
		double peakHwhm;
		double peakHalfDiff;
		double peakFivePercentDiff;
		double leftSharpenessValue;
		double rightSharpenessValue;
		double gaussianSigma;
		double gaussianNormalize;
		double gaussianArea;
		double gaussinaSimilarityValue;
		double gaussianSimilarityLeftValue;
		double gaussianSimilarityRightValue;
		double realAreaAboveZero;
		double realAreaAboveBaseline;
		double leftPeakArea;
		double rightPeakArea;
		double idealSlopeValue;
		double nonIdealSlopeValue;
		double symmetryValue;
		double basePeakValue;
		double peakPureValue;
		int peaktopCheckPoint;
		int peakTopId = -1;
		int peakHalfId = -1;
		int leftPeakFivePercentId = -1;
		int rightPeakFivePercentId = -1;
		int leftPeakHalfId = -1;
		int rightPeakHalfId = -1;
		boolean peaktopCheck = false;

		int excludedLeftCutPoint = 0;
		int excludedRightCutPoint = 0;

		for (int i = 0; i < peaklist.size(); i++) {
			if (i >= peaklist.size() - 1 - minimumDatapointCriteria) break;
			//1. Left edge criteria
			if (firstDiffPeaklist.get(i) > slopeNoise * slopeNoiseFactor && firstDiffPeaklist.get(i + 1) > slopeNoise * slopeNoiseFactor) {
				datapoints = new ArrayList<>();
				datapoints.add(new double[]{peaklist.get(i)[0], peaklist.get(i)[1], peaklist.get(i)[2], peaklist.get(i)[3], firstDiffPeaklist.get(i), secondDiffPeaklist.get(i)});

				//search real left edge within 5 data points
				for (int j = 0; j <= 5; j++) {
					if (i - j - 1 < 0) break;
					if (peaklist.get(i - j)[3] <= peaklist.get(i - j - 1)[3])
						break;
					if (peaklist.get(i - j)[3] > peaklist.get(i - j - 1)[3])
						datapoints.add(0, new double[]{peaklist.get(i - j - 1)[0], peaklist.get(i - j - 1)[1], peaklist.get(i - j - 1)[2], peaklist.get(i - j - 1)[3], firstDiffPeaklist.get(i - j - 1), secondDiffPeaklist.get(i - j - 1)});
				}

				//2. Right edge criteria
				peaktopCheck = false;
				peaktopCheckPoint = i;
				while (true) {
					if (i + 1 == peaklist.size() - 1) break;

					i++;
					datapoints.add(new double[]{peaklist.get(i)[0], peaklist.get(i)[1], peaklist.get(i)[2], peaklist.get(i)[3], firstDiffPeaklist.get(i), secondDiffPeaklist.get(i)});
					if (!peaktopCheck && firstDiffPeaklist.get(i - 1) > 0 &&
							firstDiffPeaklist.get(i) < 0 &&
							secondDiffPeaklist.get(i) < -1 * peaktopNoise * peaktopNoiseFactor) {
						peaktopCheck = true;
						peaktopCheckPoint = i;
					}
					if (peaktopCheck && peaktopCheckPoint + 2 + (int) (minimumDatapointCriteria / 2) <= i - 1 &&
							firstDiffPeaklist.get(i - 1) > -1 * slopeNoise * slopeNoiseFactor &&
							firstDiffPeaklist.get(i) > -1 * slopeNoise * slopeNoiseFactor)
						break;
				}

				//Search real right edge within 5 data points
				//case: wrong edge is in left of real edge
				for (int j = 0; j <= 5; j++) {
					if (i + j + 1 > peaklist.size() - 1) break;
					if (peaklist.get(i + j)[3] <= peaklist.get(i + j + 1)[3]) break;
					if (peaklist.get(i + j)[3] > peaklist.get(i + j + 1)[3]) {
						datapoints.add(new double[]{peaklist.get(i + j + 1)[0], peaklist.get(i + j + 1)[1], peaklist.get(i + j + 1)[2], peaklist.get(i + j + 1)[3], firstDiffPeaklist.get(i + j + 1), secondDiffPeaklist.get(i + j + 1)});
					}
				}
				//case: wrong edge is in right of real edge
				for (int j = 0; j <= 5; j++) {
					if (i - j - 1 < 0) break;
					if (peaklist.get(i - j)[3] <= peaklist.get(i - j - 1)[3]) break;
					if (peaklist.get(i - j)[3] > peaklist.get(i - j - 1)[3]) {
						datapoints.remove(datapoints.size() - 1);
					}
				}

				//3. Check minimum datapoint criteria
				if (datapoints.size() < minimumDatapointCriteria) continue;


				//4. Check peak half height at half width
				peakTopIntensity = Double.MIN_VALUE;
				peakTopId = -1;
				for (int j = 0; j < datapoints.size(); j++) {
					if (peakTopIntensity < datapoints.get(j)[3]) {
						peakTopIntensity = datapoints.get(j)[3];
						peakTopId = j;
					}
				}
				if (datapoints.get(peakTopId)[3] - datapoints.get(0)[3] < minimumAmplitudeCriteria ||
						datapoints.get(peakTopId)[3] - datapoints.get(datapoints.size() - 1)[3] < minimumAmplitudeCriteria ||
						datapoints.get(peakTopId)[3] - datapoints.get(0)[3] < amplitudeNoise * amplitudeNoiseFactor ||
						datapoints.get(peakTopId)[3] - datapoints.get(datapoints.size() - 1)[3] < amplitudeNoise * amplitudeNoiseFactor) {
					continue;
				}
				if (peakTopId > averagePeakWidth) {
					excludedLeftCutPoint = 0;
					for (int j = peakTopId - averagePeakWidth; j >= 0; j--) {
						if (j - 1 <= 0) break;
						if (datapoints.get(j)[3] <= datapoints.get(j - 1)[3]) {
							excludedLeftCutPoint = j;
							break;
						}
					}
					if (excludedLeftCutPoint > 0) {
						for (int j = 0; j < excludedLeftCutPoint; j++) datapoints.remove(0);
						peakTopId = peakTopId - excludedLeftCutPoint;
					}
				}
				if (datapoints.size() - 1 > peakTopId + averagePeakWidth) {
					excludedRightCutPoint = 0;
					for (int j = peakTopId + averagePeakWidth; j < datapoints.size(); j++) {
						if (j + 1 > datapoints.size() - 1) break;
						if (datapoints.get(j)[3] <= datapoints.get(j + 1)[3]) {
							excludedRightCutPoint = datapoints.size() - 1 - j;
							break;
						}
					}
					if (excludedRightCutPoint > 0) {
						for (int j = 0; j < excludedRightCutPoint; j++) datapoints.remove(datapoints.size() - 1);
					}
				}

				//5. Check HWHM criteria and calculate sharpeness value, symmetry value, base peak value, ideal value, non ideal value
				idealSlopeValue = 0;
				nonIdealSlopeValue = 0;
				peakHalfDiff = Double.MAX_VALUE;
				peakFivePercentDiff = Double.MAX_VALUE;
				leftSharpenessValue = Double.MIN_VALUE;
				for (int j = peakTopId; j >= 0; j--) {
					double currPeakHalfWidth = Math.abs((datapoints.get(peakTopId)[3] - datapoints.get(0)[3]) / 2 - (datapoints.get(j)[3] - datapoints.get(0)[3]));
					if (peakHalfDiff > currPeakHalfWidth) {
						peakHalfDiff = currPeakHalfWidth;
						leftPeakHalfId = j;
					}

					double currPeakFivePercent = Math.abs((datapoints.get(peakTopId)[3] - datapoints.get(0)[3]) / 5 - (datapoints.get(j)[3] - datapoints.get(0)[3]));
					if (peakFivePercentDiff > currPeakFivePercent) {
						peakFivePercentDiff = currPeakFivePercent;
						leftPeakFivePercentId = j;
					}

					if (j == peakTopId) continue;

					double currSharpness = (datapoints.get(peakTopId)[3] - datapoints.get(j)[3]) / (peakTopId - j) / Math.sqrt(datapoints.get(peakTopId)[3]);
					if (leftSharpenessValue < currSharpness)
						leftSharpenessValue = currSharpness;

					double currSlope = datapoints.get(j + 1)[3] - datapoints.get(j)[3];
					if (currSlope >= 0)
						idealSlopeValue += Math.abs(currSlope);
					else
						nonIdealSlopeValue += Math.abs(currSlope);
				}

				peakHalfDiff = Double.MAX_VALUE;
				peakFivePercentDiff = Double.MAX_VALUE;
				rightSharpenessValue = Double.MIN_VALUE;
				for (int j = peakTopId; j <= datapoints.size() - 1; j++) {
					double currPeakHalfDiff = Math.abs((datapoints.get(peakTopId)[3] - datapoints.get(datapoints.size() - 1)[3]) / 2 - (datapoints.get(j)[3] - datapoints.get(datapoints.size() - 1)[3]));
					if (peakHalfDiff > currPeakHalfDiff) {
						peakHalfDiff = currPeakHalfDiff;
						rightPeakHalfId = j;
					}

					double currPeakFivePercent = Math.abs((datapoints.get(peakTopId)[3] - datapoints.get(datapoints.size() - 1)[3]) / 5 - (datapoints.get(j)[3] - datapoints.get(datapoints.size() - 1)[3]));
					if (peakFivePercentDiff > currPeakFivePercent) {
						peakFivePercentDiff = currPeakFivePercent;
						rightPeakFivePercentId = j;
					}

					if (j == peakTopId) continue;

					double currSharpness = (datapoints.get(peakTopId)[3] - datapoints.get(j)[3]) / (j - peakTopId) / Math.sqrt(datapoints.get(peakTopId)[3]);
					if (rightSharpenessValue < currSharpness)
						rightSharpenessValue = currSharpness;

					double currSlope = datapoints.get(j - 1)[3] - datapoints.get(j)[3];
					if (currSlope >= 0)
						idealSlopeValue += Math.abs(currSlope);
					else
						nonIdealSlopeValue += Math.abs(currSlope);
				}

				if (datapoints.get(0)[3] <= datapoints.get(datapoints.size() - 1)[3]) {
					gaussianNormalize = datapoints.get(peakTopId)[3] - datapoints.get(0)[3];
					peakHalfId = leftPeakHalfId;
					basePeakValue = Math.abs((datapoints.get(peakTopId)[3] - datapoints.get(datapoints.size() - 1)[3]) / (datapoints.get(peakTopId)[3] - datapoints.get(0)[3]));
				} else {
					gaussianNormalize = datapoints.get(peakTopId)[3] - datapoints.get(datapoints.size() - 1)[3];
					peakHalfId = rightPeakHalfId;
					basePeakValue = Math.abs((datapoints.get(peakTopId)[3] - datapoints.get(0)[3]) / (datapoints.get(peakTopId)[3] - datapoints.get(datapoints.size() - 1)[3]));
				}

				if (Math.abs(datapoints.get(peakTopId)[1] - datapoints.get(leftPeakFivePercentId)[1]) <= Math.abs(datapoints.get(peakTopId)[1] - datapoints.get(rightPeakFivePercentId)[1])) {
					symmetryValue = Math.abs(datapoints.get(peakTopId)[1] - datapoints.get(leftPeakFivePercentId)[1]) / Math.abs(datapoints.get(peakTopId)[1] - datapoints.get(rightPeakFivePercentId)[1]);
				} else {
					symmetryValue = Math.abs(datapoints.get(peakTopId)[1] - datapoints.get(rightPeakFivePercentId)[1]) / Math.abs(datapoints.get(peakTopId)[1] - datapoints.get(leftPeakFivePercentId)[1]);
				}

				peakHwhm = Math.abs(datapoints.get(peakHalfId)[1] - datapoints.get(peakTopId)[1]);


				//6. calculate peak pure value (from gaussian area and real area)
				gaussianSigma = peakHwhm / Math.sqrt(2 * Math.log(2));
				gaussianArea = gaussianNormalize * gaussianSigma * Math.sqrt(2 * Math.PI) / 2;

				realAreaAboveZero = 0;
				leftPeakArea = 0;
				rightPeakArea = 0;
				for (int j = 0; j < datapoints.size() - 1; j++) {
					realAreaAboveZero += (datapoints.get(j)[3] + datapoints.get(j + 1)[3]) * (datapoints.get(j + 1)[1] - datapoints.get(j)[1]) * 0.5;
					if (j == peakTopId - 1) {
						leftPeakArea = realAreaAboveZero;
					} else if (j == datapoints.size() - 2) {
						rightPeakArea = realAreaAboveZero - leftPeakArea;
					}
				}
				realAreaAboveBaseline = realAreaAboveZero - (datapoints.get(0)[3] + datapoints.get(datapoints.size() - 1)[3]) * (datapoints.get(datapoints.size() - 1)[1] - datapoints.get(0)[1]) / 2;

				if (datapoints.get(0)[3] <= datapoints.get(datapoints.size() - 1)[3]) {
					leftPeakArea = leftPeakArea - datapoints.get(0)[3] * (datapoints.get(peakTopId)[1] - datapoints.get(0)[1]);
					rightPeakArea = rightPeakArea - datapoints.get(0)[3] * (datapoints.get(datapoints.size() - 1)[1] - datapoints.get(peakTopId)[1]);
				} else {
					leftPeakArea = leftPeakArea - datapoints.get(datapoints.size() - 1)[3] * (datapoints.get(peakTopId)[1] - datapoints.get(0)[1]);
					rightPeakArea = rightPeakArea - datapoints.get(datapoints.size() - 1)[3] * (datapoints.get(datapoints.size() - 1)[1] - datapoints.get(peakTopId)[1]);
				}

				if (gaussianArea >= leftPeakArea) gaussianSimilarityLeftValue = leftPeakArea / gaussianArea;
				else gaussianSimilarityLeftValue = gaussianArea / leftPeakArea;

				if (gaussianArea >= rightPeakArea) gaussianSimilarityRightValue = rightPeakArea / gaussianArea;
				else gaussianSimilarityRightValue = gaussianArea / rightPeakArea;

				gaussinaSimilarityValue = (gaussianSimilarityLeftValue + gaussianSimilarityRightValue) / 2;
				idealSlopeValue = (idealSlopeValue - nonIdealSlopeValue) / idealSlopeValue;

				if (idealSlopeValue < 0) idealSlopeValue = 0;

				peakPureValue = (gaussinaSimilarityValue + 1.2 * basePeakValue + 0.8 * symmetryValue + idealSlopeValue) / 4;
				if (peakPureValue > 1) peakPureValue = 1;
				if (peakPureValue < 0) peakPureValue = 0;

				//7. Set peakInforamtion
				PeakDetectionResult result = new PeakDetectionResult(peakID, -1, -1, realAreaAboveBaseline * 60, realAreaAboveZero * 60, basePeakValue, gaussinaSimilarityValue, idealSlopeValue,
						datapoints.get(0)[3],
						datapoints.get(peakTopId)[3],
						datapoints.get(datapoints.size() - 1)[3],
						peakPureValue,
						datapoints.get(0)[1],
						datapoints.get(peakTopId)[1],
						datapoints.get(datapoints.size() - 1)[1],
						(int)datapoints.get(0)[0],
						(int)datapoints.get(peakTopId)[0],
						(int)datapoints.get(datapoints.size() - 1)[0],
						(leftSharpenessValue + rightSharpenessValue) / 2,
						symmetryValue
				);

				detectedPeaks.add(result);
				peakID++;
			}
		}

		//finalize
		if (detectedPeaks.size() == 0) return null;

//		detectedPeaks = detectedPeaks.stream().sorted(Comparator.comparing(DetectedPeak::intensityAtPeakTop).reversed()).collect(Collectors.toList());
		detectedPeaks.sort(Comparator.comparing(PeakDetectionResult::intensityAtPeakTop).reversed());
		double maxIntensity = detectedPeaks.get(0).intensityAtPeakTop();

		for (int i = 0; i < detectedPeaks.size(); i++) {
			detectedPeaks.get(i).amplitudeScoreValue = detectedPeaks.get(i).intensityAtPeakTop / maxIntensity;
			detectedPeaks.get(i).amplitudeOrderValue = i + 1;
		}

		detectedPeaks.sort(Comparator.comparing(PeakDetectionResult::peakID));

		return detectedPeaks;
	}
}
