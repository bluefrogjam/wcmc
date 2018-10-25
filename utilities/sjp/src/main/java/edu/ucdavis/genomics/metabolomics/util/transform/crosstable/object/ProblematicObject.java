/*
 * Created on Jun 16, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object;

import org.xml.sax.Attributes;

public class ProblematicObject<Type> extends ContentObject<Type> {

    /**
     *
     */
    private static final long serialVersionUID = 2L;


    public ProblematicObject(Type value) {
        super(value);
    }


    public ProblematicObject(Type value, Attributes a) {
        super(value, a);
    }

}
