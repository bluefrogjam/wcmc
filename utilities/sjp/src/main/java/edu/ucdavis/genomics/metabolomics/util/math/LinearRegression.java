package edu.ucdavis.genomics.metabolomics.util.math;

import edu.ucdavis.genomics.metabolomics.exception.CalculationException;


/**
 * calculates a linear regresion
 *
 * @author wohlgemuth
 */
public class LinearRegression implements Regression {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * x data
     */
    double[] x;

    /**
     * y data
     */
    double[] y;
    double ssto = 0.0;
    double sumx = 0.0;
    double sumx2 = 0.0;

    /**
     * sumy
     */
    double sumy = 0.0;
    double xbar;
    double xxbar = 0.0;
    double xybar = 0.0;
    double ybar;
    double yybar = 0.0;

    /**
     * DOCUMENT ME!
     */
    private double betaX;

    /**
     * DOCUMENT ME!
     */
    private double betaY;

    /**
     * DOCUMENT ME!
     */
    private double r2;

    /**
     * residual sum of squares
     */
    private double rss;

    /**
     * regression sum of squares
     */
    private double ssr;

    /**
     * DOCUMENT ME!
     */
    private double svar;

    /**
     * DOCUMENT ME!
     */
    private double svarX;

    /**
     * DOCUMENT ME!
     */
    private double svarY;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double getBetaX() {
        return betaX;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double getBetaY() {
        return betaY;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double[] getCoeffizent() {
        return new double[]{this.r2};
    }

    /**
     * sts the data and calculate the regression variables
     */
    public void setData(double[] x, double[] y) {
        if (x.length != y.length) {
            throw new CalculationException("x and y must have the same lengt!");
        }

        sumx = 0.0;
        sumy = 0.0;

        sumx2 = 0.0;

        xxbar = 0.0;

        yybar = 0.0;

        xybar = 0.0;

        sumx = 0;
        sumy = 0;

        this.x = x;
        this.y = y;

        for (int i = 0; i < x.length; i++) {
            sumx += x[i];
            sumx2 += (x[i] * x[i]);
            sumy += y[i];
        }

        xbar = sumx / x.length;
        ybar = sumy / y.length;

        // second pass: compute summary statistics
        for (int i = 0; i < x.length; i++) {
            xxbar += ((x[i] - xbar) * (x[i] - xbar));
            yybar += ((y[i] - ybar) * (y[i] - ybar));
            xybar += ((x[i] - xbar) * (y[i] - ybar));
        }

        betaX = xybar / xxbar;
        betaY = ybar - (betaX * xbar);

        // print results
        // analyze results
        int df = x.length - 2;
        rss = 0.0;
        ssr = 0.0;

        for (int i = 0; i < x.length; i++) {
            double fit = (betaX * x[i]) + betaY;
            rss += ((fit - y[i]) * (fit - y[i]));
            ssr += ((fit - ybar) * (fit - ybar));
        }

        r2 = ssr / yybar;
        svar = rss / df;
        svarX = svar / xxbar;
        svarY = (svar / x.length) + (xbar * xbar * svarX);
        ssto = yybar;
        svarY = (svar * sumx2) / (x.length * xxbar);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double getR2() {
        return r2;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double getRss() {
        return rss;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double getSsr() {
        return ssr;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double getSsto() {
        return ssto;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double getSumx() {
        return sumx;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double getSumy() {
        return sumy;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double getSvar() {
        return svar;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double getSvarX() {
        return svarX;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double getSvarY() {
        return svarY;
    }

    /**
     * DOCUMENT ME!
     *
     * @param x DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public double getY(double x) {
        return (betaX * x) + betaY;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getFormula() + "\n");

        buffer.append("y - values calculated\n");
        buffer.append("\n");

        for (int i = 0; i < this.x.length; i++) {
            buffer.append("y[" + i + "]\t=\t" + this.getY(x[i]));
            buffer.append("\n");
        }

        buffer.append("\n");
        buffer.append("y - values calibration\n");
        buffer.append("\n");

        for (int i = 0; i < this.x.length; i++) {
            buffer.append("y[" + i + "]\t=\t" + y[i]);
            buffer.append("\n");
        }

        buffer.append("\n");
        buffer.append("x - values\n");
        buffer.append("\n");

        for (int i = 0; i < this.x.length; i++) {
            buffer.append("x[" + i + "]\t=\t" + x[i]);
            buffer.append("\n");
        }

        return buffer.toString();
    }

    public String getFormula() {
        return "y   = " + betaX + " * x + " + betaY;
    }

    @Override
    public double[] getXData() {
        return x;
    }

    @Override
    public double[] getYData() {
        return y;
    }

    @Override
    public String[] getFormulas() {
        return new String[]{getFormula()};
    }
}
