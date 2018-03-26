package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api;

public class Scan {
    private double[] mzValues;
    private double[] intensityValues;
    private int scanNumber;
    private double mzRangeMin;
    private double mzRangeMax;

    public Scan(double[] _mzValues, double[] _intensityValues, int _scanNumber, double _mzRangeMin, double _mzRangeMax) {
        this.mzValues = _mzValues;
        this.intensityValues = _intensityValues;
        this.scanNumber = _scanNumber;
        this.mzRangeMin = _mzRangeMin;
        this.mzRangeMax = _mzRangeMax;
    }

    public Scan(int _scanNumber) {
        this.scanNumber = _scanNumber;
    }

    public double getMZRangeMin() {
        return this.mzRangeMin;
    }

    public void setMZRangeMin(double val) {
        this.mzRangeMin = val;
    }

    public double getMZRangeMax() {
        return this.mzRangeMax;
    }

    public void setMZRangeMax(double val) {
        this.mzRangeMax = val;
    }

    public double getTotalIonCurrent() {
        double intensitysum = 0.0D;

        for(int ei = 0; ei < this.intensityValues.length; ++ei) {
            intensitysum += this.intensityValues[ei];
        }

        return intensitysum;
    }

    public double getExtractedIonCurrent(double minMZ, double maxMZ) {
        double intensitysum = 0.0D;

        for(int ei = 0; ei < this.intensityValues.length; ++ei) {
            double mzvalue = this.mzValues[ei];
            if((minMZ == -1.0D || minMZ <= mzvalue) && (maxMZ == -1.0D || maxMZ >= mzvalue)) {
                intensitysum += this.intensityValues[ei];
            }
        }

        return intensitysum;
    }

    public double[] getBinnedIntensities(double startMZ, double stopMZ, int numOfBins, boolean interpolate) {
        double[] leftBorderInts = new double[numOfBins + 2];
        double[] rightBorderInts = new double[numOfBins + 2];
        double[] maxInts = new double[numOfBins];
        boolean[] leftBorderExists = new boolean[numOfBins + 2];
        boolean[] rightBorderExists = new boolean[numOfBins + 2];
        boolean[] maxExists = new boolean[numOfBins];
        double binwidth = (stopMZ - startMZ) / (double)numOfBins;

        int bini;
        for(bini = 0; bini < numOfBins + 2; ++bini) {
            leftBorderInts[bini] = 0.0D;
            leftBorderExists[bini] = false;
            rightBorderInts[bini] = 0.0D;
            rightBorderExists[bini] = false;
        }

        for(bini = 0; bini < numOfBins; ++bini) {
            maxInts[bini] = 0.0D;
            maxExists[bini] = false;
        }

        int mi = 0;
        rightBorderInts[0] = 0.0D;

        for(rightBorderExists[0] = true; mi < this.mzValues.length && this.mzValues[mi] < startMZ; ++mi) {
            rightBorderInts[0] = this.intensityValues[mi];
        }

        double limitMZ = startMZ + binwidth;
        bini = 0;

        while(mi < this.mzValues.length) {
            if(this.mzValues[mi] < limitMZ) {
                if(maxInts[bini] <= this.intensityValues[mi]) {
                    maxInts[bini] = this.intensityValues[mi];
                    maxExists[bini] = true;
                }

                if(!leftBorderExists[bini + 1]) {
                    leftBorderInts[bini + 1] = this.intensityValues[mi];
                    leftBorderExists[bini + 1] = true;
                }

                rightBorderInts[bini + 1] = this.intensityValues[mi];
                rightBorderExists[bini + 1] = true;
                ++mi;
            } else {
                ++bini;
                if(bini == numOfBins) {
                    break;
                }

                limitMZ += binwidth;
            }
        }

        if(mi < this.mzValues.length) {
            leftBorderInts[numOfBins + 1] = this.intensityValues[mi];
            leftBorderExists[numOfBins + 1] = true;
        } else {
            leftBorderInts[numOfBins + 1] = 0.0D;
            leftBorderExists[numOfBins + 1] = true;
        }

        if(interpolate) {
            double prevint = 0.0D;
            int prevbin = 0;
            double nextint = 0.0D;
            int nextbin = 1;

            for(mi = 0; mi < numOfBins; ++mi) {
                if(!maxExists[mi]) {
                    int ni;
                    for(ni = mi - 1; ni >= -1; --ni) {
                        if(rightBorderExists[ni + 1]) {
                            prevbin = ni;
                            prevint = rightBorderInts[ni + 1];
                            break;
                        }
                    }

                    for(ni = mi + 1; ni <= numOfBins + 1; ++ni) {
                        if(leftBorderExists[ni + 1]) {
                            nextbin = ni;
                            nextint = leftBorderInts[ni + 1];
                            break;
                        }
                    }

                    maxInts[mi] = prevint + (nextint - prevint) / ((double)nextbin - (double)prevbin) * ((double)mi - (double)prevbin);
                }
            }
        }

        return maxInts;
    }

    public void setScanNumber(int _scanNumber) {
        this.scanNumber = _scanNumber;
    }

    public int getScanNumber() {
        return this.scanNumber;
    }

    public void setMZValues(double[] _mzValues) {
        this.mzValues = _mzValues;
    }

    public double[] getMZValues() {
        return this.mzValues;
    }

    public void setIntensityValues(double[] _intensityValues) {
        this.intensityValues = _intensityValues;
    }

    public double[] getIntensityValues() {
        return this.intensityValues;
    }

    public double getMaxIntensity() {
        double maxIntensity = 4.9E-324D;
        double[] arr$ = this.intensityValues;
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            double intensity = arr$[i$];
            if(intensity > maxIntensity) {
                maxIntensity = intensity;
            }
        }

        return maxIntensity;
    }
}

