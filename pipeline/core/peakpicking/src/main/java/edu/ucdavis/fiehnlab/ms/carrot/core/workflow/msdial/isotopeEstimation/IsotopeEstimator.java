package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.isotopeEstimation;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.MassResolution;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.PeakAreaBean;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.utils.GCMSDataAccessUtility;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.utils.MassDiffDictionary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by diego on 8/26/2016.
 */
public class IsotopeEstimator {
	public static void setIsotopeInformation(List<PeakAreaBean> detectedPeakAreas, double massAccuracy, MassResolution massResolution) {
		detectedPeakAreas.sort(Comparator.comparing(PeakAreaBean::scanNumAtPeakTop).thenComparing(PeakAreaBean::accurateMass));

		int focusedScanNumber;
		int startScanIndex;
		int spectrumMargin = 2;
		double focusedRt;
		double rtMargin = 0.015F;

		for (int i = 0; i < detectedPeakAreas.size(); i++) {
			if (detectedPeakAreas.get(i).isotopeWeightNumber >= 0) continue;

			focusedScanNumber = detectedPeakAreas.get(i).scanNumberAtPeakTop;
			focusedRt = detectedPeakAreas.get(i).rtAtPeakTop();
			startScanIndex = GCMSDataAccessUtility.getMs1StartIndex(focusedScanNumber - spectrumMargin, detectedPeakAreas);

			List<PeakAreaBean> focusedPeakAreaBeanList = new ArrayList<>();

			for (int j = startScanIndex; j < detectedPeakAreas.size(); j++) {
				if (detectedPeakAreas.get(j).scanNumberAtPeakTop < focusedScanNumber - spectrumMargin || detectedPeakAreas.get(j).rtAtPeakTop < focusedRt - rtMargin)
					continue;
				if (detectedPeakAreas.get(j).isotopeWeightNumber >= 0) continue;
				if (detectedPeakAreas.get(j).scanNumberAtPeakTop > focusedScanNumber + spectrumMargin || detectedPeakAreas.get(j).rtAtPeakTop > focusedRt + rtMargin)
					break;
				focusedPeakAreaBeanList.add(detectedPeakAreas.get(j));
			}

			focusedPeakAreaBeanList.sort(Comparator.comparing(PeakAreaBean::accurateMass));
			isotopeCalculation(detectedPeakAreas.get(i), focusedPeakAreaBeanList, massAccuracy, massResolution);
		}

		detectedPeakAreas.sort(Comparator.comparing(PeakAreaBean::peakID));
	}



	private static void isotopeCalculation(PeakAreaBean focusedPeakArea, List<PeakAreaBean> detectedPeakAreas, double massAccuracy, MassResolution massResolution) {
		focusedPeakArea.isotopeWeightNumber = 0;
		focusedPeakArea.isotopeParentPeakID = focusedPeakArea.peakID;

		double reminderIntensity = focusedPeakArea.intensityAtPeakTop;
		double c13_c12Diff = MassDiffDictionary.C13_C12;
		double tolerance = massAccuracy;

		if (massResolution == MassResolution.NOMINAL) {
			tolerance = 0.5F;
		}

		for (PeakAreaBean tmp : detectedPeakAreas) {
			if (focusedPeakArea.accurateMass + 8 < tmp.accurateMass) break;

			for (int j = 1; j < 8; j++) {
				double isotopicMass = focusedPeakArea.accurateMass + (double) j * c13_c12Diff;
				if (isotopicMass - tolerance < tmp.accurateMass && tmp.accurateMass < isotopicMass + tolerance) {
					if (focusedPeakArea.intensityAtPeakTop > tmp.intensityAtPeakTop) {
						if (reminderIntensity < tmp.intensityAtPeakTop) break;
						tmp.isotopeParentPeakID = focusedPeakArea.peakID;
						tmp.isotopeWeightNumber = j;
						reminderIntensity = tmp.intensityAtPeakTop;
					}
				}
			}
		}
	}
}
