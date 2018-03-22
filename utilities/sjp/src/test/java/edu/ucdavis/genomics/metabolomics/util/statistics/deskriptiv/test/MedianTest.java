/*
 * Created on 29.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.test;

import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.Median;

import junit.framework.TestCase;

import java.util.List;
import java.util.Vector;


/**
 * @author wohlgemuth
 *
 */
public class MedianTest extends TestCase {
    /**
     * @param arg0
     */
    public MedianTest(String arg0) {
        super(arg0);
    }

    /**
     * DOCUMENT ME!
     */
    public void testCalculate() {
        List data = new Vector();
        data.add("1");
        data.add("2");
        data.add("3");
        data.add("2");
        data.add("1");
        data.add("0");

        Median v = new Median();
        assertTrue(Math.abs(v.calculate(data) - 1.5) < 0.0001);

        data.clear();
        data.add("1");
        data.add("2");
        data.add("3");
        assertTrue(Math.abs(v.calculate(data) - 2) < 0.0001);

        data.clear();
        data.add("1");
        assertTrue(Math.abs(v.calculate(data) - 1) < 0.0001);

        data.clear();
        data.add("1");
        data.add("1");

        assertTrue(Math.abs(v.calculate(data) - 1) < 0.0001);
    }
}
