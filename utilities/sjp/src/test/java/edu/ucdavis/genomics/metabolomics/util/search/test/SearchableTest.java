package edu.ucdavis.genomics.metabolomics.util.search.test;

import edu.ucdavis.genomics.metabolomics.util.search.BinarySearch;
import edu.ucdavis.genomics.metabolomics.util.search.Searchable;
import junit.framework.TestCase;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class SearchableTest extends TestCase {
    /**
     * DOCUMENT ME!
     */
    private Searchable search = null;

    /*
     * Test method for
     * 'edu.ucdavis.genomics.metabolomics.util.search.Searchable.search(double[][],
     * int, double)'
     */
    public void testSearchDoubleArrayArrayIntDouble() {
        double[][] x = new double[10][10];

        for (int i = 0; i < x.length; i++) {
            for (int a = 0; a < x[i].length; a++) {
                x[i][a] = i;
            }
        }

        assertTrue(search.search(x, 0, 3) == 3);
        assertTrue(search.search(x, 1, 5) == 5);
        assertTrue(search.search(x, 2, 7) == 7);
    }

    /*
     * Test method for
     * 'edu.ucdavis.genomics.metabolomics.util.search.Searchable.search(double[],
     * double)'
     */
    public void testSearchDoubleArrayDouble() {
        double[] x = new double[10];

        for (int i = 0; i < x.length; i++) {
            x[i] = i;
        }

        int i = search.search(x, 4);
        System.err.println(i);
        assertTrue(x[i] == 4);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        search = new BinarySearch();
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
