package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.math.BasicMathematics;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MSDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SpectralCentroiding {

    private static final double[] firstDiffCoeff = new double[] { -0.2, -0.1, 0, 0.1, 0.2 };
    private static final double[] secondDiffCoeff = new double[] { 0.14285714, -0.07142857, -0.1428571, -0.07142857, 0.14285714 };


    /**
     *
     * @param spectrumList
     * @param dataType
     * @param msScanPoint
     * @param massBin
     * @param peakDetectionBasedCentroid
     * @return
     */
    public static List<Ion> getLCMSCentroidedSpectrum(List<? extends Feature> spectrumList, MSDataType dataType, int msScanPoint, double massBin, boolean peakDetectionBasedCentroid) {

        if (msScanPoint < 0) {
            return new ArrayList<>();
        }

        List<Ion> spectrum = TypeConverter.getJavaIonList(spectrumList.get(msScanPoint));

        if (spectrum.isEmpty() || dataType == MSDataType.CENTROID) {
            return spectrum;
        }

        List<Ion> centroidedSpectrum = new ArrayList<>(centroid(spectrum, massBin, peakDetectionBasedCentroid));

        // Return the original spectrum if the centroided version is empty
        return centroidedSpectrum.isEmpty() ? spectrum : centroidedSpectrum;
    }


    /**
     *
     * @param spectrumList
     * @param dataType
     * @param msScanPoint
     * @param massBin
     * @param amplitudeThreshold
     * @param mzStart
     * @param mzEnd
     * @return
     */
    public static List<Ion> getGCMSCentroidedSpectrum(List<? extends Feature> spectrumList, MSDataType dataType, int msScanPoint,
                                                      double massBin, double amplitudeThreshold, double mzStart, double mzEnd) {

        if (msScanPoint < 0) {
            return new ArrayList<>();
        }

        List<Ion> spectrum = TypeConverter.getJavaIonList(spectrumList.get(msScanPoint))
                .stream()
                .filter(ion -> ion.mass() >= mzStart && ion.mass() <= mzEnd)
                .collect(Collectors.toList());

        if (spectrum.isEmpty()) {
            return spectrum;
        }

        if (dataType == MSDataType.CENTROID) {
            return spectrum.stream()
                    .filter(ion -> ion.intensity() > amplitudeThreshold)
                    .collect(Collectors.toList());
        }

        // Return a centroided, and intensity filtered spectrum
        List<Ion> centroidedSpectrum = peakDetectionBasedCentroiding(spectrum, massBin);

        return centroidedSpectrum.stream()
                .filter(ion -> ion.intensity() > amplitudeThreshold)
                .collect(Collectors.toList());
    }


	/**
     * This is the spectrum centroid method for MS-DIAL program.
     * This method will return array ([0]m/z, [1]intensity) list as the observablecollection.
     * The first arg is the spectrum arrary collection. Each array, i.e. double[], should be [0]m/z and [1]intensity.
     * The second arg should be not required as long as peakdetectionBasedCentroid is true.
     * Although I prepared two type centroidings for MS-DIAL paper, now I recommend to use 'peakdetectionbasedcentroid' method,
     * that is, we do not have to set bin (second arg) parameter.
     *
	 * @param spectrum The first arg is the spectrum arrary collection. Each array, i.e. double[], should be [0]m/z and [1]intensity.
	 * @param massBin The second arg should be not required as long as peakdetectionBasedCentroid is true.
	 * @param peakDetectionBasedCentroid
	 * @return This method will return array ([0]m/z, [1]intensity) list as the observablecollection.
	 */

	private static List<Ion> centroid(List<Ion> spectrum, double massBin, boolean peakDetectionBasedCentroid) {

		if (peakDetectionBasedCentroid) {
            List<Ion> centroidedSpectrum = peakDetectionBasedCentroiding(spectrum, massBin);
			List<Ion> filteredCentroidedSpectra = new ArrayList<>();

			centroidedSpectrum.sort(Comparator.comparing(Ion::intensity).reversed());

            double maxIntensity = 0;
            if (centroidedSpectrum.size() > 0) {
                maxIntensity = centroidedSpectrum.get(0).intensity();
            }

            for (Ion centSpec : centroidedSpectrum) {
				if (centSpec.intensity() > maxIntensity * 0.000001) {
					filteredCentroidedSpectra.add(centSpec);
				} else {
					break;
				}
			}

			filteredCentroidedSpectra.sort(Comparator.comparing(Ion::mass));
			return filteredCentroidedSpectra;
		} else {
			// Sweep bin based centroid
			massBin = 0.1;
			List<Ion> centroidedSpectra = new ArrayList<>();

			double minMz = spectrum.get(0).mass();
			double maxMz = spectrum.get(spectrum.size() - 1).mass();

			List<Ion> spectraList = new ArrayList<>(spectrum);
			double minInt = Collections.min(spectraList, Comparator.comparing(Ion::intensity)).intensity();
			spectraList.sort(Comparator.comparing(Ion::mass));

			double focusedMz = minMz;
			int startIndex, remaindIndex = 0;

			while (focusedMz <= maxMz) {
				double sumXY = 0.0;
				float sumY = 0;
				int counter = 0;
				startIndex = getStartIndex(focusedMz, massBin, spectraList);

				for (int i = startIndex; i < spectraList.size(); i++) {
					if (spectraList.get(i).mass() < focusedMz - massBin) {
						continue;
					} else if (spectraList.get(i).mass() > focusedMz + massBin) {
						remaindIndex = i;
						break;
					} else {
						sumXY += spectraList.get(i).mass() * spectraList.get(i).intensity();
						sumY += spectraList.get(i).intensity();
						counter++;
					}
				}

				if (sumY == 0) {
					focusedMz = Math.max(focusedMz + massBin, spectraList.get(remaindIndex).mass() - massBin);
					continue;
				}

				if (counter == 1 && sumY < minInt + 5) {
					focusedMz = Math.max(focusedMz + massBin, spectraList.get(remaindIndex).mass() - massBin);
					continue;
				} else if (counter == 2 && sumY < minInt * 2 + 10) {
					focusedMz = Math.max(focusedMz + massBin, spectraList.get(remaindIndex).mass() - massBin);
					continue;
				} else if (counter == 3 && sumY < minInt * 3 + 15) {
					focusedMz = Math.max(focusedMz + massBin, spectraList.get(remaindIndex).mass() - massBin);
					continue;
				}

				if (centroidedSpectra.size() != 0) {
					if (Math.abs(centroidedSpectra.get(centroidedSpectra.size() - 1).mass() - sumXY / sumY) < massBin) {
						if (centroidedSpectra.get(centroidedSpectra.size() - 1).intensity() < sumY) {
						    centroidedSpectra.remove(centroidedSpectra.size() - 1);
                            centroidedSpectra.add(new Ion(sumXY / sumY, sumY));
						}
					} else {
						centroidedSpectra.add(new Ion(sumXY / sumY, sumY));
					}
				} else {
					centroidedSpectra.add(new Ion(sumXY / sumY, sumY));
				}

				focusedMz = Math.max(focusedMz + massBin, spectraList.get(remaindIndex).mass() - massBin);
			}


			List<Ion> filteredCentroidedSpectra = new ArrayList<>();

			if (!centroidedSpectra.isEmpty()) {
				double maxIntensity = Collections.max(centroidedSpectra, Comparator.comparing(Ion::intensity).reversed()).intensity();

				for (Ion ion : centroidedSpectra) {
					if (ion.intensity() > maxIntensity * 0.001) {
						filteredCentroidedSpectra.add(ion);
					} else {
						break;
					}
				}

                filteredCentroidedSpectra.sort(Comparator.comparing(Ion::mass));
			}

            return filteredCentroidedSpectra;
		}
	}


	private static List<Ion> peakDetectionBasedCentroiding(List<Ion> spectrum, double massBin) {
        // Peak detection based centroid
        List<Ion> centroidedSpectrum = new ArrayList<>();

        // Differential calculation
        ArrayList<Float> firstDiffPeakList = new ArrayList<>();
        ArrayList<Float> secondDiffPeakList = new ArrayList<>();

        float firstDiff, secondDiff;
        float maxFirstDiff = Float.MIN_VALUE;
        float maxSecondDiff = Float.MIN_VALUE;
        float maxAmplitudeDiff = Float.MIN_VALUE;

        int halfDatapoint = (firstDiffCoeff.length / 2);
        int peakID = 0;

        for (int i = 0; i < spectrum.size(); i++) {
            if (i < halfDatapoint) {
                firstDiffPeakList.add(0.0f);
                secondDiffPeakList.add(0.0f);
                continue;
            }

            if (i >= spectrum.size() - halfDatapoint) {
                firstDiffPeakList.add(0.0f);
                secondDiffPeakList.add(0.0f);
                continue;
            }

            firstDiff = secondDiff = 0;

            for (int j = 0; j < firstDiffCoeff.length; j++) {
                firstDiff += firstDiffCoeff[j] * spectrum.get(i + j - halfDatapoint).intensity();
                secondDiff += secondDiffCoeff[j] * spectrum.get(i + j - halfDatapoint).intensity();
            }

            firstDiffPeakList.add(firstDiff);
            secondDiffPeakList.add(secondDiff);

            if (Math.abs(firstDiff) > maxFirstDiff) {
                maxFirstDiff = Math.abs(firstDiff);
            }

            if (secondDiff < 0 && maxSecondDiff < -1 * secondDiff) {
                maxSecondDiff = -1 * secondDiff;
            }

            if (Math.abs(spectrum.get(i).intensity() - spectrum.get(i - 1).intensity()) > maxAmplitudeDiff) {
                maxAmplitudeDiff = Math.abs(spectrum.get(i).intensity() - spectrum.get(i - 1).intensity());
            }
        }

        // Noise estimate
        ArrayList<Float> amplitudeNoiseCandidate = new ArrayList<>();
        ArrayList<Float> slopeNoiseCandidate = new ArrayList<>();
        ArrayList<Float> peaktopNoiseCandidate = new ArrayList<>();

        double amplitudeNoiseThresh = maxAmplitudeDiff * 0.05;
        double slopeNoiseThresh = maxFirstDiff * 0.05;
        double peaktopNoiseThresh = maxSecondDiff * 0.05;

        for (int i = 2; i < spectrum.size() - 2; i++) {
            if (Math.abs(spectrum.get(i + 1).intensity() - spectrum.get(i).intensity()) < amplitudeNoiseThresh &&
                    Math.abs(spectrum.get(i + 1).intensity() - spectrum.get(i).intensity()) > 0) {
                amplitudeNoiseCandidate.add(Math.abs(spectrum.get(i + 1).intensity() - spectrum.get(i).intensity()));
            }

            if (Math.abs(firstDiffPeakList.get(i)) < slopeNoiseThresh && Math.abs(firstDiffPeakList.get(i)) > 0) {
                slopeNoiseCandidate.add(Math.abs(firstDiffPeakList.get(i)));
            }

            if (secondDiffPeakList.get(i) < 0 &&
                    Math.abs(secondDiffPeakList.get(i)) < peaktopNoiseThresh &&
                    Math.abs(secondDiffPeakList.get(i)) > 0) {
                peaktopNoiseCandidate.add(Math.abs(secondDiffPeakList.get(i)));
            }
        }

        double amplitudeNoise = amplitudeNoiseCandidate.isEmpty() ? 0.0001 : BasicMathematics.brokenMedian(amplitudeNoiseCandidate);
        double slopeNoise = slopeNoiseCandidate.isEmpty() ? 0.0001 : BasicMathematics.brokenMedian(slopeNoiseCandidate);
        double peaktopNoise = peaktopNoiseCandidate.isEmpty() ? 0.0001 : BasicMathematics.brokenMedian(peaktopNoiseCandidate);


        // Search peaks
        ArrayList<double[]> datapoints;     //array of: [0] mz, [1] intensity, [2] first differential, [3] second differential
        double peakTopIntensity;
        int peaktopCheckPoint, peakTopId = -1;
        boolean peaktopCheck = false;

        double minimumDatapointCriteria = 1;
        double slopeNoiseFoldCriteria = 1;
        double peaktopNoiseFoldCriteria = 1;
        double minimumAmplitudeCriteria = 1;

        for (int i = 0; i < spectrum.size(); i++) {
            if (i >= spectrum.size() - 1 - minimumDatapointCriteria) {
                break;
            }

            // 1. Left edge criteria
            if (firstDiffPeakList.get(i) >= 0 && firstDiffPeakList.get(i + 1) > slopeNoise * slopeNoiseFoldCriteria) {
                datapoints = new ArrayList<>();
                datapoints.add(new double[] { spectrum.get(i).mass(), spectrum.get(i).intensity(), firstDiffPeakList.get(i), secondDiffPeakList.get(i) });

                // 2. Right edge criteria
                peaktopCheck = false;
                peaktopCheckPoint = i;

                while (true) {
                    if (i + 1 == spectrum.size() - 1)
                        break;

                    i++;
                    datapoints.add(new double[] { spectrum.get(i).mass(), spectrum.get(i).intensity(), firstDiffPeakList.get(i), secondDiffPeakList.get(i) });

                    if (!peaktopCheck && firstDiffPeakList.get(i - 1) > 0 && firstDiffPeakList.get(i) < 0 &&
                            secondDiffPeakList.get(i) < -1 * peaktopNoise * peaktopNoiseFoldCriteria) {
                        peaktopCheck = true;
                        peaktopCheckPoint = i;
                    }

                    if (peaktopCheck && peaktopCheckPoint + 2 <= i - 1 &&
                            firstDiffPeakList.get(i) > -1 * slopeNoise * slopeNoiseFoldCriteria) {
                        break;
                    }

                    if (Math.abs(datapoints.get(0)[0] - datapoints.get(datapoints.size() - 1)[0]) > 1) {
                        break;
                    }
                }

                // 3. Check minimum datapoint criteria
                if (datapoints.size() < minimumDatapointCriteria) {
                    continue;
                }

                // 4. Check peak half height at half width
                peakTopIntensity = Double.MIN_VALUE;
                peakTopId = -1;

                for (int j = 0; j < datapoints.size(); j++) {
                    if (peakTopIntensity < datapoints.get(j)[1]) {
                        peakTopIntensity = datapoints.get(j)[1];
                        peakTopId = j;
                    }
                }

                if (datapoints.get(peakTopId)[1] < minimumAmplitudeCriteria) {
                    continue;
                }

                // 5. Set peakInformation
                centroidedSpectrum.add(new Ion(datapoints.get(peakTopId)[0], (float)datapoints.get(peakTopId)[1]));
                peakID++;
            }
        }

        return centroidedSpectrum.isEmpty() ? new ArrayList<>() : centroidedSpectrum;
    }


	private static int getStartIndex(double focusedMass, double ms1Tolerance, List<Ion> spectra) {
		if (spectra.isEmpty())
		    return 0;

		double targetMass = focusedMass - ms1Tolerance;
		int startIndex = 0, endIndex = spectra.size() - 1;
		int counter = 0;

		while (counter < 10) {
			if (spectra.get(startIndex).mass() <= targetMass && targetMass < spectra.get((startIndex + endIndex) / 2).mass()) {
				endIndex = (startIndex + endIndex) / 2;
			} else if (spectra.get((startIndex + endIndex) / 2).mass() <= targetMass && targetMass < spectra.get(endIndex).mass()) {
				startIndex = (startIndex + endIndex) / 2;
			}
			counter++;
		}

		return startIndex;
	}
}
