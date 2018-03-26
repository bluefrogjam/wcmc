/*
 * Created on 10.06.2004
 */
package edu.ucdavis.genomics.metabolomics.util.type.converter.test;

import edu.ucdavis.genomics.metabolomics.util.type.converter.BooleanConverter;
import junit.framework.TestCase;


/**
 * @author wohlgemuth
 */
public class BooleanConverterTest extends TestCase {
    /**
     * @param arg0
     */
    public BooleanConverterTest(String arg0) {
        super(arg0);
    }

    /**
     * DOCUMENT ME!
     */
    public static void testBooleanToString() {
        assertTrue("TRUE".equals(BooleanConverter.booleanToString(true)));
        assertTrue("FALSE".equals(BooleanConverter.booleanToString(false)));
    }

    /**
     * DOCUMENT ME!
     */
    public static void testStringtoBoolean() {
        assertTrue(BooleanConverter.StringtoBoolean("TRUE") == true);
        assertTrue(BooleanConverter.StringtoBoolean("FALSE") == false);
    }
}
