package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.deconvolution.MsDialPeak;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diego on 8/17/2016.
 */
public abstract class DeconvolutionResult {
	public int scanNumber = -1;
	public List<Double> modelMasses = new ArrayList<>();
	public List<MsDialPeak> baseChromatogram = new ArrayList<>();
}
