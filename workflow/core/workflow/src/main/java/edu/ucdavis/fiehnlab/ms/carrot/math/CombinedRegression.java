package edu.ucdavis.fiehnlab.ms.carrot.math;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.Regression;
import edu.ucdavis.fiehnlab.ms.carrot.math.exception.RegressionException;

/**
 * Created by wohlgemuth on 6/22/16.
 */
public class CombinedRegression implements Regression {

    /**
     * linear regression for everything smaller than the calibrated range
     */
    private Regression linearBegin = null;

    /**
     * linear regression for everything bigger than the calibrated range
     */
    private Regression linearEnd = null;

    /**
     * regression model for the calibration beetween the first and last dataset
     */
    private Regression polynomial = null;

    /**
     * returns only the coeeficent of the polynomial range
     */
    private double[] x;

    /**
     * DOCUMENT ME!
     */
    private double[] y;

    /**
     * how many points should be used for the calibration
     */
    private int n = 2;

    public CombinedRegression(int poly) {
        this.linearBegin = new LinearRegression();
        this.linearEnd = new LinearRegression();
        this.polynomial = new PolynomialRegression(poly);

    }

    public CombinedRegression(int n, int poly) {
        this.n = n;
        this.linearBegin = new LinearRegression();
        this.linearEnd = new LinearRegression();
        this.polynomial = new PolynomialRegression(poly);
    }



    public CombinedRegression() {
        this.linearBegin = new LinearRegression();
        this.linearEnd = new LinearRegression();
        this.polynomial = new PolynomialRegression();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double[] coefficient() {
        return polynomial.coefficient();
    }

    /**
     * the data must be sorted in the right way, there is now validataion!
     */
    public void calibration(double[] x, double[] y) throws RegressionException{
        if (x.length != y.length) {
            throw new RegressionException("x and y must have the same lengt!");
        }

        if (n > x.length) {
            throw new RegressionException("x must contain more data than the size of n!");

        }
        this.x = x;
        this.y = y;

        double[] linareBeginX = new double[n];
        double[] linareEndX = new double[n];
        double[] linareBeginY = new double[n];
        double[] linareEndY = new double[n];

        for (int i = 0; i < n; i++) {
            linareBeginX[i] = x[i];
            linareBeginY[i] = y[i];
        }

        int z = 0;

        for (int i = x.length - 1; i > (x.length - n - 1); i--) {
            linareEndX[z] = x[i];
            linareEndY[z] = y[i];
            z++;
        }

        linearEnd.calibration(linareEndX, linareEndY);
        linearBegin.calibration(linareBeginX, linareBeginY);
        polynomial.calibration(this.x, this.y);
    }

    /**
     * return the y value for the provided x value if(x < calibrated range) use
     * linear regression defined for the beginning if(x > calibrated range) use
     * linear regression defined for the end else us the polynomial regression
     */
    public double computeY(double x) {
        if (x < this.x[0]) {
            // System.err.println("linear begin " + linearBegin.getY(x));
            return this.linearBegin.computeY(x);
        }
        else if (x > this.x[this.x.length - 1]) {
            // System.err.println("linear end " + linearEnd.getY(x));
            return this.linearEnd.computeY(x);
        }
        else {
            // System.err.println("polynoimal " + polynomial.getY(x));
            return this.polynomial.computeY(x);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        StringBuffer a = new StringBuffer();
        a.append("begin:    \n" + linearBegin + "\n");
        a.append("end:       \n" + linearEnd + "\n");
        a.append("middle: \n" + polynomial + "\n");

        return a.toString();
    }

    @Override
    public double[] getXCalibrationData() {
        return x;
    }

    @Override
    public double[] getYCalibrationData() {
        // TODO Auto-generated method stub
        return y;
    }

    @Override
    public String[] getFormulas() {
        return new String[] { "begin: " + this.linearBegin.getFormulas()[0],"middle: " +  this.polynomial.getFormulas()[0],"end: " + this.linearEnd.getFormulas()[0]

        };
    }
}
