/*
 * Created on Sep 27, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.xls;

import junit.framework.TestCase;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;


/**
 * test class for the converter
 *
 * @author wohlgemuth
 *
 */
public class XLSToXMLConverterTest extends TestCase {
    InputStream stream;

    /**
     * DOCUMENT ME!
     */
    private HSSFSheet sheet1;

    /**
     * DOCUMENT ME!
     */
    private HSSFSheet sheet2;

    /**
     * DOCUMENT ME!
     */
    private HSSFSheet sheet3;

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(XLSToXMLConverterTest.class);
    }

    /*
     * Test method for
     * 'edu.ucdavis.genomics.metabolomics.util.xls.XLSToXMLConverter.convert(InputStream)'
     */
    public void testConvert() throws Exception {
        XLSToXMLConverter xls = new XLSToXMLConverter();
        Document d = xls.convert(stream);
        Element r = d.getRootElement();

        assertTrue(sheet1 != null);
        assertTrue(sheet2 != null);
        assertTrue(sheet3 != null);

        assertTrue(r.getChildren().size() == 3);
        assertTrue(r.getChildren("sheet").size() == 3);
        assertTrue(((Element) r.getChildren().get(0)).getChildren().size() == 10);
        assertTrue(((Element) r.getChildren().get(1)).getChildren().size() == 0);
        assertTrue(((Element) r.getChildren().get(2)).getChildren().size() == 0);

        Iterator it = ((Element) r.getChildren().get(0)).getChildren().iterator();

        while (it.hasNext()) {
            Element e = (Element) it.next();
            assertTrue(e.getChildren().size() == 5);
        }
    }

    /**
     * creates a basic workbook
     *
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();
        sheet1 = wb.createSheet();
        sheet2 = wb.createSheet();
        sheet3 = wb.createSheet();

        for (int i = 0; i < 10; i++) {
            HSSFRow r = sheet1.createRow(i);

            for (short x = 0; x < 5; x++) {
                HSSFCell c = r.createCell(x);
                c.setCellValue(x * i);
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);

        stream = new ByteArrayInputStream(out.toByteArray());
    }
}
