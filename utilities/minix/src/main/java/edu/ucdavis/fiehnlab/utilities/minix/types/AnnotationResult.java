package edu.ucdavis.fiehnlab.utilities.minix.types;

/**
 * this provides a general implementation of a MiniX annotation result set
 * concreate implementations will provide additional properties
 * and behavior
 */
public abstract class AnnotationResult {
    protected Double retentionindex;
    protected Double retentionTime = 0.0;
    protected String spectra;
    protected Double intensity = 0.0;

    public Double getRetentionindex() {
        return retentionindex;
    }

    public Double getRetentionTime() {
        return retentionTime;
    }

    public Double getIntensity() {
return intensity;
}

    public String getSpectra() {
        return spectra;
    }

}
