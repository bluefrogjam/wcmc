package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.peakpicking.gcms;

import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakDetectionResult;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.BasicMathematics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by diego on 7/21/16.
 */
public class GCMSDifferentialBasedPeakDetection {

    private static Logger logger = LoggerFactory.getLogger(GCMSDifferentialBasedPeakDetection.class);

    private static final double[] firstDiffCoeff = new double[] {-0.2, -0.1, 0, 0.1, 0.2};
    private static final double[] secondDiffCoeff = new double[] {0.14285714, -0.07142857, -0.1428571, -0.07142857, 0.14285714};


    /**
     * Very similar to the LC/MS peak detection, but with enough differences to make code duplication easier for now
     * TODO: Combine the two peak detection method into a single class
     * @param peakList
     * @param minimumDatapointCriteria
     * @param minimumAmplitudeCriteria
     * @param amplitudeNoiseFoldCriteria
     * @param slopeNoiseFoldCriteria
     * @param peaktopNoiseFoldCriteria
     * @return
     */
    public static List<PeakDetectionResult> detectPeaks(List<double[]> peakList, double minimumDatapointCriteria,
                                                        double minimumAmplitudeCriteria, double amplitudeNoiseFoldCriteria,
                                                        double slopeNoiseFoldCriteria, int averagePeakWidth,
                                                        double peaktopNoiseFoldCriteria) {

        List<PeakDetectionResult> peakDetectionResults = new ArrayList<>();

        // Differential calculation
        List<Double> firstDiffPeakList = new ArrayList<>();
        List<Double> secondDiffPeakList = new ArrayList<>();

        double maxFirstDiff = Double.MIN_VALUE, maxSecondDiff = Double.MIN_VALUE, maxAmplitudeDiff = Double.MIN_VALUE;
        int halfDatapoint = (int) (firstDiffCoeff.length / 2), peakID = 0;

        for (int i = 0; i < peakList.size(); i++) {
            if (i < halfDatapoint || i >= peakList.size() - halfDatapoint) {
                firstDiffPeakList.add(0.0);
                secondDiffPeakList.add(0.0);
                continue;
            }

            double firstDiff = 0, secondDiff = 0;

            for (int j = 0; j < firstDiffCoeff.length; j++) {
                firstDiff += firstDiffCoeff[j] * peakList.get(i + j - halfDatapoint)[3];
                secondDiff += secondDiffCoeff[j] * peakList.get(i + j - halfDatapoint)[3];
            }

            firstDiffPeakList.add(firstDiff);
            secondDiffPeakList.add(secondDiff);

            if (Math.abs(firstDiff) > maxFirstDiff) {
                maxFirstDiff = Math.abs(firstDiff);
            }

            if (secondDiff < 0 && maxSecondDiff < -1 * secondDiff) {
                maxSecondDiff = -1 * secondDiff;
            }

            if (Math.abs(peakList.get(i)[3] - peakList.get(i - 1)[3]) > maxAmplitudeDiff) {
                maxAmplitudeDiff = Math.abs(peakList.get(i)[3] - peakList.get(i - 1)[3]);
            }
        }


        // Noise estimate
        List<Float> amplitudeNoiseCandidate = new ArrayList<>();
        List<Float> slopeNoiseCandidate = new ArrayList<>();
        List<Float> peaktopNoiseCandidate = new ArrayList<>();

        double amplitudeNoiseThresh = maxAmplitudeDiff * 0.05;
        double slopeNoiseThresh = maxFirstDiff * 0.05;
        double peaktopNoiseThresh = maxSecondDiff * 0.05;

        for (int i = 2; i < peakList.size() - 2; i++) {
            if (Math.abs(peakList.get(i + 1)[3] - peakList.get(i)[3]) < amplitudeNoiseThresh && Math.abs(peakList.get(i + 1)[3] - peakList.get(i)[3]) > 0) {
                amplitudeNoiseCandidate.add((float)Math.abs(peakList.get(i + 1)[3] - peakList.get(i)[3]));
            }

            if (Math.abs(firstDiffPeakList.get(i)) < slopeNoiseThresh && Math.abs(firstDiffPeakList.get(i)) > 0) {
                slopeNoiseCandidate.add((float)Math.abs(firstDiffPeakList.get(i)));
            }

            if (secondDiffPeakList.get(i) < 0 && Math.abs(secondDiffPeakList.get(i)) < peaktopNoiseThresh && Math.abs(secondDiffPeakList.get(i)) > 0) {
                peaktopNoiseCandidate.add((float)Math.abs(secondDiffPeakList.get(i)));
            }
        }

        double amplitudeNoise = amplitudeNoiseCandidate.isEmpty() ? 0.0001 : BasicMathematics.brokenMedian(amplitudeNoiseCandidate);
        double slopeNoise = slopeNoiseCandidate.isEmpty() ? 0.0001 : BasicMathematics.brokenMedian(slopeNoiseCandidate);
        double peaktopNoise = peaktopNoiseCandidate.isEmpty() ? 0.0001 : BasicMathematics.brokenMedian(peaktopNoiseCandidate);


        // Search peaks
        List<double[]> dataPoints;

        double peakTopIntensity, peakHwhm, peakHalfDiff, peakFivePercentDiff, leftShapenessValue, rightShapenessValue,
            gaussianSigma, gaussianNormalize, gaussianArea, gaussinaSimilarityValue, gaussianSimilarityLeftValue, gaussianSimilarityRightValue,
            realAreaAboveZero, realAreaAboveBaseline, leftPeakArea, rightPeakArea, idealSlopeValue, nonIdealSlopeValue, symmetryValue, basePeakValue, peakPureValue;

        int peaktopCheckPoint, peakTopId, peakHalfId, leftPeakFivePercentId = -1, rightPeakFivePercentId = -1, leftPeakHalfId = -1, rightPeakHalfId = -1;

        for (int i = 0; i < peakList.size(); i++) {

            if (i >= peakList.size() - 1 - minimumDatapointCriteria || i >= peakList.size() - 5) {
                break;
            }

            // 1. Left edge criteria
            if (firstDiffPeakList.get(i) > slopeNoise * slopeNoiseFoldCriteria && firstDiffPeakList.get(i + 1) > slopeNoise * slopeNoiseFoldCriteria) {

                dataPoints = new ArrayList<>();
                dataPoints.add(new double[] {peakList.get(i)[0], peakList.get(i)[1], peakList.get(i)[2], peakList.get(i)[3], firstDiffPeakList.get(i), secondDiffPeakList.get(i)});

                // Search real left edge within 5 data points
                for (int j = 0; j <= 5; j++) {
                    if (i - j - 1 < 0)
                        break;

                    if (peakList.get(i - j)[3] <= peakList.get(i - j - 1)[3])
                        break;

                    if (peakList.get(i - j)[3] > peakList.get(i - j - 1)[3])
                        dataPoints.add(0, new double[]  {peakList.get(i - j - 1)[0], peakList.get(i - j - 1)[1], peakList.get(i - j - 1)[2], peakList.get(i - j - 1)[3], firstDiffPeakList.get(i - j - 1), secondDiffPeakList.get(i - j - 1)});
                }


                // 2. Right edge criteria
                boolean peaktopCheck = false;
                peaktopCheckPoint = i;

                while (true) {
                    if (i + 1 == peakList.size() - 1)
                        break;

                    i++;
                    dataPoints.add(new double[] {peakList.get(i)[0], peakList.get(i)[1], peakList.get(i)[2], peakList.get(i)[3], firstDiffPeakList.get(i), secondDiffPeakList.get(i)});

                    if (!peaktopCheck && firstDiffPeakList.get(i - 1) > 0 && firstDiffPeakList.get(i) < 0 && secondDiffPeakList.get(i) < -1 * peaktopNoise * peaktopNoiseFoldCriteria) {
                        peaktopCheck = true;
                        peaktopCheckPoint = i;
                    }

                    if (peaktopCheck && peaktopCheckPoint + 2 + (int)(minimumDatapointCriteria / 2) <= i - 1 &&
                            firstDiffPeakList.get(i - 1) > -1 * slopeNoise * slopeNoiseFoldCriteria &&
                            firstDiffPeakList.get(i) > -1 * slopeNoise * slopeNoiseFoldCriteria) {
                        break;
                    }
                }

                // Search real right edge within 5 data points
                // Case: wrong edge is in left of real edge
                for (int j = 0; j <= 5; j++) {
                    if (i + j + 1 > peakList.size() - 1)
                        break;

                    if (peakList.get(i + j)[3] <= peakList.get(i + j + 1)[3])
                        break;

                    if (peakList.get(i + j)[3] > peakList.get(i + j + 1)[3]) {
                        dataPoints.add(new double[] {peakList.get(i + j + 1)[0], peakList.get(i + j + 1)[1], peakList.get(i + j + 1)[2], peakList.get(i + j + 1)[3], firstDiffPeakList.get(i + j + 1), secondDiffPeakList.get(i + j + 1)});
                    }
                }

                // Case: wrong edge is in right of real edge
                for (int j = 0; j <= 5; j++) {
                    if (i - j - 1 < 0)
                        break;

                    if (peakList.get(i - j)[3] <= peakList.get(i - j - 1)[3])
                        break;

                    if (peakList.get(i - j)[3] > peakList.get(i - j - 1)[3]) {
                        dataPoints.remove(dataPoints.size() - 1);
                    }
                }


                // 3. Check minimum datapoint criteria
                if (dataPoints.size() < minimumDatapointCriteria)
                    continue;


                //4. Check peak half height at half width
                peakTopIntensity = Double.MIN_VALUE;
                peakTopId = -1;

                for (int j = 0; j < dataPoints.size(); j++) {
                    if (peakTopIntensity < dataPoints.get(j)[3]) {
                        peakTopIntensity = dataPoints.get(j)[3];
                        peakTopId = j;
                    }
                }

                if (dataPoints.get(peakTopId)[3] - dataPoints.get(0)[3] < minimumAmplitudeCriteria ||
                        dataPoints.get(peakTopId)[3] - dataPoints.get(dataPoints.size() - 1)[3] < minimumAmplitudeCriteria ||
                        dataPoints.get(peakTopId)[3] - dataPoints.get(0)[3] < amplitudeNoise * amplitudeNoiseFoldCriteria ||
                        dataPoints.get(peakTopId)[3] - dataPoints.get(dataPoints.size() - 1)[3] < amplitudeNoise * amplitudeNoiseFoldCriteria) {
                    continue;
                }

                if (peakTopId > averagePeakWidth) {
                    int excludedLeftCutPoint = 0;

                    for (int j = peakTopId - averagePeakWidth; j >= 0; j--) {
                        if (j - 1 <= 0)
                            break;

                        if (dataPoints.get(j)[3] <= dataPoints.get(j - 1)[3]) {
                            excludedLeftCutPoint = j;
                            break;
                        }
                    }

                    if (excludedLeftCutPoint > 0) {
                        for (int j = 0; j < excludedLeftCutPoint; j++)
                            dataPoints.remove(0);

                        peakTopId = peakTopId - excludedLeftCutPoint;
                    }
                }

                if (dataPoints.size() - 1 > peakTopId + averagePeakWidth) {
                    int excludedRightCutPoint = 0;

                    for (int j = peakTopId + averagePeakWidth; j < dataPoints.size(); j++) {
                        if (j + 1 > dataPoints.size() - 1)
                            break;

                        if (dataPoints.get(j)[3] <= dataPoints.get(j + 1)[3]) {
                            excludedRightCutPoint = dataPoints.size() - 1 - j;
                            break;
                        }
                    }

                    if (excludedRightCutPoint > 0) {
                        for (int j = 0; j < excludedRightCutPoint; j++)
                            dataPoints.remove(dataPoints.size() - 1);
                    }
                }


                // 5. Check HWHM criteria and calculate shapeness value, symmetry value, base peak value, ideal value, non ideal value
                idealSlopeValue = 0;
                nonIdealSlopeValue = 0;
                peakHalfDiff = Double.MAX_VALUE;
                peakFivePercentDiff = Double.MAX_VALUE;
                leftShapenessValue = Double.MIN_VALUE;

                for (int j = peakTopId; j >= 0; j--) {
                    double currPeakHalfWidth = Math.abs((dataPoints.get(peakTopId)[3] - dataPoints.get(0)[3]) / 2 - (dataPoints.get(j)[3] - dataPoints.get(0)[3]));

                    if (peakHalfDiff > currPeakHalfWidth) {
                        peakHalfDiff = currPeakHalfWidth;
                        leftPeakHalfId = j;
                    }

                    double currPeakFivePercent = Math.abs((dataPoints.get(peakTopId)[3] - dataPoints.get(0)[3]) / 5 - (dataPoints.get(j)[3] - dataPoints.get(0)[3]));

                    if (peakFivePercentDiff > currPeakFivePercent) {
                        peakFivePercentDiff = currPeakFivePercent;
                        leftPeakFivePercentId = j;
                    }

                    if (j == peakTopId)
                        continue;

                    double currSharpness = (dataPoints.get(peakTopId)[3] - dataPoints.get(j)[3]) / (peakTopId - j) / Math.sqrt(dataPoints.get(peakTopId)[3]);

                    if (leftShapenessValue < currSharpness)
                        leftShapenessValue = currSharpness;

                    double currSlope = dataPoints.get(j + 1)[3] - dataPoints.get(j)[3];

                    if (currSlope > 0)
                        idealSlopeValue += Math.abs(currSlope);
                    else
                        nonIdealSlopeValue += Math.abs(currSlope);
                }

                peakHalfDiff = Double.MAX_VALUE;
                peakFivePercentDiff = Double.MAX_VALUE;
                rightShapenessValue = Double.MIN_VALUE;

                for (int j = peakTopId; j <= dataPoints.size() - 1; j++) {
                    double currPeakHalfDiff = Math.abs((dataPoints.get(peakTopId)[3] - dataPoints.get(dataPoints.size() - 1)[3]) / 2 - (dataPoints.get(j)[3] - dataPoints.get(dataPoints.size() - 1)[3]));

                    if (peakHalfDiff > currPeakHalfDiff) {
                        peakHalfDiff = currPeakHalfDiff;
                        rightPeakHalfId = j;
                    }

                    double currPeakFivePercent = Math.abs((dataPoints.get(peakTopId)[3] - dataPoints.get(dataPoints.size() - 1)[3]) / 5 - (dataPoints.get(j)[3] - dataPoints.get(dataPoints.size() - 1)[3]));

                    if (peakFivePercentDiff > currPeakFivePercent) {
                        peakFivePercentDiff = currPeakFivePercent;
                        rightPeakFivePercentId = j;
                    }

                    if (j == peakTopId)
                        continue;

                    double currSharpness = (dataPoints.get(peakTopId)[3] - dataPoints.get(j)[3]) / (j - peakTopId) / Math.sqrt(dataPoints.get(peakTopId)[3]);

                    if (rightShapenessValue < currSharpness)
                        rightShapenessValue = currSharpness;

                    double currSlope = dataPoints.get(j)[4];

                    if (currSlope > 0)
                        idealSlopeValue += Math.abs(currSlope);
                    else
                        nonIdealSlopeValue += Math.abs(currSlope);
                }

                if (dataPoints.get(0)[3] <= dataPoints.get(dataPoints.size() - 1)[3]) {
                    gaussianNormalize = dataPoints.get(peakTopId)[3] - dataPoints.get(0)[3];
                    peakHalfId = leftPeakHalfId;
                    basePeakValue = Math.abs((dataPoints.get(peakTopId)[3] - dataPoints.get(dataPoints.size() - 1)[3]) / (dataPoints.get(peakTopId)[3] - dataPoints.get(0)[3]));
                } else {
                    gaussianNormalize = dataPoints.get(peakTopId)[3] - dataPoints.get(dataPoints.size() - 1)[3];
                    peakHalfId = rightPeakHalfId;
                    basePeakValue = Math.abs((dataPoints.get(peakTopId)[3] - dataPoints.get(0)[3]) / (dataPoints.get(peakTopId)[3] - dataPoints.get(dataPoints.size() - 1)[3]));
                }

                if (Math.abs(dataPoints.get(peakTopId)[1] - dataPoints.get(leftPeakFivePercentId)[1]) <= Math.abs(dataPoints.get(peakTopId)[1] - dataPoints.get(rightPeakFivePercentId)[1])) {
                    symmetryValue = Math.abs(dataPoints.get(peakTopId)[1] - dataPoints.get(leftPeakFivePercentId)[1]) / Math.abs(dataPoints.get(peakTopId)[1] - dataPoints.get(rightPeakFivePercentId)[1]);
                } else {
                    symmetryValue = Math.abs(dataPoints.get(peakTopId)[1] - dataPoints.get(rightPeakFivePercentId)[1]) / Math.abs(dataPoints.get(peakTopId)[1] - dataPoints.get(leftPeakFivePercentId)[1]);
                }

                peakHwhm = Math.abs(dataPoints.get(peakHalfId)[1] - dataPoints.get(peakTopId)[1]);


                // 6. calculate peak pure value (from gaussian area and real area)
                gaussianSigma = peakHwhm / Math.sqrt(2 * Math.log(2));
                gaussianArea = gaussianNormalize * gaussianSigma * Math.sqrt(2 * Math.PI) / 2;

                realAreaAboveZero = 0;
                leftPeakArea = 0;
                rightPeakArea = 0;

                for (int j = 0; j < dataPoints.size() - 1; j++) {
                    realAreaAboveZero += (dataPoints.get(j)[3] + dataPoints.get(j + 1)[3]) * (dataPoints.get(j + 1)[1] - dataPoints.get(j)[1]) * 0.5;

                    if (j == peakTopId - 1) {
                        leftPeakArea = realAreaAboveZero;
                    } else if (j == dataPoints.size() - 2) {
                        rightPeakArea = realAreaAboveZero - leftPeakArea;
                    }
                }

                realAreaAboveBaseline = realAreaAboveZero - (dataPoints.get(0)[3] + dataPoints.get(dataPoints.size() - 1)[3]) * (dataPoints.get(dataPoints.size() - 1)[1] - dataPoints.get(0)[1]) / 2;

                if (dataPoints.get(0)[3] <= dataPoints.get(dataPoints.size() - 1)[3]) {
                    leftPeakArea = leftPeakArea - dataPoints.get(0)[3] * (dataPoints.get(peakTopId)[1] - dataPoints.get(0)[1]);
                    rightPeakArea = rightPeakArea - dataPoints.get(0)[3] * (dataPoints.get(dataPoints.size() - 1)[1] - dataPoints.get(peakTopId)[1]);
                } else {
                    leftPeakArea = leftPeakArea - dataPoints.get(dataPoints.size() - 1)[3] * (dataPoints.get(peakTopId)[1] - dataPoints.get(0)[1]);
                    rightPeakArea = rightPeakArea - dataPoints.get(dataPoints.size() - 1)[3] * (dataPoints.get(dataPoints.size() - 1)[1] - dataPoints.get(peakTopId)[1]);
                }

                if (gaussianArea >= leftPeakArea) {
                    gaussianSimilarityLeftValue = leftPeakArea / gaussianArea;
                } else {
                    gaussianSimilarityLeftValue = gaussianArea / leftPeakArea;
                }

                if (gaussianArea >= rightPeakArea) {
                    gaussianSimilarityRightValue = rightPeakArea / gaussianArea;
                } else {
                    gaussianSimilarityRightValue = gaussianArea / rightPeakArea;
                }

                gaussinaSimilarityValue = (gaussianSimilarityLeftValue + gaussianSimilarityRightValue) / 2;
                idealSlopeValue = (idealSlopeValue - nonIdealSlopeValue) / idealSlopeValue;

                if (idealSlopeValue < 0)
                    idealSlopeValue = 0;

                peakPureValue = (basePeakValue + symmetryValue + gaussinaSimilarityValue) / 3;

                if (peakPureValue > 1) peakPureValue = 1;
                if (peakPureValue < 0) peakPureValue = 0;


                // 7. Set peak information
                PeakDetectionResult result = new PeakDetectionResult(
                    peakID,
                    -1,
                    -1,
                    realAreaAboveBaseline * 60,
                    realAreaAboveZero * 60,
                    basePeakValue,
                    gaussinaSimilarityValue,
                    idealSlopeValue,
                    (float) dataPoints.get(0)[3],
                    (float) dataPoints.get(peakTopId)[3],
                    (float) dataPoints.get(dataPoints.size() - 1)[3],
                    peakPureValue,
                    dataPoints.get(0)[1],
                    dataPoints.get(peakTopId)[1],
                    dataPoints.get(dataPoints.size() - 1)[1],
                    (int) dataPoints.get(0)[0],
                    (int) dataPoints.get(peakTopId)[0],
                    (int) dataPoints.get(dataPoints.size() - 1)[0],
                    (leftShapenessValue + rightShapenessValue) / 2,
                    symmetryValue
                );

                peakDetectionResults.add(result);
                peakID++;
            }
        }

        // Finalize
        peakDetectionResults = peakDetectionResults.stream()
                .sorted(Comparator.comparing(PeakDetectionResult::intensityAtPeakTop))
                .collect(Collectors.toList());
        double maxIntensity = peakDetectionResults.get(peakDetectionResults.size() - 1).intensityAtPeakTop;

        for (int i = 0; i < peakDetectionResults.size(); i++) {
            peakDetectionResults.get(i).amplitudeScoreValue = peakDetectionResults.get(i).intensityAtPeakTop / maxIntensity;
            peakDetectionResults.get(i).amplitudeOrderValue = i + 1;
        }

        return peakDetectionResults.stream()
                .sorted(Comparator.comparing(PeakDetectionResult::peakID))
                .collect(Collectors.toList());
    }
}
