/*
 * Created on Aug 14, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


/**
 * @author wohlgemuth
 * @version Aug 14, 2003
 * <br>
 * BinBaseDatabase
 * @description
 */
public abstract class XMLtoFileTransformHandler
    extends AbstractXMLTransformHandler {
    /**
     * setzt die ausgabedatei
     *
     * @param file
     * @version Aug 14, 2003
     * @author wohlgemuth
     * <br>
     */
    public final void setFile(File file) {
        try {
            this.setStream(new BufferedOutputStream(new FileOutputStream(file)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * setzt die ausgabe datei
     *
     * @param file
     * @version Aug 14, 2003
     * @author wohlgemuth
     * <br>
     */
    public final void setFile(String file) {
        this.setFile(new File(file));
    }

    /**
     * DOCUMENT ME!
     *
     * @param file DOCUMENT ME!
     */
    public abstract void setShortFile(String file);
}
