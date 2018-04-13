package edu.ucdavis.genomics.metabolomics.util.math;


import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * uses the appache simple regression as base for the implementation
 *
 * @author wohlgemuth
 */
public class LeastSquareRegression implements Regression {
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    SimpleRegression regression = new SimpleRegression();
    private double[] y;
    private double[] x;

    public boolean equals(Object obj) {
        return regression.equals(obj);
    }

    public double getIntercept() {
        return regression.getIntercept();
    }

    public double getInterceptStdErr() {
        return regression.getInterceptStdErr();
    }

    public double getMeanSquareError() {
        return regression.getMeanSquareError();
    }

    public long getN() {
        return regression.getN();
    }

    public double getR() {
        return regression.getR();
    }

    public double getRegressionSumSquares() {
        return regression.getRegressionSumSquares();
    }

    public double getRSquare() {
        return regression.getRSquare();
    }

    public double getSignificance() {
        return regression.getSignificance();
    }

    public double getSlope() {
        return regression.getSlope();
    }

    public double getSlopeConfidenceInterval() {
        return regression.getSlopeConfidenceInterval();
    }

    public double getSlopeConfidenceInterval(double alpha) {
        return regression.getSlopeConfidenceInterval(alpha);
    }

    public double getSlopeStdErr() {
        return regression.getSlopeStdErr();
    }

    public double getSumOfCrossProducts() {
        return regression.getSumOfCrossProducts();
    }

    public double getSumSquaredErrors() {
        return regression.getSumSquaredErrors();
    }

    public double getTotalSumSquares() {
        return regression.getTotalSumSquares();
    }

    public double getXSumSquares() {
        return regression.getXSumSquares();
    }

    public String toString() {
        return regression.toString();
    }


    public double[] getCoeffizent() {
        return new double[]{regression.getRSquare()};
    }

    public double getY(double x) {
        return regression.predict(x);
    }

    public void setData(double[] x, double[] y) {
        regression.clear();
        for (int i = 0; i < x.length; i++) {
            regression.addData(x[i], y[i]);
        }

        this.x = x;
        this.y = y;

    }

    public String getFormula() {
        return "y   = " + regression.getIntercept() + regression.getSlope() + " * x";
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double getBetaX() {
        return regression.getSlope();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double getBetaY() {
        return regression.getIntercept();
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
