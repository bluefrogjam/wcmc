package edu.ucdavis.fiehnlab.utilities.minix.types;

import org.springframework.data.annotation.Id;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by diego on 4/20/2017.
 */
public class PredictedFameSample {
	@Id
	private String sample;
	private Map<Integer, Double> predictions = new HashMap<>();
	private Double ftic = 0.0;

	public PredictedFameSample(String sample) {
		this.sample = sample;
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append("PredictedFameSample { sample: ")
				.append(this.sample)
				.append(", predicted fames: ")
				.append(predictions.entrySet().stream().map(k -> k.getKey() + "=" + k.getValue())
						.collect(Collectors.joining(", "))).toString();
	}

	public String getSample() {
		return sample;
	}

	public void setSample(String sample) {
		this.sample = sample;
	}

	public void addPredictedFame(Integer binId, Double intensity) {
		predictions.put(binId, intensity);
		ftic += intensity;
	}

	public Map<Integer, Double> getPredictions() {
		return predictions;
	}

	public void setPredictions(Map<Integer, Double> preds) {
		predictions = preds;
	}

	public Double getPredictionByBinId(Integer binId) {
		return predictions.get(binId);
	}

	public Double getFtic() {
		return predictions.entrySet().stream().map(Map.Entry::getValue).mapToDouble(v -> v).sum();
	}

	public void setFtic(Double value) {
		ftic = value;
	}
}
