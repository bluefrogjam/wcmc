/*
 * Created on Aug 18, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.util.xls;

import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


/**
 * @author wohlgemuth
 * @version Aug 18, 2003
 * <br>
 * BinBaseDatabase
 * @description
 */
public interface Splitter {
    /**
     * gibt das workbook zur?ck
     *
     * @return
     * @version Aug 21, 2003
     * @author wohlgemuth
     * <br>
     */
    Workbook getBook();

    /**
     * gibt an ob die aktuelle zeile ein header is, muss vor addLine aufgerufen werden und kann dazu verwendet werden
     * um formatierungen festzulegen
     *
     * @param value
     * @uml.property name="header"
     */
    void setHeader(boolean value);

    /**
     * gibt an ob die aktuelle zeile ein header is, muss vor addLine aufgerufen werden und kann dazu verwendet werden
     * um formatierungen festzulegen
     *
     * @uml.property name="header"
     */
    boolean isHeader();

    /**
     * f?gt eine neue linie in das workbook ein
     *
     * @param line
     * @version Aug 18, 2003
     * @author wohlgemuth
     * <br>
     */
    void addLine(List<FormatObject<?>> line);

    /**
     * Speichert die Datei als excel workbook und beendet die m?glichkeit des anf?gen von Linie
     *
     * @param file
     * @throws FileNotFoundException
     * @throws IOException
     * @version Aug 18, 2003
     * @author wohlgemuth
     * <br>
     */
    void saveBook(File file) throws FileNotFoundException, IOException;

    /**
     * @param stream
     * @version Aug 30, 2003
     * @author wohlgemuth
     * <br>
     */
    void saveBook(OutputStream stream) throws IOException;
}
