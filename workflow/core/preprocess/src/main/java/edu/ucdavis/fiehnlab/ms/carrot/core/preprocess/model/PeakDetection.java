package edu.ucdavis.fiehnlab.ms.carrot.core.preprocess.model;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diego on 2/9/2017.
 */

public class PeakDetection implements IPeakDetection {
	private List<String> methods = new ArrayList<String>() {{
		add("SimpleMovingAverage");
		add("LinearWeightedMovingAverage");
		add("SavitzkyGolayFilter");
		add("BinomialFilter");
	}};

	private String smoothingMethod = "LinearWeightedMovingAverage";
	private int smoothingLevel = 2;
	private int averagePeakWidth = 20;
	private int minimumPeakHeight = 2000;
	private Double massSliceWidth = 0.5;
	private Double massAccuracy = 0.5;

	@JsonProperty(value = "smoothingmethod")
	public String getSmoothingMethod() {
		return smoothingMethod;
	}

	public void set(String value) {
		if (!methods.contains(value)) {
			value = "LinearWeightedMovingAverage";
		}
		smoothingMethod = value;
	}

	@JsonProperty(value = "smoothinglevel")
	public int getSmoothingLevel() {
		return smoothingLevel;
	}

	public void setSmoothingLevel(int value) {
		if (value < 1) {
			value = 1;
		}
		smoothingLevel = value;
	}

	@JsonProperty(value = "avgpeakwidth")
	public int getAveragePeakWidth() {
		return averagePeakWidth;
	}

	public void setAveragePeakWidth(int value) {
		if (value < 0) {
			value = 0;
		}
		averagePeakWidth = value;
	}

	@JsonProperty(value = "minpeakheight")
	public int getMinimumPeakHeight() {
		return minimumPeakHeight;
	}

	public void setMinimumPeakHeight(int value) {
		if (value < 0) {
			value = 0;
		}
		minimumPeakHeight = value;
	}

	@JsonProperty(value = "mzslicewidth")
	public Double getMassSliceWidth() {
		return massSliceWidth;
	}

	public void setMassSliceWidth(Double value) {
		if (value < 0.0) {
			value = 0.0;
		}
		massSliceWidth = value;
	}

	@JsonProperty(value = "mzaccuracy")
	public Double getMassAccuracy() {
		return massAccuracy;
	}

	public void setMassAccuracy(Double value) {
		if (value < 0) {
			value = 0.0;
		}
		massAccuracy = value;
	}

	@JsonIgnore
	public String getName() {
		return "#Peak detection parameters";
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Smoothing method: ").append(smoothingMethod).append("\n")
				.append("Smoothing level: ").append(smoothingLevel).append("\n")
				.append("Average peak width: ").append(averagePeakWidth).append("\n")
				.append("Minimum peak height: ").append(minimumPeakHeight).append("\n")
				.append("Mass slice width: ").append(massSliceWidth.toString()).append("\n")
				.append("Mass accuracy: ").append(massAccuracy.toString()).append("\n");

		return str.toString();
	}
}
