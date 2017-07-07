package edu.ucdavis.fiehnlab.ms.carrot.core.preprocess.model;

import com.fasterxml.jackson.annotation.*;

/**
 * Created by diego on 2/9/2017.
 */
public class Deconvolution implements IDeconvolution {
	private double sigmaWindowValue = 0.1;
	private int amplitudCutOff = 10;

	@JsonProperty(value="sigmawindowvalue")
	public double getSigmaWindowValue() {
		return sigmaWindowValue;
	}

	public void setSigmaWindowValue(Double value) {
		if (value < 0.0) {
			value = 0.0;
		}
		sigmaWindowValue = value;
	}

	@JsonProperty(value="amplitudcutoff")
	public int getAmplitudCutOff() {
		return amplitudCutOff;
	}

	public void setAmplitudCutOff(int value) {
		if (value < 0) {
			value = 0;
		}
		amplitudCutOff = value;
	}

	@JsonIgnore
	public String getName() {
		return "#MS1Dec parameters";
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Sigma window value: ").append(sigmaWindowValue).append("\n")
				.append("Amplitude cut off: ").append(amplitudCutOff).append("\n");

		return str.toString();
	}
}

