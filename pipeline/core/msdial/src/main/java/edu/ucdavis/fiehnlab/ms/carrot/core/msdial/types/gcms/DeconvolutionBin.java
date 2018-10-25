package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.gcms;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sajjan on 04/16/2018.
 */
public class DeconvolutionBin {

	public double totalSharpnessValue = 0;
	public int rawScanNumber;
	public double retentionTime;
	public List<PeakSpot> peakSpots = new ArrayList<>();
}
