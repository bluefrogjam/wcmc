/*
 * Created on Jun 16, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object;

import org.xml.sax.Attributes;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * needed for format objects and works internally on unmodifed maps. So once you
 * set the attributes you cant change them anymore! Except using the method set
 * attributes
 *
 * @author wohlgemuth
 * @version Jun 16, 2006
 */
public abstract class FormatObject<Type> implements Serializable, Cloneable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    protected void finalize() throws Throwable {
        this.attributes.clear();
        this.attributes = null;
        super.finalize();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FormatObject) {
            FormatObject<?> o = (FormatObject<?>) obj;

            return o.getValue().equals(this.getValue());
        }
        return false;
    }

    /**
     * the value which should be formated
     */
    public Type value;

    /**
     * associated attributes from the xml file
     */
    public Map<String, String> attributes = null;

    public Type getValue() {
        return value;
    }

    public void setValue(Type value) {
        this.value = value;
    }

    public FormatObject(Type value) {
        this.setValue(value);
    }

    public FormatObject(Type value, Attributes attributes) {
        this.setValue(value);
        this.setAttributes(attributes);
    }

    public FormatObject(Type value, final Map<String, String> attributes) {
        this.setValue(value);
        if (attributes == null) {
            this.attributes = Collections.unmodifiableMap(new HashMap<String, String>());
        } else {
            this.attributes = Collections.unmodifiableMap(attributes);
        }
    }

    protected void setAttributes(Attributes attributes2) {
        Map<String, String> temp = new HashMap<String, String>();
        for (int i = 0; i < attributes2.getLength(); i++) {

            temp.put(attributes2.getQName(i), attributes2.getValue(i));
        }

        this.attributes = Collections.unmodifiableMap(temp);

    }

    public FormatObject() {
    }

    public String toString() {
        return getValue().toString() + " - " + getAttributes();
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void addAttribute(String name, String value) {
        Iterator<String> it = this.getAttributes().keySet().iterator();

        Map<String, String> temp = new HashMap<String, String>();

        while (it.hasNext()) {
            String key = it.next();
            temp.put(key, this.getAttributes().get(key));
        }

        temp.put(name, value);

        this.attributes = Collections.unmodifiableMap(temp);

    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = Collections.unmodifiableMap(attributes);
    }

}
