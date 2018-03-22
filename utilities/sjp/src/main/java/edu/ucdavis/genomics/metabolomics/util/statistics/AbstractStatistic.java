/*
 * Created on 28.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics;

import edu.ucdavis.genomics.metabolomics.util.statistics.replacement.NoReplacement;
import edu.ucdavis.genomics.metabolomics.util.statistics.replacement.ZeroReplaceable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import java.util.List;
import java.util.Vector;


/**
 * @author wohlgemuth
 */
public abstract class AbstractStatistic implements Statistics {
    /**
     * DOCUMENT ME!
     */
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * DOCUMENT ME!
     *
     * @uml.property name="zero"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    protected ZeroReplaceable zero = new NoReplacement();

    /**
     * gibt die daten f?r diesen metaboliten zur?ck, die leerstellen werden bereits ersetzt
     *
     * @param position
     * @param classname
     * @return
     */
    public List getMetabolite(List data, int position, String classname) {
        List list = new Vector();

        for (int i = 0; i < data.size(); i++) {
            List temp = (List) data.get(i);

            if (temp.get(1).toString().equals(classname)) {
                list.add(temp.get(position + 2).toString().replace(',', '.'));
            }
        }

        return zero.replaceZeros(list);
    }

    /**
     * gibt die daten f?r diesen metaboliten zur?ck, die leerstellen werden bereits ersetzt
     *
     * @param position
     * @param classname
     * @return
     */
    public List getMetaboliteWithZeros(List data, int position, String classname) {
        List list = new Vector();

        for (int i = 0; i < data.size(); i++) {
            List temp = (List) data.get(i);

            if (temp.get(1).toString().equals(classname)) {
                list.add(temp.get(position + 2).toString().replace(',', '.'));
            }
        }

        return list;
    }

    /**
     * DOCUMENT ME!
     *
     * @param method DOCUMENT ME!
     */
    public void setZeroReplacementMethod(ZeroReplaceable method) {
        this.zero = method;
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.transform.Statistics#generateGraphics(boolean,
     *      File)
     */
    public void generateGraphics(boolean value, File dir) {
    }
}
