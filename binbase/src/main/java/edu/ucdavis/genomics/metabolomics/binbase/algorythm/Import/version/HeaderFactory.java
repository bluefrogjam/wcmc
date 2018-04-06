/*;
 * Created on Jun 10, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.version;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import edu.ucdavis.genomics.metabolomics.util.config.xml.XmlHandling;
import org.slf4j.Logger;
import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.config.Configable;
import edu.ucdavis.genomics.metabolomics.util.config.xml.XMLConfigable;

/**
 * @author wohlgemuth build automaticly the needed versions handler from the
 *         configuration
 */
public class HeaderFactory {
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * DOCUMENT ME!
     */
    private static HeaderFactory instance;

    /**
     * DOCUMENT ME!
     */
    private XMLConfigable config = Configable.CONFIG;

    /**
     * @return
     */
    public static HeaderFactory create() {
        if (instance == null) {
            instance = new HeaderFactory();
        }

        return instance;
    }

    /**
     * creates the needed header and sets the header properties
     *
     * @return
     * @throws ConfigurationException
     */
    @SuppressWarnings("unchecked")
    public synchronized Header
    getHeader(final String[] headerLine) throws ConfigurationException {
        try {

            List<Element> elements = (config.getElement("header").getChildren("version"));

            Header header = null;

            logger.debug("defined list of headers: " + elements.size());

            for (Element element : elements) {

                logger.info("current configuration:");
                XmlHandling.logXML(logger, element);

                List<Element> headerList = element.getChildren("entry");
                String[] configurationFields = new String[headerList.size()];

                logger.debug("required fields: " + configurationFields.length);

                for (int i = 0; i < configurationFields.length; i++) {
                    configurationFields[i] = headerList.get(i).getAttributeValue("file");

                    if (configurationFields[i] == null) {
                        throw new ConfigurationException("was not able to find the 'file' attribute in the configuration element!");
                    }
                }

                logger.debug("header fields: " + headerList.size());

                Collection<String> found = new Vector<String>();
                for (int i = 0; i < headerLine.length; i++) {
                    for (int x = 0; x < configurationFields.length; x++) {
                        if (configurationFields[x].equals(headerLine[i])) {
                            found.add(configurationFields[x]);
                        }
                    }
                }

                logger.debug("found: " + found.size());


                if (found.size() == configurationFields.length) {
                    logger.debug("found implementation");
                    header = new Header();
                    header.setElement(element);
                } else {
                    logger.debug("found no implementation");

                    logger.debug("but I did find the following fields");
                    for (String s : found) {
                        logger.debug(s);
                    }
                }
            }
            if (header == null) {
                logger.error("no matching configuration found in config file");

                logger.error("defined headers:");
                for (String s : headerLine) {
                    logger.error(s);
                }

                logger.error("registered headers were:");
                for (Element e : elements) {
                    XmlHandling.logXML(logger, e);
                }

                throw new ConfigurationException("no configuration found for this file!");
            }
            return header;
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }
}
