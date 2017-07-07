package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.utils;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.Ion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by diego on 8/11/2016.
 */
public class DataAccessUtility {
	private static Logger logger = LoggerFactory.getLogger(DataAccessUtility.class);

	/**
	 * finds the start of the chromatografic peak for the selected targetMass within the optional ms1 tolerance window (in no more than 10 steps)
	 * [NOTE: for more accurate results the counter can be raised up to 15 (no difference after that)]
	 *
	 * @param focusedMass
	 * @param massSpectrum
	 * @param ms1Tolerance (optional)
	 * @return
	 */
	public static int getStartIndexForTargetMass(double focusedMass, List<Ion> massSpectrum, double ms1Tolerance) {
		if (massSpectrum.isEmpty()) {
			return 0;
		}

		double targetMass = focusedMass - ms1Tolerance;
		int startIndex = 0, endIndex = massSpectrum.size() - 1;
		int counter = 0;

		while (counter < 10) {
			if (massSpectrum.get(startIndex).mass <= targetMass && targetMass < massSpectrum.get((startIndex + endIndex) / 2).mass) {
				endIndex = (startIndex + endIndex) / 2;
			} else if (massSpectrum.get((startIndex + endIndex) / 2).mass <= targetMass && targetMass < massSpectrum.get(endIndex).mass) {
				startIndex = (startIndex + endIndex) / 2;
			}
			counter++;
		}

		return startIndex;
	}
}
