package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types;

import java.io.Serializable;

public class Peak implements Serializable {
    public double mz;
    public float intensity;
    public double retentionTime;
    public double resolution;
    public boolean isotopeFragment;
    public String comment = "NA";
    public int charge = 1;
    public int scanNumber;
    public PeakQuality peakQuality;

    public Peak(double mz, float intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }

    public Peak(int scanNumber, double retentionTime, double mz, float intensity) {
        this.scanNumber = scanNumber;
        this.retentionTime = retentionTime;
        this.mz = mz;
        this.intensity = intensity;
    }

    public Peak(int scanNumber, double retentionTime, double mz, double intensity) {
        this(scanNumber, retentionTime, mz, (float)intensity);
    }

    public double mz() {
        return mz;
    }
    public double intensity() {
        return intensity;
    }
}
