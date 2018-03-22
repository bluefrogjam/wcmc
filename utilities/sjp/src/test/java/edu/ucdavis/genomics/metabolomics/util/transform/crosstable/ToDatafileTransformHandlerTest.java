/*
 * Created on Nov 19, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;

import junit.framework.TestCase;

public class ToDatafileTransformHandlerTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(ToDatafileTransformHandlerTest.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for
	 * 'edu.ucdavis.genomics.metabolomics.util.transform.crosstable.ToDatafileTransformHandler.getFile()'
	 */
	public void testGetFile() throws SAXException, IOException,
			ParserConfigurationException {
		ToDatafileTransformHandler handler = new ToDatafileTransformHandler();
		handler.setKey(Transformator.HEIGHT);
		handler.addHeader("retention_index");
		handler.addHeader("quantmass");
		handler.addHeader("id");

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);

		SAXParser builder = factory.newSAXParser();

		File files = new File("src/test/resources/test/1981.xml");
		assertTrue(files.exists());
		builder.parse(new FileInputStream(files),
				handler);
		DataFile file = handler.getFile();
		System.err.println(file.getColumnCount());
		assertTrue(file.getColumnCount() == 294);
		assertTrue(file.getRowCount() == 41 + 1 + handler.getHeader().size());
		file.write(new FileOutputStream(File.createTempFile("binbase-test", "result")));
	}
}
