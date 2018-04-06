package edu.ucdavis.genomics.metabolomics.util.math.test;

import edu.ucdavis.genomics.metabolomics.util.math.Similarity;
import junit.framework.TestCase;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class SimilarityTest extends TestCase {
    /**
     * DOCUMENT ME!
     */
    private Similarity sim;

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
    }

    /*
     * Test method for
     * 'edu.ucdavis.genomics.metabolomics.util.math.Similarity.calculateSimimlarity()'
     */
    public void testCalculateSimimlarity() {
        sim.setLibrarySpectra("11:11 12:12 13:13 19:44 133:434");
        sim.setUnknownSpectra("11:11 12:12 13:13 19:44 133:434");

        assertTrue((sim.calculateSimimlarity() - 1000) < 0.0001);
    }

    public void testSpeed() {
        int count = 100000;

        sim.setLibrarySpectra("11:11 12:12 13:13 19:44 133:434");
        sim.setUnknownSpectra("11:11 12:12 13:13 19:44 133:434");

        long begin = System.nanoTime();

        for (int i = 0; i < count; i++) {
            sim.calculateSimimlarity();
        }

        long end = System.nanoTime();

        System.out.println((end - begin) / count);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        sim = new Similarity();
    }
}
