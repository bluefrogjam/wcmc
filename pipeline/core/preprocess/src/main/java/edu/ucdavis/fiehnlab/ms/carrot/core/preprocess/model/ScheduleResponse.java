package edu.ucdavis.fiehnlab.ms.carrot.core.preprocess.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;

/**
 * Created by diego on 2/7/2017.
 */
@Component
public class ScheduleResponse {
	@JsonProperty
	public String filename;
	@JsonProperty
	public String link;
	@JsonProperty(defaultValue = "")
	public String message;
	@JsonProperty(required = false)
	public String error;

	public ScheduleResponse(){}

	public ScheduleResponse(String message, String link){
		this.message = message;
		this.link= link;
	}

	@Override
	public String toString() {
		return String.format("Message: %s; status check url: %s", message, link);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ScheduleResponse)) { return false; }
		ScheduleResponse other = (ScheduleResponse)obj;
		return other.link.equals(link) && other.message.equals(message);
	}
}
