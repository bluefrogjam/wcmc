/*
 * Created on Sep 1, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable;

import edu.ucdavis.genomics.metabolomics.util.statistics.Statistics;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;


/**
 * @author wohlgemuth
 * @version Sep 1, 2003 <br>
 * BinBaseDatabase
 * @description definiert den auf einem xml format definierten transformator. Der dient dazu das binbase database resultfile in verschiedene tabellen zu transformieren
 */
public interface Transformator extends Serializable {
    /**
     * soll die fl?che exportiert werden
     */
    public static String AREA = "area";

    /**
     * soll die h?he exportiert werden
     */
    public static String HEIGHT = "height";

    /**
     * @param string
     * @version Aug 30, 2003
     * @author wohlgemuth <br>
     * @uml.property name="key"
     */
    public abstract void setKey(String string);

    /**
     * @return
     * @version Aug 30, 2003
     * @author wohlgemuth <br>
     * @uml.property name="key"
     */
    public abstract String getKey();

    /**
     * gibt den verwendeten handler zur?ck
     *
     * @return
     * @version Aug 30, 2003
     * @author wohlgemuth <br>
     * @uml.property name="transformer"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    public abstract AbstractXMLTransformHandler getTransformer();

    /**
     * @version Aug 30, 2003
     * @author wohlgemuth <br>
     */
    public abstract void run();

    /**
     * f?gt die statistische auswertung hinzu
     * f?gt eine statistische auswertung hinzu
     *
     * @param stat
     */
    public void setStatistics(Statistics stat);

    /**
     * setzt den zu verwendeten transformator
     *
     * @param handler
     * @version Aug 30, 2003
     * @author wohlgemuth <br>
     * @uml.property name="transformer"
     */
    public abstract void setTransformer(AbstractXMLTransformHandler handler);

    /**
     * setzt den inputstream
     *
     * @param stream
     * @version Sep 1, 2003
     * @author wohlgemuth <br>
     * @uml.property name="in"
     */
    public void setIn(InputStream stream);

    /**
     * gibt den inputstream zur?ck
     *
     * @return
     * @version Sep 1, 2003
     * @author wohlgemuth <br>
     * @uml.property name="in"
     */
    public InputStream getIn();

    /**
     * setzt den outputstream
     *
     * @param stream
     * @version Sep 1, 2003
     * @author wohlgemuth <br>
     * @uml.property name="out"
     */
    public void setOut(OutputStream stream);

    /**
     * gibt den autputstream zur?ck
     *
     * @return
     * @version Sep 1, 2003
     * @author wohlgemuth <br>
     * @uml.property name="out"
     */
    public OutputStream getOut();

    /**
     * another bin header to display
     *
     * @param header
     */
    public void addHeader(String header);
}
