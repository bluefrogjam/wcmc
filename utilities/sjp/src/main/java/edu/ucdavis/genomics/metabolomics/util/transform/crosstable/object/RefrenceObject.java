/*
 * Created on Jun 16, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object;

import org.xml.sax.Attributes;

public class RefrenceObject<Type> extends MetaObject<Type> {

    /**
     *
     */
    private static final long serialVersionUID = 2L;

    private String hyperlink;

    public RefrenceObject(Type value) {
        super(value);
    }

    public RefrenceObject(Type value, String hyperlink) {
        super(value);
        this.setHyperlink(hyperlink);
    }

    public RefrenceObject(Type value, Attributes a, String hyperlink) {
        super(value, a);
        this.setHyperlink(hyperlink);
    }

    public String getHyperlink() {
        return hyperlink;
    }

    public void setHyperlink(String hyperlink) {
        this.hyperlink = hyperlink;
    }
}
