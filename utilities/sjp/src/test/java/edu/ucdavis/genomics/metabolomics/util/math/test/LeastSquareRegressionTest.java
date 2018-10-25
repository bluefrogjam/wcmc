package edu.ucdavis.genomics.metabolomics.util.math.test;

import edu.ucdavis.genomics.metabolomics.util.math.LeastSquareRegression;
import edu.ucdavis.genomics.metabolomics.util.math.Regression;
import org.junit.After;
import org.junit.Before;

public class LeastSquareRegressionTest extends LinearRegressionTest {

    @Override
    protected Regression generateRegression() {
        return new LeastSquareRegression();
    }

    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @After
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
