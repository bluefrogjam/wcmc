/*
 * Created on Jan 20, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.io.dest;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.io.source.ByteArraySource;
import edu.ucdavis.genomics.metabolomics.util.io.source.ByteArraySourceFactoryImpl;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;

import java.io.*;

public class ByteArrayDestinationTest extends AbstractDestinationTest {
	public ByteArrayDestinationTest() {
		super();
	}

    /*
     * Test method for 'edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.dest.DatabaseDestination.getOutputStream()'
     */
    public void testGetOutputStream() throws IOException, ConfigurationException {
    	
        ByteArrayDestination destination = new ByteArrayDestination();
        OutputStream stream = destination.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
        writer.write("test\n");
        writer.write("1\n");
        writer.write("2\n");
        writer.write("3\n");
        writer.close();
        
        byte[] data = destination.getBytes();
        Source source = new ByteArraySource(data);
        BufferedReader reader = new BufferedReader(new InputStreamReader(source.getStream()));
        assertTrue(reader.readLine().trim().equals("test"));
        assertTrue(reader.readLine().trim().equals("1"));
        assertTrue(reader.readLine().trim().equals("2"));
        assertTrue(reader.readLine().trim().equals("3"));
        reader.close();
        
    }

	@Override
	protected String getDestinationFactoryImpl() {
		return ByteArrayDestinationFactoryImpl.class.getName();
	}

	@Override
	protected String getSourceFactoryImpl() {
		return ByteArraySourceFactoryImpl.class.getName();
	}

}
