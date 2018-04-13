/*
 * Created on 29.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.test;

import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.Modus;
import junit.framework.TestCase;

import java.util.List;
import java.util.Vector;


/**
 * @author wohlgemuth
 */
public class ModusTest extends TestCase {
    /**
     * @param arg0
     */
    public ModusTest(String arg0) {
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
        data.add("2");
        data.add("1");
        data.add("0");

        Modus v = new Modus();
        assertTrue(Math.abs(v.calculate(data) - 2) < 0.0001);
    }
}
