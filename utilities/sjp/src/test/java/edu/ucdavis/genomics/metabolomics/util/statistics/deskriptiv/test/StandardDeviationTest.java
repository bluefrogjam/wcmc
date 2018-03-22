/*
 * Created on 29.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.test;

import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.StandardDeviation;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ContentObject;

import junit.framework.TestCase;

import java.util.List;
import java.util.Vector;


/**
 * @author wohlgemuth
 *
 */
public class StandardDeviationTest extends TestCase {
    /**
     * @param arg0
     */
    public StandardDeviationTest(String arg0) {
        super(arg0);
    }

    /**
     * DOCUMENT ME!
     */
    public void testCalculate() {
        List data = new Vector();
        data.add("1");
        data.add("2");
        data.add(new ContentObject<Double>(3.0));
        data.add("2");
        data.add("1");
        data.add(new ContentObject<Double>(0.0));

        StandardDeviation v = new StandardDeviation();
        assertTrue(v.calculate(data) >= 1.048808);
        assertTrue(v.calculate(data) <= 1.048809);
    }
}
