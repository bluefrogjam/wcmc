package edu.ucdavis.fiehnlab.ms.carrot.core.preprocess.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by diego on 2/9/2017.
 */

public class DataType implements IDataType {
	private String dataType = "Centroid";
	private String ionMode = "Positive";
	private String accuracyType = "IsNominal";

	@JsonProperty(value = "datatype")
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String value) {
		if (value.toLowerCase().equals("Centroid") ||
				value.toLowerCase().equals("Profile")) {
			dataType = String.join(value.substring(0, 1).toUpperCase(), value.substring(1).toLowerCase());
		} else {
			dataType = "Centroid";
		}
	}

	@JsonProperty(value = "ionmode")
	public String getIonMode() { return ionMode; }

	public void	setIonMode(String value) {
		if (value.toLowerCase().equals("Positive") ||
				value.toLowerCase().equals("Negative")) {
			ionMode = String.join(value.substring(0, 1).toUpperCase(), value.substring(1).toLowerCase());
		} else {
			ionMode = "Positive";
		}
	}

	@JsonProperty(value = "accuracy")
	public String getAccuracyType() { return accuracyType; }
	public void setAccuracyType(String value) {
		if (value.toLowerCase().equals("isnominal")) {
			accuracyType = "IsNominal";
		}
		if (value.toLowerCase().equals("isaccurate")) {
			accuracyType = "IsAccurate";
		}
	}

	@JsonIgnore
	public String getName() {
		return "#Data type";
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Data type: ").append(dataType).append("\n")
				.append("Ion mode: ").append(ionMode).append("\n")
				.append("Accuracy type: ").append(accuracyType).append("\n");
		return str.toString();
	}
}

