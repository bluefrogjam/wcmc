/*
 * Created on Sep 27, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.xls;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;

import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ContentObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;
import org.apache.poi.ss.usermodel.Workbook;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public abstract class SplitterTestBasicClass extends TestCase {
    List<FormatObject<?>> data;
    Splitter splitter;

    /*
     * Test method for
     * 'edu.ucdavis.genomics.metabolomics.util.xls.Splitter.addLine(Collection)'
     */
    public void testAddLine() {
        splitter.addLine(data);
        splitter.addLine(data);
        splitter.addLine(data);

        for (int i = 0; i < 10; i++) {
            Sheet sheet = splitter.getBook().getSheetAt(i);

            assertNotNull(sheet);
            assertTrue(sheet.getPhysicalNumberOfRows() == 3);
        }
    }

    /*
     * Test method for
     * 'edu.ucdavis.genomics.metabolomics.util.xls.Splitter.getBook()'
     */
    public void testGetBook() {
        assertNotNull(splitter.getBook());
    }

    /*
     * Test method for
     * 'edu.ucdavis.genomics.metabolomics.util.xls.Splitter.isHeader()'
     */
    public void testIsHeader() {
        splitter.setHeader(true);
        assertTrue(splitter.isHeader());
        splitter.setHeader(false);
        assertFalse(splitter.isHeader());
    }

    /*
     * Test method for
     * 'edu.ucdavis.genomics.metabolomics.util.xls.Splitter.saveBook(OutputStream)'
     */
    public void testSaveBookOutputStream() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        splitter.saveBook(out);
    }

    /*
     * Test method for
     * 'edu.ucdavis.genomics.metabolomics.util.xls.Splitter.setHeader(boolean)'
     */
    public void testSetHeader() {
        splitter.setHeader(true);
        assertTrue(splitter.isHeader());
        splitter.setHeader(false);
        assertFalse(splitter.isHeader());
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();

        data = new Vector();

        for (int i = 0; i < 2540; i++) {
            data.add(new ContentObject<String>(String.valueOf(i)));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        splitter = null;
    }
}
