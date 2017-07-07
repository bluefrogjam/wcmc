package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types;

/**
 * Created by diego on 9/13/2016.
 */
public class Ion {
	public double mass;
	public double intensity;

	public Ion(double mass, double intensity) {
		this.mass = mass;
		this.intensity = intensity;
	}

	public double mass() { return mass; }
	public double intensity() { return intensity; }

	@Override
	public String toString() {
		return String.format("%.5f:%.5f", mass, intensity);
	}
}
