package edu.ucdavis.genomics.metabolomics.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * simple method to load properties into system.properties
 *
 * @author wohlgemuth
 */
public class PropertySetter {

    private static Logger logger = LoggerFactory.getLogger(PropertySetter.class);

    public static Properties setPropertiesToSystem(String fileName) throws FileNotFoundException, IOException {
        return setPropertiesToSystem(new FileInputStream(fileName));
    }

    @SuppressWarnings("unchecked")
    public static Properties setPropertiesToSystem(InputStream in) throws IOException {
        Properties p = new Properties();
        p.load(in);

        Enumeration enums = p.keys();

        while (enums.hasMoreElements()) {
            String key = enums.nextElement().toString();
            if (System.getProperty(key) == null) {
                logger.debug("setting property: " + key);
                System.setProperty(key, p.getProperty(key));
            } else {
                logger.debug("property already defiend, skipping: " + key);
            }

        }

        return p;
    }
}
