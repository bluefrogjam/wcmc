/*
 * Created on Jun 16, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object;

import org.xml.sax.Attributes;

public class SampleObject<Type> extends MetaObject<Type> {

    /**
     *
     */
    private static final long serialVersionUID = 2L;

    public SampleObject(Type value) {
        super(value);
    }

    public SampleObject(Type value, Attributes a) {
        super(value, a);
    }
}
