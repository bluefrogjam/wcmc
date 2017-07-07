package edu.ucdavis.fiehnlab.ms.carrot.core.preprocess.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by diego on 2/9/2017.
 */
public class DataCollection implements IDataCollection {
	private double retentionTimeBegin = 0D;
	private double retentionTimeEnd = 15D;
	private double massRangeBegin = 0D;
	private double massRangeEnd = 2000D;

	@JsonProperty(value = "rtbegin")
	public double getRetentionTimeBegin() { return retentionTimeBegin; }

	public void setRetentionTimeBegin(Double value) {
		if (value < 0.0) {
			value = 0.0;
		}
		retentionTimeBegin = value;
	}

	@JsonProperty(value = "rtend")
	public double getRetentionTimeEnd() { return retentionTimeEnd; }

	public void setRetentionTimeEnd(Double value) {
		if (value < 0.0) {
			value = 0.0;
		}
		if (value < retentionTimeBegin) {
			value = retentionTimeBegin;
		}
		retentionTimeEnd = value;
	}

	@JsonProperty(value = "mzrangebegin")
	public double getMassRangeBegin() { return massRangeBegin; }

	public void setMassRangeBegin(Double value) {
		if (value < 0) {
			value = 0.0;
		}
		massRangeBegin = value;
	}

	@JsonProperty(value = "mzrangeend")
	public double getMassRangeEnd() { return massRangeEnd; }

	public void setMassRangeEnd(Double value) {
		if (value < 0) {
			value = 0.0;
		}
		if (value < massRangeBegin) {
			value = massRangeBegin;
		}
		massRangeEnd = value;
	}

	@JsonIgnore
	public String getName() {
		return "#Data collection parameters";
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Retention time begin: ").append(retentionTimeBegin).append("\n")
				.append("Retention time end: ").append(retentionTimeEnd).append("\n")
				.append("Mass range begin: ").append(massRangeBegin).append("\n")
				.append("Mass range end: ").append(massRangeEnd).append("\n");

		return str.toString();
	}
}
