package edu.ucdavis.fiehnlab.ms.carrot.core.preprocess.model;

import com.fasterxml.jackson.annotation.*;

/**
 * Created by diego on 2/9/2017.
 */

public class Alignment implements IAlignment {
	@JsonProperty(value = "rttolerance")
	private double retentionTimeTolerance = 0.075;
	@JsonProperty(value = "eisimilaritytolerance")
	private double eiSimilarityTolerance = 70;
	@JsonProperty(value = "rtfactor")
	private double retentionTimeFactor = 0.5;
	@JsonProperty(value = "eisimilarityfactor")
	private double eiSimilarityFactor = 0.5;
	@JsonProperty(value = "peakcountfilter")
	private int peakCountFilter = 0;
	@JsonProperty(value = "qcatleastfilter")
	private boolean qcAtLeastFilter = true;

	@JsonIgnore
	public String getName() {
		return "#Alignment parameters setting";
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Retention time tolerance for alignment: ").append(retentionTimeTolerance).append("\n")
				.append("EI similarity tolerance for alignment: ").append(eiSimilarityTolerance).append("\n")
				.append("Retention time factor for alignment: ").append(retentionTimeFactor).append("\n")
				.append("EI similarity factor for alignment: ").append(eiSimilarityFactor).append("\n")
				.append("Peak count filter: ").append(peakCountFilter).append("\n")
				.append("QC at least filter: ").append(qcAtLeastFilter ? "True" : "False").append("\n");

		return str.toString();
	}
}
