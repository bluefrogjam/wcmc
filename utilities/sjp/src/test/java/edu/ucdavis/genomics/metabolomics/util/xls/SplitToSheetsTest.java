/*
 * Created on Sep 27, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.xls;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class SplitToSheetsTest extends SplitterTestBasicClass {
    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SplitToSheetsTest.class);
    }

    /**
     * DOCUMENT ME!
     */
    public void testIsHeader() {
        // not supported by this class
    }

    /**
     * DOCUMENT ME!
     */
    public void testSetHeader() {
        // not supported by this class
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
        this.splitter = new SplitToSheets();
    }
}
