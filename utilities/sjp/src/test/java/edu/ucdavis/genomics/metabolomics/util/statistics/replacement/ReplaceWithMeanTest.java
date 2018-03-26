/*
 * Created on Sep 27, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.replacement;

import junit.framework.TestCase;

import java.util.List;
import java.util.Vector;

import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ContentObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class ReplaceWithMeanTest extends TestCase {
    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ReplaceWithMeanTest.class);
    }

    /**
     * DOCUMENT ME!
     */
    public void testReplaceZeros() {
        List data = new Vector();
        data.add("1");
        data.add("0");
        data.add("2");
        data.add(new ContentObject<Double>(0.0));
        data.add("3");
        data.add("2");
        data.add(new ContentObject<Double>(1.0));
        data.add("0");

        ReplaceWithMean discard = new ReplaceWithMean();

        assertTrue(discard.replaceZeros(data).size() == 8);
        assertFalse(discard.replaceZeros(data).contains("0"));
        assertTrue((Double.parseDouble(data.get(1).toString()) - 1.8) < 0.001);
        assertTrue(data.get(3) instanceof FormatObject);
        assertTrue(data.get(6) instanceof FormatObject);
        
    }
}
