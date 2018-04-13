/*
 * Created on Jun 16, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.xls;

import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * converts a list to a xls file
 *
 * @author wohlgemuth
 * @version Jun 16, 2006
 */
public class DataFileToXLSConverter {


    /**
     * @param stream the tab delimited inputstream
     * @return the workbook
     */
    public Workbook convert(DataFile data, int sheetSize) throws IOException {
        Splitter splitter = new ColoredSplitToSheets(sheetSize);

        for (int i = 0; i < data.getTotalRowCount(); i++) {
            splitter.addLine((List<FormatObject<?>>) data.getRow(i));
        }
        Workbook book = splitter.getBook();
        return book;
    }

    /**
     * args[0] = input file
     * args[1] = output file
     *
     * @param args
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        new TXTtoXLSConverter().convert(new FileInputStream(args[0])).write(new FileOutputStream(args[1]));
    }
}
