/*
 * Created on Nov 10, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.io.dest;

import edu.ucdavis.genomics.metabolomics.util.io.source.FileSourceFactoryImpl;

/**
 * runs the dest using a fiel as destination
 * @author wohlgemuth
 * @version Nov 10, 2005
 *
 */
public class FileDestinationTest extends AbstractDestinationTest{
    
    public FileDestinationTest() {
        super();
    }

	@Override
	protected String getDestinationFactoryImpl() {
		return FileDestinationFactoryImpl.class.getName();
	}

	@Override
	protected String getSourceFactoryImpl() {
		return FileSourceFactoryImpl.class.getName();
	}
    

}
