package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types;

public class Peak {
    public double mz;
    public double intensity;
    public double retentionTime;
    public double resolution;
    public boolean isotopeFragment;
    public String comment = "NA";
    public int charge = 1;
    public int scanNumber;

    public Peak(double mz, double intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }
}
