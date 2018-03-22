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
 * 
 * @author wohlgemuth
 * @version Nov 18, 2005
 *
 */
public class ReplaceWithMinTest extends TestCase {
    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ReplaceWithMinTest.class);
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

        ReplaceWithMin discard = new ReplaceWithMin();
        List result = discard.replaceZeros(data);
        
        
        assertTrue((Double.parseDouble(result.get(1).toString()) - 1) < 0.001);
        assertTrue((Double.parseDouble(result.get(1).toString()) - 1) > -0.001);
        assertTrue(result.get(3) instanceof FormatObject);
        assertTrue(result.get(6) instanceof FormatObject);
    }
}
