/*
 * Created on Nov 10, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.io.dest;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * defines a file as destination
 * @author wohlgemuth
 * @version Nov 10, 2005
 *
 */
public class FileDestination implements Destination{
    File file;

    private Logger logger = LoggerFactory.getLogger(getClass());
    private String dir;
    
    /**
     * property to get the destination directory
     */
    public static String DIR_PROPERTY = "DIR";
    
    public FileDestination() {
        super();
    }

    public FileDestination(String dir) {
        super();
        logger.info("using directory: " + dir);
        this.dir = dir;
    }

    /**
     * 
     * @author wohlgemuth
     * @version Nov 10, 2005
     * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.dest.Destination#getOutputStream()
     */
    public OutputStream getOutputStream() throws IOException {
    	if(file == null){
    		throw new IOException("no file was set!");
    	}
        return new FileOutputStream(file);
    }

    /**
     * identifier must be a file or is used as filename
     * @author wohlgemuth
     * @version Nov 10, 2005
     * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.dest.Destination#setIdentifier(Object)
     */
    public void setIdentifier(Object o) throws ConfigurationException {
        if( o instanceof File){
            file = (File) o;
            return;
        }
        else if(o instanceof String){
            if(dir != null){
                file = new File(this.dir + File.separator + o);
                logger.info("final filename is: " + file.getAbsolutePath());
                if(file.getParentFile().exists() == false){
                	logger.debug("create directory strucure since parent dircetory does not exist");
                	file.getParentFile().mkdirs();
                }
                if(file.isDirectory()){
                    throw new ConfigurationException("destination cannot be a directory: " + file);
                }
            }
            else{
                file = new File((String) o);
            }
            return;
        }
        
        throw new ConfigurationException("object is from wrong type as identifier");
    }

    /**
     * the map must contain the directory, if we want to copy the file to a specific directory
     * @author wohlgemuth
     * @version Nov 10, 2005
     * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.dest.Destination#configure(Map)
     */
    public void configure(Map<?,?> p) throws ConfigurationException {
        this.dir = (String) p.get(DIR_PROPERTY);
    }

    /**
     * returns the internal file object
     * @author wohlgemuth
     * @version Nov 10, 2005
     * @return
     */
    public File getFile(){
        return this.file;
    }
}
