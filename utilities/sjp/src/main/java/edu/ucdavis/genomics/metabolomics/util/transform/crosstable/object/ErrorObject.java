/*
 * Created on Jun 16, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object;

import org.xml.sax.Attributes;

public class ErrorObject<Type> extends ContentObject<Type> {

    public ErrorObject(Type value) {
        super(value);

    }

    public ErrorObject(Type value, Attributes a) {
        super(value, a);
    }

    /**
     *
     */
    private static final long serialVersionUID = 2L;

}
