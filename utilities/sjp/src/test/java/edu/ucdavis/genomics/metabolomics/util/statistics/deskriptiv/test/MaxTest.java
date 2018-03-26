/*
 * Created on 29.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.test;

import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.Max;

import junit.framework.TestCase;

import java.util.List;
import java.util.Vector;


/**
 * @author wohlgemuth
 *
 */
public class MaxTest extends TestCase {
    /**
     * @param arg0
     */
    public MaxTest(String arg0) {
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

        Max v = new Max();
        assertTrue(Math.abs(v.calculate(data) - 3) < 0.0001);
    }

    /**
     * DOCUMENT ME!
     */
    public void testCalculateNulls() {
        List data = new Vector();
        data.add(null);
        data.add("2");
        data.add(null);
        data.add(null);
        data.add("1");
        data.add("0");

        Max v = new Max();
        assertTrue(Math.abs(v.calculate(data) - 2) < 0.0001);
    }

}
