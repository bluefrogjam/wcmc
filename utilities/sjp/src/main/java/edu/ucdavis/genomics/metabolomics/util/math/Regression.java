/*
 * Created on 18.04.2004
 *
 */
package edu.ucdavis.genomics.metabolomics.util.math;

import java.io.Serializable;


/**
 * basis klasse f?r regressionen
 *
 * @author wohlgemuth
 */
public interface Regression extends Serializable, Cloneable {
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    double[] getCoeffizent();

    /**
     * DOCUMENT ME!
     *
     * @param x DOCUMENT ME!
     * @param y DOCUMENT ME!
     */
    void setData(double[] x, double[] y);

    double[] getXData();

    double[] getYData();

    /**
     * DOCUMENT ME!
     *
     * @param x DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    double getY(double x);

    public String[] getFormulas();
}
