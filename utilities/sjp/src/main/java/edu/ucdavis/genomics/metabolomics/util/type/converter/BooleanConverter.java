/*
 * Created on 10.06.2004
 */
package edu.ucdavis.genomics.metabolomics.util.type.converter;


/**
 * @author wohlgemuth
 */
public class BooleanConverter {
    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public static boolean StringtoBoolean(String b) {
        return Boolean.valueOf(b).booleanValue();
    }

    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public static String booleanToString(boolean b) {
        if (b == true) {
            return "TRUE";
        } else {
            return "FALSE";
        }
    }
}
