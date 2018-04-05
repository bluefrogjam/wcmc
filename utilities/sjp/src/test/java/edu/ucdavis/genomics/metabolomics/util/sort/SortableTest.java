/*
 * Created on Sep 27, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.sort;

import junit.framework.TestCase;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class SortableTest extends TestCase {
    Sortable sort;
    double[] data;
    double[][] data2;

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SortableTest.class);
    }

    /*
     * Test method for 'edu.ucdavis.genomics.metabolomics.util.sort.Sortable.sort(double[])'
     */
    public void testSortDoubleArray() {
        double[] test = sort.sort(data);

        assertTrue(test[0] == 1);
        assertTrue(test[1] == 3);
        assertTrue(test[2] == 5);
        assertTrue(test[3] == 6);
        assertTrue(test[4] == 8);
    }

    /*
     * Test method for 'edu.ucdavis.genomics.metabolomics.util.sort.Sortable.sort(double[][], int)'
     */
    public void testSortDoubleArrayArrayInt() {
        double[][] test = sort.sort(data2, 0);

        assertTrue(test[0][0] == 1);
        assertTrue(test[1][0] == 3);
        assertTrue(test[2][0] == 5);
        assertTrue(test[3][0] == 6);
        assertTrue(test[4][0] == 8);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();

        sort = new Quicksort();
        data = new double[5];

        data[0] = 5;
        data[1] = 1;
        data[2] = 6;
        data[3] = 3;
        data[4] = 8;

        data2 = new double[5][2];

        data2[0][0] = 5;
        data2[1][0] = 1;
        data2[2][0] = 6;
        data2[3][0] = 3;
        data2[4][0] = 8;

        data2[0][1] = 5;
        data2[1][1] = 1;
        data2[2][1] = 6;
        data2[3][1] = 3;
        data2[4][1] = 8;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        super.tearDown();

        sort = null;
        data = null;
        data2 = null;
    }
}
