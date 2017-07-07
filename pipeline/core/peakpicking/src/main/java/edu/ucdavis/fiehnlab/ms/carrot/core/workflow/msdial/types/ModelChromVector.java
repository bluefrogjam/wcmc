package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diego on 8/11/2016.
 */
public class ModelChromVector {

	public Ms1DecPattern ms1DecPattern;

	public int targetScanLeftInModelChromVector;
	public int targetScanTopInModelChromVector;
	public int targetScanRightInModelChromVector;

	public List<Integer> chromScanList;
	public List<Integer> rawScanList;
	public List<Double> rtArray;
	public List<Double> targetIntensityArray;

	public List<Double> oneLeftIntensityArray;
	public List<Double> oneRightIntensityArray;

	public List<Double> twoLeftIntensityArray;
	public List<Double> twoRightInetnsityArray;

	public List<Double> modelMasses;
	public double sharpness;

	public ModelChromVector() {
		ms1DecPattern = Ms1DecPattern.C;
		chromScanList = new ArrayList<>();
		rawScanList = new ArrayList<>();
		rtArray = new ArrayList<>();
		targetIntensityArray = new ArrayList<>();
		oneLeftIntensityArray = new ArrayList<>();
		oneRightIntensityArray = new ArrayList<>();
		twoLeftIntensityArray = new ArrayList<>();
		twoRightInetnsityArray = new ArrayList<>();
		modelMasses = new ArrayList<>();
		sharpness = 0.0F;
	}
}
