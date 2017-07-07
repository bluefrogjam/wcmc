package edu.ucdavis.fiehnlab.ms.carrot.core.preprocess.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by diego on 2/9/2017.
 */
public class Parameters implements IParameters {
	@JsonProperty(value="DataType")
	public IDataType dataType = new DataType();
	@JsonProperty(value="DataCollection")
	public IDataCollection dataCollection = new DataCollection();
	@JsonProperty(value="PeakDetection")
	public IPeakDetection peakDetection = new PeakDetection();
	@JsonProperty(value="Deconvolution")
	public IDeconvolution deconvolution = new Deconvolution();
	@JsonProperty(value="Identification")
	public IIdentification identification = new Identification();
	@JsonProperty(value="Alignment")
	public IAlignment alignment = new Alignment();

	@JsonIgnore
	public String getVersion() {
		return "2.26";
	}

	public static Parameters getDefaultParameters() {
		return new Parameters();
	}

	@Override
	public String toString() {
		StringBuilder paramStr = new StringBuilder();

		paramStr.append(dataType.getName()).append("\n").append(dataType).append("\n")
				.append(dataCollection.getName()).append("\n").append(dataCollection).append("\n")
				.append(peakDetection.getName()).append("\n").append(peakDetection).append("\n")
				.append(deconvolution.getName()).append("\n").append(deconvolution).append("\n");

		if (identification.toString() != null) {
			paramStr.append(identification.getName()).append("\n").append(identification);
		}
		if (alignment.toString() != null) {
			paramStr.append(alignment.getName()).append("\n").append(alignment);
		}

		return paramStr.toString();
	}
}
