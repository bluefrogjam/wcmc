package edu.ucdavis.fiehnlab.ms.carrot.core.preprocess.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;

/**
 * Created by diego on 2/7/2017.
 */
@Component
public class StatusResponse {
	@JsonProperty(defaultValue = "")
	public String filename;
	@JsonProperty
	public String link;
	@JsonProperty(defaultValue = "")
	public String status;
	@JsonProperty(required = false)
	public String message;
	@JsonProperty(required = false)
	public String error;

	public StatusResponse() {}

	public StatusResponse(String filename, String status, String result, String error) {
		this.filename = filename;
		this.link = result;
		this.status = status;
		this.message = error;
		this.error = error;
	}

	@Override
	public String toString() {
		return String.format("File: %s is %s; download from: %s", filename, status, link);
	}

	@Override
	public boolean equals(Object obj) {
		if(! (obj instanceof StatusResponse)) { return false; }
		StatusResponse other = (StatusResponse)obj;

		return other.filename.equals(filename) && other.link.equals(link) && other.status.equals(status);
	}
}
