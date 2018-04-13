package edu.ucdavis.genomics.metabolomics.util.math.test;

import edu.ucdavis.genomics.metabolomics.util.math.LinearRegression;
import edu.ucdavis.genomics.metabolomics.util.math.Regression;
import junit.framework.TestCase;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class LinearRegressionTest extends TestCase {
    /**
     * DOCUMENT ME!
     */
    private Regression regression;

    /**
     * DOCUMENT ME!
     */
    private double[] x;

    /**
     * DOCUMENT ME!
     */
    private double[] y;

    /*
     * Test method for 'edu.ucdavis.genomics.metabolomics.util.math.LinearRegression.getCoeffizent()'
     */
    public void testGetCoeffizent() {
        x = new double[9];
        y = new double[9];

        x[0] = 1;
        x[1] = 2;
        x[2] = 3;
        x[3] = 4;
        x[4] = 5;
        x[5] = 6;
        x[6] = 7;
        x[7] = 8;
        x[8] = 9;

        y[0] = 12;
        y[1] = 23;
        y[2] = 43;
        y[3] = 54;
        y[4] = 65;
        y[5] = 67;
        y[6] = 68;
        y[7] = 77;
        y[8] = 88;

        regression.setData(x, y);

        System.out.println(regression.getCoeffizent()[0]);
        assertTrue(regression.getCoeffizent() != null);
        assertTrue(regression.getCoeffizent()[0] - 0.93214d < 0.0001);
    }

    /*
     * Test method for 'edu.ucdavis.genomics.metabolomics.util.math.LinearRegression.getY(double)'
     */
    public void testGetY() {
        x = new double[9];
        y = new double[9];

        x[0] = 1;
        x[1] = 2;
        x[2] = 3;
        x[3] = 4;
        x[4] = 5;
        x[5] = 6;
        x[6] = 7;
        x[7] = 8;
        x[8] = 9;

        y[0] = 12;
        y[1] = 23;
        y[2] = 43;
        y[3] = 54;
        y[4] = 65;
        y[5] = 67;
        y[6] = 68;
        y[7] = 77;
        y[8] = 88;

        regression.setData(x, y);

        System.err.println(regression);
        assertTrue(Math.abs(regression.getY(1) - 19.9557) < 0.001);
        assertTrue(Math.abs(regression.getY(2) - 28.7724) < 0.001);
        assertTrue(Math.abs(regression.getY(3) - 37.5891) < 0.001);
        assertTrue(Math.abs(regression.getY(4) - 46.4058) < 0.001);
        assertTrue(Math.abs(regression.getY(5) - 55.2225) < 0.001);
        assertTrue(Math.abs(regression.getY(6) - 64.0392) < 0.001);
        assertTrue(Math.abs(regression.getY(7) - 72.8559) < 0.001);
        assertTrue(Math.abs(regression.getY(8) - 81.6726) < 0.001);
        assertTrue(Math.abs(regression.getY(9) - 90.4893) < 0.001);
    }

    public void testGetY2() {
        x = new double[5];
        y = new double[5];

        x[0] = 1;
        x[1] = 2.5;
        x[2] = 5;
        x[3] = 10;
        x[4] = 25;

        y[0] = 0;
        y[1] = 0;
        y[2] = 0;
        y[3] = 0;
        y[4] = 15359;

        regression.setData(x, y);
        System.err.println(regression);
        assertTrue(Math.abs(regression.getY(1) - -2017.19) < 0.01);
        assertTrue(Math.abs(regression.getY(2.5) - -1025.825) < 0.01);
        assertTrue(Math.abs(regression.getY(5) - 626.4428458) < 0.01);
        assertTrue(Math.abs(regression.getY(10) - 3930.97954065) < 0.01);
        assertTrue(Math.abs(regression.getY(25) - 13844.58962) < 0.01);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        regression = generateRegression();

    }

    protected Regression generateRegression() {
        return new LinearRegression();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
