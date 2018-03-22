/*
 * Created on Aug 14, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable.format.ascii;

import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.XMLtoFileTransformHandler;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import java.util.Iterator;
import java.util.List;


/**
 * @author wohlgemuth
 * @version Aug 14, 2003
 * <br>
 * BinBaseDatabase
 * @description Transformiert eine XML Datei in eine ASCII Kreuztabelle, voraussetzung ist das es sich um ein validese quantifizierungs dokument handelt welches von der Klasse Quantfinder erstellt wird.
 */
public class XmlToAsciiTransformHandler extends XMLtoFileTransformHandler {
    /**
     * @version Aug 14, 2003
     * @author wohlgemuth
     * <br>
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public final void endTransform() {
        try {
            this.getStream().flush();
            this.getStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getDescription() {
        return "creates a plain tab delimited ascii file";
    }

    /**
     * @version Aug 15, 2003
     * @author wohlgemuth
     * <br>
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.transform.crosstable.XMLtoFileTransformHandler#setShortFile(String)
     */
    public void setShortFile(String file) {
        this.setFile(new File(file + ".txt"));
    }

    /**
     * @version Aug 14, 2003
     * @author wohlgemuth
     * <br>
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return "plain tab delimited ascii file";
    }

    /**
     * @version Aug 14, 2003
     * @author wohlgemuth
     * <br>
     * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.util.transform.abstracthandler.binlib.algorythm.quantification.quantify.XMLTransformHandler#writeLine(java.util.Collection)
     */
    public void writeLine(List line) {
        Iterator it = line.iterator();

        try {
            while (it.hasNext()) {
                this.getStream().write((it.next() + "\t").getBytes());
            }

            this.getStream().write(("\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
