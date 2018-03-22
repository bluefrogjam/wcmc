package edu.ucdavis.genomics.metabolomics.util.math;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Max Planck Institute</p>
 * @author Gert Wohlgemuth
 * @version 1.0
 */
public class Correlation {
    /**
     * x data
     */
    double[] x;

    /**
     * y data
     */
    double[] y;
    double avgX;
    double avgY;

    /**
     * @param x
     *
     * @uml.property name="x"
     */
    public void setX(double[] x) {
        this.x = x;
    }

    /**
     * @return
     *
     * @uml.property name="x"
     */
    public double[] getX() {
        return x;
    }

    /**
     * @param y
     *
     * @uml.property name="y"
     */
    public void setY(double[] y) {
        this.y = y;
    }

    /**
     * @return
     *
     * @uml.property name="y"
     */
    public double[] getY() {
        return y;
    }

    /**
           * @return
           * @throws ArithmeticException
           */
    public double calculate() throws ArithmeticException {
        //berechnen des mittelwertes
        this.avgX = 0;
        this.avgY = 0;

        double sumTop = 0;
        double sumBottom = 0;
        double sumXBottom = 0;
        double sumYBottom = 0;

        if (this.x.length != this.y.length) {
            throw new ArithmeticException(
                "both arrays must have the same length");
        }

        for (int i = 0; i < x.length; i++) {
            this.avgX = this.avgX + this.x[i];
            this.avgY = this.avgY + this.y[i];
        }

        this.avgX = this.avgX / x.length;
        this.avgY = this.avgY / y.length;

        //berechne abschnitte
        for (int i = 0; i < x.length; i++) {
            sumTop = sumTop + ((x[i] - avgX) * (y[i] - avgY));
            sumXBottom = sumXBottom + ((x[i] - avgX) * (x[i] - avgX));
            sumYBottom = sumYBottom + ((y[i] - avgY) * (y[i] - avgY));
        }

        sumBottom = Math.sqrt((sumXBottom * sumYBottom));

        return sumTop / sumBottom;
    }
}
