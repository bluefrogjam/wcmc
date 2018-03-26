package edu.ucdavis.genomics.metabolomics.util.statistics.replacement;

import edu.ucdavis.genomics.metabolomics.util.statistics.replacement.NoReplacement;

import junit.framework.TestCase;

import java.util.List;
import java.util.Vector;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class NoReplacementTest extends TestCase {
    /**
     * DOCUMENT ME!
     */
    public void testReplaceZeros() {
        List data = new Vector();
        data.add("1");
        data.add("0");
        data.add("2");
        data.add("0");
        data.add("3");
        data.add("2");
        data.add("1");
        data.add("0");

        NoReplacement discard = new NoReplacement();
        assertTrue(discard.replaceZeros(data).size() == 8);
        assertTrue(discard.replaceZeros(data).contains("0"));
    }
}
