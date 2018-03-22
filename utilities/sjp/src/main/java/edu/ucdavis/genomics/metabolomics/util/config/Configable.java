/*
 * Created on 05.06.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.ucdavis.genomics.metabolomics.util.config;

import edu.ucdavis.genomics.metabolomics.util.config.xml.XMLConfigable;


/**
 * @author wohlgemuth
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface Configable {
    /**
     * DOCUMENT ME!
     *
     * @uml.property name="cONFIG"
     * @uml.associationEnd javaType="XMLConfigable" multiplicity="(0 1)"
     */
    XMLConfigable CONFIG = XMLConfigurator.getInstance().getXMLConfigable(
            "binbase.config");
}
