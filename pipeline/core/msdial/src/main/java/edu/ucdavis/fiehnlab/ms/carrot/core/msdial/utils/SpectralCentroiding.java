package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MSDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    public static List<Ion> getCentroidSpectrum(List<Feature> spectrumList, MSDataType dataType, int msScanPoint, double massBin, boolean peakDetectionBasedCentroid) {

        if (msScanPoint < 0) {
            return new ArrayList<>();
        }

//        if(massBin <= 0.01) {
//            msScanPoint--;
//        }

		System.out.println("Spectrum count\tscan point\tmass bin\tdata type\n" + spectrumList.size()+"\t"+ msScanPoint +"\t"+ massBin +"\t"+ dataType);

        List<Ion> spectrum = TypeConverter.getJavaIonList(spectrumList.get(msScanPoint));

        if (spectrum.isEmpty() || dataType == MSDataType.CENTROID) {
            return spectrum;
        }

        List<Ion> centroidedSpectra = new ArrayList<>(centroid(spectrum, massBin, peakDetectionBasedCentroid));

        // Return the original spectrum if the centroided version is empty
        return centroidedSpectra.isEmpty() ? spectrum : centroidedSpectra;
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
			// Peak detection based centroid
			List<Ion> centroidedSpectrum = new ArrayList<>();

			// Differential calculation
			ArrayList<Double> firstDiffPeakList = new ArrayList<>();
			ArrayList<Double> secondDiffPeakList = new ArrayList<>();

			double firstDiff, secondDiff;
			double maxFirstDiff = Double.MIN_VALUE;
			double maxSecondDiff = Double.MIN_VALUE;
			double maxAmplitudeDiff = Double.MIN_VALUE;

			int halfDatapoint = (firstDiffCoeff.length / 2);
			int peakID = 0;

			for (int i = 0; i < spectrum.size(); i++) {
				if (i < halfDatapoint) {
					firstDiffPeakList.add(0.0);
					secondDiffPeakList.add(0.0);
					continue;
				}

				if (i >= spectrum.size() - halfDatapoint) {
					firstDiffPeakList.add(0.0);
					secondDiffPeakList.add(0.0);
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
			ArrayList<Double> amplitudeNoiseCandidate = new ArrayList<>();
			ArrayList<Double> slopeNoiseCandidate = new ArrayList<>();
			ArrayList<Double> peaktopNoiseCandidate = new ArrayList<>();

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
						if (i + 1 == spectrum.size() - 1) {
							break;
						}

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
					centroidedSpectrum.add(new Ion(datapoints.get(peakTopId)[0], datapoints.get(peakTopId)[0]));
					peakID++;
				}
			}

			if (centroidedSpectrum.isEmpty()) {
				return new ArrayList<>();
			}


			List<Ion> filteredCentroidedSpectra = new ArrayList<>();

			centroidedSpectrum.sort(Comparator.comparing(Ion::intensity).reversed());

            double maxIntensity = centroidedSpectrum.get(0).intensity();

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
			int counter = 0;
			double sumXY = 0;
			double sumY = 0;

			while (focusedMz <= maxMz) {
				sumXY = 0.0;
				sumY = 0;
				counter = 0;
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

//	/**
//	 * This is the spectrum centroid method for MS-FINDER program.
//	 * In contrast to MS-DIAL program, this method require one argument, list of 'Peak' class (please see Peak.cs of Common assembly).
//	 * Although I prepared two type centroidings for MS-DIAL paper, now I recommend to use 'peakdetectionbasedcentroid' method,
//	 * that is, we do not have to set bin (second arg) parameter.
//	 * @param spectraCollection The first arg is the spectrum arrary collection. Each array, i.e. double[], should be [0]m/z and [1]intensity.
//	 * @param threshold The second arg should be not required as long as peakdetectionBasedCentroid is true.
//	 * @return This method will return array ([0]m/z, [1]intensity) list as the observablecollection.
//	 */
//	public static List<Ion> centroid(List<Ion> spectraCollection, double bin, double threshold) {
//		List<Ion> centroidedSpectra = new ArrayList<>();
//		Ion detectedPeakInformation;
//
//		//Differential calculation
//		List<Double> firstDiffPeaklist = new ArrayList<>();
//		List<Double> secondDiffPeaklist = new ArrayList<>();
//		double[] firstDiffCoeff = new double[] { -0.2, -0.1, 0, 0.1, 0.2 };
//		double[] secondDiffCoeff = new double[] { 0.14285714, -0.07142857, -0.1428571, -0.07142857, 0.14285714 };
//		double firstDiff;
//		double secondDiff;
//		double maxFirstDiff = Double.MIN_VALUE;
//		double maxSecondDiff = Double.MIN_VALUE;
//		double maxAmplitudeDiff = Double.MIN_VALUE;
//		int halfDatapoint = (firstDiffCoeff.length / 2);
//		int peakID = 0;
//
//		for (int i = 0; i < spectraCollection.size(); i++) {
//			if (i < halfDatapoint) {
//				firstDiffPeaklist.add(0.0);
//				secondDiffPeaklist.add(0.0);
//				continue;
//			}
//
//			if (i >= spectraCollection.size() - halfDatapoint) {
//				firstDiffPeaklist.add(0.0);
//				secondDiffPeaklist.add(0.0);
//				continue;
//			}
//
//			firstDiff = secondDiff = 0;
//			for (int j = 0; j < firstDiffCoeff.length; j++) {
//				firstDiff += firstDiffCoeff[j] * spectraCollection.get(i + j - halfDatapoint).intensity;
//				secondDiff += secondDiffCoeff[j] * spectraCollection.get(i + j - halfDatapoint).intensity;
//			}
//			firstDiffPeaklist.add(firstDiff);
//			secondDiffPeaklist.add(secondDiff);
//
//			if (Math.abs(firstDiff) > maxFirstDiff) maxFirstDiff = Math.abs(firstDiff);
//			if (secondDiff < 0 && maxSecondDiff < -1 * secondDiff) maxSecondDiff = -1 * secondDiff;
//			if (Math.abs(spectraCollection.get(i).intensity - spectraCollection.get(i - 1).intensity) > maxAmplitudeDiff) maxAmplitudeDiff = Math.abs(spectraCollection.get(i).intensity - spectraCollection.get(i - 1).intensity);
//		}
//
//		//Noise estimate
//		List<Double> amplitudeNoiseCandidate = new ArrayList<>();
//		List<Double> slopeNoiseCandidate = new ArrayList<>();
//		List<Double> peaktopNoiseCandidate = new ArrayList<>();
//		double amplitudeNoiseThresh = maxAmplitudeDiff * 0.05, slopeNoiseThresh = maxFirstDiff * 0.05, peaktopNoiseThresh = maxSecondDiff * 0.05;
//		double amplitudeNoise, slopeNoise, peaktopNoise;
//		for (int i = 2; i < spectraCollection.size() - 2; i++)
//		{
//			if (Math.abs(spectraCollection.get(i + 1).intensity - spectraCollection.get(i).intensity) < amplitudeNoiseThresh && Math.abs(spectraCollection.get(i + 1).intensity - spectraCollection.get(i).intensity) > 0)
//				amplitudeNoiseCandidate.add(Math.abs(spectraCollection.get(i + 1).intensity - spectraCollection.get(i).intensity));
//			if (Math.abs(firstDiffPeaklist.get(i)) < slopeNoiseThresh && Math.abs(firstDiffPeaklist.get(i)) > 0)
//				slopeNoiseCandidate.add(Math.abs(firstDiffPeaklist.get(i)));
//			if (secondDiffPeaklist.get(i) < 0 && Math.abs(secondDiffPeaklist.get(i)) < peaktopNoiseThresh && Math.abs(secondDiffPeaklist.get(i)) > 0)
//				peaktopNoiseCandidate.add(Math.abs(secondDiffPeaklist.get(i)));
//		}
//		if (amplitudeNoiseCandidate.size() == 0) {
//			amplitudeNoise = 0.0001;
//		} else {
//			amplitudeNoise = BasicMathematics.Median(amplitudeNoiseCandidate);
//		}
//		if (slopeNoiseCandidate.size() == 0) {
//			slopeNoise = 0.0001;
//		} else {
//			slopeNoise = BasicMathematics.Median(slopeNoiseCandidate);
//		}
//		if (peaktopNoiseCandidate.size() == 0) {
//			peaktopNoise = 0.0001;
//		} else {
//			peaktopNoise = BasicMathematics.Median(peaktopNoiseCandidate);
//		}
//
//		//Search peaks
//		List<double[]> datapoints;
//		double peakTopIntensity;
//		int peaktopCheckPoint, peakTopId = -1;
//		boolean peaktopCheck = false;
//
//		double minimumDatapointCriteria = 1;
//		double slopeNoiseFoldCriteria = 1;
//		double peaktopNoiseFoldCriteria = 1;
//		double minimumAmplitudeCriteria = 1;
//
//		for (int i = 0; i < spectraCollection.size(); i++) {
//			if (i >= spectraCollection.size() - 1 - minimumDatapointCriteria) break;
//			//1. Left edge criteria
//			if (firstDiffPeaklist.get(i) >= 0 && firstDiffPeaklist.get(i + 1) > slopeNoise * slopeNoiseFoldCriteria) {
//				datapoints = new ArrayList<>();
//				datapoints.add(new double[] { spectraCollection.get(i).mass, spectraCollection.get(i).intensity, firstDiffPeaklist.get(i), secondDiffPeaklist.get(i) });
//
//				//2. Right edge criteria
//				peaktopCheck = false;
//				peaktopCheckPoint = i;
//				while (true) {
//					if (i + 1 == spectraCollection.size() - 1) break;
//
//					i++;
//					datapoints.add(new double[] { spectraCollection.get(i).mass, spectraCollection.get(i).intensity, firstDiffPeaklist.get(i), secondDiffPeaklist.get(i) });
//					if (!peaktopCheck && firstDiffPeaklist.get(i - 1) > 0 && firstDiffPeaklist.get(i) < 0 && secondDiffPeaklist.get(i) < -1 * peaktopNoise * peaktopNoiseFoldCriteria) {
//						peaktopCheck = true; peaktopCheckPoint = i;
//					}
//					if (peaktopCheck && peaktopCheckPoint + 2 <= i - 1 && firstDiffPeaklist.get(i) > -1 * slopeNoise * slopeNoiseFoldCriteria)
//						break;
//
//					if (Math.abs(datapoints.get(0)[0] - datapoints.get(datapoints.size() - 1)[0]) > 1)
//						break;
//				}
//
//				//3. Check minimum datapoint criteria
//				if (datapoints.size() < minimumDatapointCriteria) continue;
//
//				//4. Check peak half height at half width
//				peakTopIntensity = Double.MIN_VALUE;
//				peakTopId = -1;
//				for (int j = 0; j < datapoints.size(); j++) {
//					if (peakTopIntensity < datapoints.get(j)[1]) {
//						peakTopIntensity = datapoints.get(j)[1];
//						peakTopId = j;
//					}
//				}
//				if (datapoints.get(peakTopId)[1] < minimumAmplitudeCriteria) continue;
//
//				//5. Set peakInforamtion
//				detectedPeakInformation = new Ion(datapoints.get(peakTopId)[0], datapoints.get(peakTopId)[1]);
//				centroidedSpectra.add(detectedPeakInformation);
//				peakID++;
//			}
//		}
//
//		if (centroidedSpectra.size() == 0) return null;
//
//		List<Ion> filteredCentroidedSpectra = new ArrayList<>();
//
//		double maxIntensity = centroidedSpectra.stream().max(Comparator.comparing(Ion::intensity).reversed()).get().intensity;
//
//		for (int i = 0; i < centroidedSpectra.size(); i++)
//			if (centroidedSpectra.get(i).intensity() > maxIntensity) { filteredCentroidedSpectra.add(centroidedSpectra.get(i)); } else break;
//		filteredCentroidedSpectra.sort(Comparator.comparing(Ion::mass));
//
//		return filteredCentroidedSpectra;
//	}


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
