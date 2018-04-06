/*
 * Created on Sep 4, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.replacement;

import java.util.List;


/**
 * @author wohlgemuth
 * @version Sep 4, 2003 <br>
 * BinBaseDatabase
 * @description
 */
public interface ZeroReplaceable {

    /**
     * ersetzt alle nullstellen gegen einen anderen wert
     *
     * @param mean
     * @param list
     * @return
     * @version Aug 21, 2003
     * @author wohlgemuth <br>
     */
    public abstract List replaceZeros(List list);

    String getDescription();
}
