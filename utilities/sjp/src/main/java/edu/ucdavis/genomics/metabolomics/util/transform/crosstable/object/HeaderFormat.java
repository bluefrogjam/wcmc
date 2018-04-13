/*
 * Created on Jun 16, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object;

import org.xml.sax.Attributes;

public class HeaderFormat<Type> extends FormatObject<Type> {

    /**
     *
     */
    private static final long serialVersionUID = 2L;

    public HeaderFormat(Type value) {
        super(value);
    }

    public HeaderFormat(Type value, Attributes a) {
        super(value, a);
    }
}
