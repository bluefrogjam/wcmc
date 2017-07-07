package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.deconvolution.MsDialPeak;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diego on 8/5/2016.
 */
public class ModelChromatogram {
	public List<MsDialPeak> peaks = new ArrayList<>();

	public int rawScanOfPeakTop = -1;
	public int chromScanOfPeakTop = -1;
	public int chromScanOfPeakLeft = -1;
	public int chromScanOfPeakRight = -1;

	public List<Double> modelMasses = new ArrayList<>();

	public double idealSlopeValue = 0.0;
	public double sharpnessValue = 0.0;
	public double maximumPeakTopValue = 0.0;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(), mm = new StringBuilder(), pk = new StringBuilder();
		modelMasses.forEach(it -> mm.append(it).append(","));
		mm.deleteCharAt(mm.length() - 1);
		peaks.forEach(p -> {
			pk.append("]scan: ").append(p.scanNum)
					.append(" mz: ").append(p.rtMin)
					.append(" int: ").append(p.mass)
					.append(" rt: ").append(p.intensity).append("],");
		});

		if (pk.length() > 0) {
			pk.deleteCharAt(pk.length() - 1);
		}

		sb.append("scan at top:").append(rawScanOfPeakTop)
				.append(", scan at left:").append(chromScanOfPeakLeft)
				.append(", scan at top:").append(chromScanOfPeakTop)
				.append(", scan at right:").append(chromScanOfPeakRight)
				.append(", ideal slope:").append(idealSlopeValue)
				.append(", sharpness:").append(sharpnessValue)
				.append(", max peaktop:").append(maximumPeakTopValue)
				.append("\n\tmodel mz:[").append(mm.toString())
				.append("]\n\tpeaks:[").append(pk.toString())
				.append("]");
		return sb.toString();
	}

	public ModelChromatogram() {
		peaks = new ArrayList<>();
		modelMasses = new ArrayList<>();
		rawScanOfPeakTop = 0;
		chromScanOfPeakTop = 0;
		chromScanOfPeakLeft = 0;
		chromScanOfPeakRight = 0;
		idealSlopeValue = 0;
		maximumPeakTopValue = 0;
		sharpnessValue = 0;
	}

	public int chromScanOfPeakTop() { return chromScanOfPeakTop; }
}
