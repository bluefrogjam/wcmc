package edu.ucdavis.fiehnlab.ms.carrot.core.preprocess.model;

import com.fasterxml.jackson.annotation.*;

/**
 * Created by diego on 2/9/2017.
 */

public class Identification implements IIdentification {

	@JsonIgnore
	@JsonProperty(value = "mspfile")
	private String mspFile = ""; // Internal resource or file path
	@JsonIgnore
	@JsonProperty(value = "riindexfile")
	private String riIndexFile = "";
	@JsonProperty(value = "retentiontype")
	private String retentionType = "RT"; // RI or RT
	@JsonProperty(value = "ricompound")
	private String riCompound = "Fames"; // Alkanes or Fames
	@JsonProperty(value = "rttolerance")
	private double retentionTimeTolerance = 0.5;
	@JsonProperty(value = "ritolerance")
	private int retentionIndexTolerance = 3000;
	@JsonProperty(value = "eisimilaritytolerance")
	private double eiSimilarityLibraryTolerance = 70;
	@JsonProperty(value = "idscorecutoff")
	private int identificationScoreCutOff = 70;

	@JsonIgnore
	public String getName() {
		return "#identification";
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		if (mspFile != null && mspFile.length() > 0) {
			str.append("MSP file: ").append(mspFile);
		}
		if (riIndexFile != null && riIndexFile.length() > 0) {
			str.append("RI index file: ").append(riIndexFile);
		}
		str.append("Retention type: ").append(retentionType).append("\n")
				.append("RI compound: ").append(riCompound).append("\n")
				.append("Retention time tolerance for identification: ").append(retentionTimeTolerance).append("\n")
				.append("Retention index tolerance for identification: ").append(retentionIndexTolerance).append("\n")
				.append("EI similarity library tolerance for identification: ").append(eiSimilarityLibraryTolerance).append("\n")
				.append("Identification score cut off: ").append(identificationScoreCutOff).append("\n");

		return str.toString();
	}
}

