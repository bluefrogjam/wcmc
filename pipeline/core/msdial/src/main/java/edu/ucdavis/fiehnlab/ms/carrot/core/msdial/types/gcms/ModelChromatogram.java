package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.gcms;

import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.Peak;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by sajjan on 04/16/2018.
 */
public class ModelChromatogram {

	public List<Peak> peaks = new ArrayList<>();

	public int rawScanOfPeakTop = 0;
	public int chromScanOfPeakTop = 0;
	public int chromScanOfPeakLeft = 0;
	public int chromScanOfPeakRight = 0;

	public List<Double> modelMasses = new ArrayList<>();

	public double idealSlopeValue = 0;
	public double sharpnessValue = 0;
	public double maximumPeakTopValue = 0;

	public int chromScanOfPeakTop() { return chromScanOfPeakTop; }

	public ModelChromatogram() {}

	public ModelChromatogram(double sharpnessValue, double idealSlopeValue) {
	    this.sharpnessValue = sharpnessValue;
	    this.idealSlopeValue = idealSlopeValue;
    }

	@Override
	public String toString() {
		StringJoiner mm = new StringJoiner("; ");
		modelMasses.forEach(it -> mm.add(it.toString()));

		StringJoiner pk = new StringJoiner(";\n\t");
		peaks.forEach(it -> pk.add("scan: "+ it.scanNumber +" mz: "+ it.mz +" int: "+ it.intensity +" rt: "+ it.retentionTime));

		return new StringBuilder()
				.append("scan at top:").append(rawScanOfPeakTop)
				.append(", scan at left:").append(chromScanOfPeakLeft)
				.append(", scan at top:").append(chromScanOfPeakTop)
				.append(", scan at right:").append(chromScanOfPeakRight)
				.append(", ideal slope:").append(idealSlopeValue)
				.append(", sharpness:").append(sharpnessValue)
				.append(", max peaktop:").append(maximumPeakTopValue)
				.append("\n\tmodel mz:[").append(mm.toString())
				.append("]\n\tpeaks:[").append(pk.toString())
				.append("]")
				.toString();
	}
}
