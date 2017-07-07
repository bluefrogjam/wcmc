package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.utils;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.MSDataType;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.RawSpectrum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diego on 7/21/2016.
 */
public class LCMSDataAccessUtility extends DataAccessUtility {
	public static List<Ion> getCentroidedMassSpectra(List<RawSpectrum> rawData, MSDataType dataType, int ms2LevelDataPoints, double centroidMs2Tolerance, boolean peakDetectionBasedCentroid) {

		List<Ion> spectra = new ArrayList<>();
		List<Ion> centroidedSpectra = new ArrayList<>();
		RawSpectrum spectrum;
		List<Ion> massSpectra;

		if (ms2LevelDataPoints < 0) {
			return new ArrayList<>();
		}

		spectrum = rawData.get(ms2LevelDataPoints);
		massSpectra = spectrum.spect;

		if (dataType == MSDataType.CENTROID) {
			return massSpectra;
		}

		if (spectra.isEmpty()) {
			return new ArrayList<>();
		}

		centroidedSpectra = SpectralCentroiding.centroid(spectra, centroidMs2Tolerance, peakDetectionBasedCentroid);

		if (centroidedSpectra != null && centroidedSpectra.size() != 0) {
			return centroidedSpectra;
		} else {
			return spectra;
		}
	}

	public static List<double[]> getMs2Peaklist(List<RawSpectrum> rawData, double precursorMz, double productMz, double startRt, double endRt, char ionMode, double massTolerance, double centroidedMs1Tolerance) {
		List<double[]> peaklist = new ArrayList<>();
		RawSpectrum spectrum;
		List<Ion> massSpectra;

		double sum;
		int startMsIndex;
		int startRtIndex = getRtStartIndex(startRt, rawData);

		for (int i = startRtIndex; i < rawData.size(); i++) {
			if (rawData.get(i).msLevel <= 1) continue;
			if (rawData.get(i).polarity != ionMode) continue;
			if (rawData.get(i).rtMin < startRt) continue;
			if (rawData.get(i).rtMin > endRt) break;

			if (precursorMz - centroidedMs1Tolerance <= rawData.get(i).precursorMz && rawData.get(i).precursorMz <= precursorMz + centroidedMs1Tolerance) {
				spectrum = rawData.get(i);
				massSpectra = spectrum.spect;
				sum = 0;

				startMsIndex = getMs2StartIndex(productMz - massTolerance, massSpectra);

				for (int j = startMsIndex; j < massSpectra.size(); j++) {
					if (massSpectra.get(j).mass() < productMz - massTolerance) {
						continue; // do nothing
					} else if (productMz - massTolerance <= massSpectra.get(j).mass() && massSpectra.get(j).mass() <= productMz + massTolerance) {
						sum += massSpectra.get(j).intensity();
					} else {
						break;
					}
				}

				peaklist.add(new double[]{i, spectrum.rtMin, 0, sum});
			}
		}

		return peaklist;
	}

	private static int getRtStartIndex(double targetRt, List<RawSpectrum> spectra) {
		int startIndex = 0, endIndex = spectra.size() - 1;
		int counter = 0;

		while (counter < 10) {
			if (spectra.get(startIndex).rtMin <= targetRt &&
					targetRt < spectra.get((startIndex + endIndex) / 2).rtMin) {
				endIndex = (startIndex + endIndex) / 2;
			} else if (spectra.get((startIndex + endIndex) / 2).rtMin <= targetRt &&
					targetRt < spectra.get(endIndex).rtMin) {
				startIndex = (startIndex + endIndex) / 2;
			}
			counter++;
		}
		return startIndex;
	}

	private static int getMs2StartIndex(double targetMass, java.util.List<Ion> massSpectra) {
		return getStartIndexForTargetMass(targetMass, massSpectra, 0);
	}
}
