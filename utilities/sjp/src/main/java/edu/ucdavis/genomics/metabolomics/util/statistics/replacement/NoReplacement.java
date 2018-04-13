/*
 * Created on 28.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.replacement;

import java.util.List;


/**
 * @author wohlgemuth
 */
public class NoReplacement implements ZeroReplaceable {
    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.replacement.ZeroReplaceable#replaceZeros(List)
     */
    public List replaceZeros(List list) {
        return list;
    }

    @Override
    public String getDescription() {
        return "does not do any form of replacment";
    }
}
