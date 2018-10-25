/*
 * Created on Jun 16, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object;

import org.xml.sax.Attributes;

public class MetaObject<Type> extends FormatObject<Type> {

    /**
     *
     */
    private static final long serialVersionUID = 2L;

    public MetaObject(Type value) {
        super(value);
    }

    public MetaObject(Type value, Attributes a) {
        super(value, a);
    }
}
