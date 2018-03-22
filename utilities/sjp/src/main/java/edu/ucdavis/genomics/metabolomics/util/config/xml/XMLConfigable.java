/*
 * Created on Aug 6, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.util.config.xml;

import edu.ucdavis.genomics.metabolomics.util.config.XMLConfigurator;
import org.jdom2.Element;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;


/**
 * @author wohlgemuth
 * @version Aug 6, 2003 <br>
 *          BinBaseDatabase
 * @description
 */
public interface XMLConfigable extends Serializable {
    /**
     * DOCUMENT ME!
     *
     * @param cof DOCUMENT ME!
     */
    public void setConfigProvider(XMLConfigurator cof);

    /**
     * setzt den wert eines attributes
     *
     * @param element
     * @param attribute
     * @param value
     */
    void setAttributeValue(String element, String attribute, String value);

    /**
     * returns an attrobute value
     * @param element
     * @param attribute
     */
    String getAttributeValue(String element, String attribute);
    
    /**
     * removes an attribute from the configuration
     * @param element
     * @param attribute
     */
    void removeAttribute(String element, String attribute);
    
    /**
     * adds an element to the parent with this given name
     * @param parentPath
     * @param element
     */
    void addElementToParent(String parentPath, Element element);
    
    /**
     * adds an element to the root node
     * @param element
     */
    void addElementToRoot(Element element);
    
    /**
     * adds a second configuration to this and this will overwrite existing targets
     * @param configuration
     */
    void addConfiguration(XMLConfigable configuration);
    
    /**
     * removes the children from the given parentpath
     * @param parentPath
     */
    void removeChildrenFromParent(String parentPath, String childrenName);
    
    /**
     * returns the config provider
     * @return
     */
    XMLConfigurator getConfigProvider();

    /**
     * gibt das element zur?ck
     *
     * @param desc
     *            beschreibung in form test.test
     * @return das letzte element
     */
    Element getElement(String desc);

    /**
     * returns all elements with this specific path
     * @param desc
     * @return
     */
    Collection<Element> getElements(String desc);
    
    /**
     * setzt die ben?tigten parameter fuer den configurator
     *
     * @version Aug 6, 2003
     * @author wohlgemuth <br>
     * @param element
     */
    void setParameter(Element element);

    /**
     * setzt den wert eines elements
     *
     * @param element
     * @param value
     */
    void setTextValue(String element, String value);

    /**
     * returns the unique identifier for this configuration
     * @return
     */
    int getUnigueID();

    /**
     * gibt den wert zur?ck
     *
     * @param desc
     *            beschreibung in form test.test
     * @return wert des elementes
     */
    String getValue(String desc);

    public void printTree(OutputStream out) throws IOException;
    
    /**
     * returns a list of all elemment names
     * @param out
     * @throws IOException
     */
    public Collection<String> getElementNames();
    
    /**
     * returns the full name for this specific element
     * @param element
     * @return
     */
    public String getName(Element element);
    
    /**
     * returns the document root
     * @return
     */
    public Element getRoot();
}
