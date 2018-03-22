/*
 * Created on 29.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.test;

import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.NonZeroMedian;

import junit.framework.TestCase;

import java.util.List;
import java.util.Vector;


/**
 * @author wohlgemuth
 *
 */
public class NonZeroMedianTest extends TestCase {
    /**
     * @param arg0
     */
    public NonZeroMedianTest(String arg0) {
        super(arg0);
    }

    /**
     * DOCUMENT ME!
     */
    public void testCalculate() {
        List data = new Vector();
        data.add("1");
        data.add("2");
        data.add(new Double(3));
        data.add("2");
        data.add("1");
        data.add("0");

        NonZeroMedian v = new NonZeroMedian();
        assertTrue(Math.abs(v.calculate(data) - 2.0) < 0.0001);
    }
}
