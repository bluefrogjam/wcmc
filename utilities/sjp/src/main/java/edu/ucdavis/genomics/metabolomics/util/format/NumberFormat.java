/*
 * Created on Sep 23, 2003
 */
package edu.ucdavis.genomics.metabolomics.util.format;

import java.text.DecimalFormat;


/**
 * @author wohlgemuth
 * @version Sep 18, 2003
 * <br>
 * Bellerophon
 * @description
 */
public interface NumberFormat {
    DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.000");
    DecimalFormat GREAT_DOUBLE_FORMAT = new DecimalFormat("0.0000");
    DecimalFormat INT_FORMAT = new DecimalFormat("0");
    DecimalFormat PROCENTUAL_FORMAT = new DecimalFormat("%");
}
