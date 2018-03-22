/*
 * Created on Nov 10, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.io.dest;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.io.source.FileSource;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import junit.framework.TestCase;

import java.io.*;

/**
 * runs the dest using a fiel as destination
 * @author wohlgemuth
 * @version Nov 10, 2005
 *
 */
public class FileDestinationTest extends TestCase{
    
    public FileDestinationTest() {
        super();
    }

	public void testGetOutputStream() throws IOException, ConfigurationException {

		Destination destination = new FileDestination("target");
		destination.setIdentifier("test.txt");
		OutputStream stream = destination.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
		writer.write("test\n");
		writer.write("1\n");
		writer.write("2\n");
		writer.write("3\n");
		writer.close();

		Source source = new FileSource(new File(new File("target"),"test.txt"));
		System.out.println("using source: " + source);
		System.out.println("source exist: " + source.exist());

		BufferedReader reader = new BufferedReader(new InputStreamReader(source.getStream()));
		assertTrue(reader.readLine().trim().equals("test"));
		assertTrue(reader.readLine().trim().equals("1"));
		assertTrue(reader.readLine().trim().equals("2"));
		assertTrue(reader.readLine().trim().equals("3"));
		reader.close();

	}


}
