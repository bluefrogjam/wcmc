/*
 * Created on Aug 14, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable.format.xls;

import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.XMLtoFileTransformHandler;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.BinObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.MetaObject;
import edu.ucdavis.genomics.metabolomics.util.xls.ColoredSplitToSheets;
import edu.ucdavis.genomics.metabolomics.util.xls.Splitter;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;


/**
 * @author wohlgemuth
 * @version Aug 14, 2003
 * <br>
 * BinBaseDatabase
 * @description Transformiert eine XML Datei in eine XLStabelle, voraussetzung ist das es sich um ein valides quantifizierungs dokument handelt welches von der Klasse Quantfinder erstellt wird.
 */
public class XMLtoXLSTransformHandler extends XMLtoFileTransformHandler {
    /**
     * @uml.property name="sheeter"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    Splitter sheeter = new ColoredSplitToSheets();

    /**
     * DOCUMENT ME!
     */
    private Collection<Map<String, FormatObject<?>>> data = new Vector<Map<String, FormatObject<?>>>();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getDescription() {
        return "creates a xls file";
    }

    /**
     * DOCUMENT ME!
     *
     * @param split DOCUMENT ME!
     * @uml.property name="sheeter"
     */
    public void setSheeter(Splitter split) {
        this.sheeter = split;
    }

    /**
     * @version Aug 15, 2003
     * @author wohlgemuth <br>
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.transform.crosstable.XMLtoFileTransformHandler#setShortFile(String)
     */
    public void setShortFile(String file) {
        this.setFile(new File(file + ".xls"));
    }

    /**
     * @version Aug 14, 2003
     * @author wohlgemuth <br>
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endTransform() {
        try {
            Iterator<Map<String, FormatObject<?>>> it = data.iterator();
            sheeter.addLine(new Vector<FormatObject<?>>());
            sheeter.addLine(new Vector<FormatObject<?>>());

            boolean first = true;

            while (it.hasNext()) {
                Map<String, FormatObject<?>> map = it.next();
                Set<String> c = map.keySet();

                if (first) {
                    Iterator<String> itx = c.iterator();

                    List<FormatObject<?>> list = new Vector<FormatObject<?>>();
                    list.add(new MetaObject<String>("header"));

                    while (itx.hasNext()) {
                        String header = itx.next();
                        list.add(new MetaObject<String>(header));
                    }

                    sheeter.addLine(list);
                    first = false;
                }

                List<FormatObject<?>> list = new Vector<FormatObject<?>>();
                Iterator<String> itx = c.iterator();

                list.add(new BinObject<String>("bin"));

                while (itx.hasNext()) {
                    Object header = itx.next();
                    list.add(map.get(header));
                }

                sheeter.setHeader(true);
                sheeter.addLine(list);
            }

            sheeter.setHeader(false);
            sheeter.saveBook(this.getStream());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @version Aug 18, 2003
     * @author wohlgemuth <br>
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        super.startDocument();
        data.clear();
        sheeter.setHeader(false);
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return "excel file with mutlitple sheets";
    }

    /**
     * DOCUMENT ME!
     *
     * @param map DOCUMENT ME!
     */
    public void writeBinProperties(Map<String, FormatObject<?>> map) {
        data.add(map);
    }

    /**
     * @version Aug 14, 2003
     * @author wohlgemuth <br>
     * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.quantification.quantify.XMLTransformHandler#writeLine(Collection)
     */
    public void writeLine(List<FormatObject<?>> line) {
        sheeter.addLine(line);
    }
}
