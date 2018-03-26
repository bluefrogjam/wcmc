package edu.ucdavis.genomics.metabolomics.util.math.test;

import edu.ucdavis.genomics.metabolomics.util.math.CombinedRegression;
import edu.ucdavis.genomics.metabolomics.util.math.Regression;

import junit.framework.TestCase;

import junit.textui.TestRunner;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class CombinedRegressionTest extends TestCase {
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

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        TestRunner.run(CombinedRegressionTest.class);
    }

    /*
     * Test method for 'edu.ucdavis.genomics.metabolomics.util.math.LinearRegression.getY(double)'
     */
    public void testGetY() {
        assertTrue((regression.getY(394841) - 262581) < 1);
        assertTrue((regression.getY(375041) - 240918.39) < 1);
        assertTrue((regression.getY(1452990) - 1236199.15) < 1);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        regression = new CombinedRegression(5);
        x = new double[11];
        y = new double[11];

        x[0] = 394841.0;
        y[0] = 262320.0;
        x[1] = 451091.0;
        y[1] = 323120.0;
        x[2] = 507191.0;
        y[2] = 381020.0;
        x[3] = 708441.0;
        y[3] = 582620.0;
        x[4] = 794291.0;
        y[4] = 668720.0;
        x[5] = 943591.0;
        y[5] = 819620.0;
        x[6] = 1009440.0;
        y[6] = 886620.0;
        x[7] = 1070590.0;
        y[7] = 948820.0;
        x[8] = 1128440.0;
        y[8] = 1006900.0;
        x[9] = 1192090.0;
        y[9] = 1061700.0;
        x[10] = 1268940.0;
        y[10] = 1113100.0;

        regression.setData(x, y);
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
