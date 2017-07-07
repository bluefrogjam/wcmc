package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diego on 8/5/2016.
 */
public class GCMSDecBin {
	public double totalSharpnessValue = 0.0;
	public int rawScanNumber = -1;
	public double retentionTime = 0;
	public List<PeakSpot> peakSpots = new ArrayList<>();

	@Override
	public String toString() {
		return new StringBuilder().append("Scan: ").append(rawScanNumber)
				.append(", RT: ").append(retentionTime)
				.append(", Sharpness: ").append(totalSharpnessValue)
				.append(", spots [").append(peakSpots.size()).append("]:\n")
				.append(peakSpots.stream().map(sp -> sp.peakSpotID + " : " + sp.quality)).toString();
	}
}
