/*
 * Created on Aug 26, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.util.math.test;

import edu.ucdavis.genomics.metabolomics.util.math.Correlation;
import junit.framework.TestCase;


/**
 * @author wohlgemuth
 * @version Aug 26, 2003
 * <br>
 * BinBaseDatabase
 * @description
 */
public class CorrelationTest extends TestCase {
    /**
     * @uml.property name="correlation"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    Correlation correlation = null;
    double[] x = null;
    double[] y = null;

    /**
     * Constructor for CorrelationTest.
     *
     * @param arg0
     */
    public CorrelationTest(String arg0) {
        super(arg0);
    }

    /**
     * DOCUMENT ME!
     */
    public void testCorrelation() {
        this.correlation.setX(x);
        this.correlation.setY(y);
        assertTrue(Math.abs(this.correlation.calculate() - 1) < 0.0001);
    }

    /**
     * DOCUMENT ME!
     */
    public void testGetX() {
        this.correlation.setX(x);
        assertTrue(this.correlation.getX() == this.x);
    }

    /**
     * DOCUMENT ME!
     */
    public void testGetY() {
        this.correlation.setY(y);
        assertTrue(this.correlation.getY() == this.y);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        this.correlation = new Correlation();
        this.x = new double[10];

        for (int i = 0; i < this.x.length; i++) {
            x[i] = i;
        }

        this.y = new double[10];

        for (int i = 0; i < this.y.length; i++) {
            y[i] = i;
        }
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        this.correlation = null;
        this.x = null;
        this.y = null;
    }
}
