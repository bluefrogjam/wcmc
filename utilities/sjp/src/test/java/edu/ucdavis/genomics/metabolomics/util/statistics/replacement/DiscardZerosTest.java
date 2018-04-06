/*
 * Created on 29.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.replacement;

import junit.framework.TestCase;

import java.util.List;
import java.util.Vector;


/**
 * @author wohlgemuth
 */
public class DiscardZerosTest extends TestCase {
    /**
     * @param arg0
     */
    public DiscardZerosTest(String arg0) {
        super(arg0);
    }

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

        DiscardZeros discard = new DiscardZeros();
        assertTrue(discard.replaceZeros(data).size() == 5);
        assertFalse(discard.replaceZeros(data).contains("0"));
    }
}
