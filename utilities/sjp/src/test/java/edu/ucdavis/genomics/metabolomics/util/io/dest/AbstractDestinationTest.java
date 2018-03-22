/*
 * Created on Nov 10, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.io.dest;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.io.source.SourceFactory;
import junit.framework.TestCase;

import java.io.*;

public abstract class AbstractDestinationTest extends TestCase {
    /**
     * destination for testing
     */
    private Destination destination;
    /**
     * source for testing
     */
    private Source source;

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        destination = null;
    }

    protected String getProperty(){
        return System.getProperty("java.io.tmpdir")+"/testFile";
    }
    /*
     * Test method for 'edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.dest.DatabaseDestination.getOutputStream()'
     */
    public void testGetOutputStream() throws IOException, ConfigurationException {
    	
        destination = DestinationFactory.newInstance(getDestinationFactoryImpl()).createDestination(getProperty());
        OutputStream stream = destination.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
        writer.write("test\n");
        writer.write("1\n");
        writer.write("2\n");
        writer.write("3\n");
        writer.close();
        
        source = SourceFactory.newInstance(getSourceFactoryImpl()).createSource(getProperty());
        System.out.println("using source: " + source);
        System.out.println("source exist: " + source.exist());
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(source.getStream()));
        assertTrue(reader.readLine().trim().equals("test"));
        assertTrue(reader.readLine().trim().equals("1"));
        assertTrue(reader.readLine().trim().equals("2"));
        assertTrue(reader.readLine().trim().equals("3"));
        reader.close();
        
    }

    /**
     * provides the destination factory
     * @return
     */
    protected abstract String getDestinationFactoryImpl();
    
    protected abstract String getSourceFactoryImpl();
    
    
}
