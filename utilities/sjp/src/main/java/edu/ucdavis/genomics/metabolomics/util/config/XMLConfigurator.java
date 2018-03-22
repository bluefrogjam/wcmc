/*
 * Created on Aug 6, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.util.config;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.config.xml.XMLConfigable;
import edu.ucdavis.genomics.metabolomics.util.config.xml.XmlHandling;
import edu.ucdavis.genomics.metabolomics.util.io.source.FileSource;
import edu.ucdavis.genomics.metabolomics.util.io.source.ResourceSource;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.io.source.SourceFactory;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author wohlgemuth
 * @version Aug 6, 2003 <br>
 *          BinBaseDatabase
 * @description
 */
public class XMLConfigurator {

    private static XMLConfigurator instance;

    Properties properties = new Properties();

    public void reset() {
        logger.debug("resetting configuration!");
        root = new Element("root");
        properties = new Properties();

    }

    public void showTree(final OutputStream out) {
        try {
            new XMLOutputter(Format.getPrettyFormat()).output(root, out);
            out.flush();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * creates an instance
     *
     * @return
     */
    public static XMLConfigurator getInstance() {
        if (instance == null) {
            logger.debug("create new instance");
            instance = new XMLConfigurator();
        }
        return instance;
    }

    /**
     * creates an isntance of this source or gets the instance and extends it by
     * this source
     *
     * @param source
     * @return
     */
    public static XMLConfigurator getInstance(final Source source) {
        if (instance == null) {
            logger.debug("create new instance");
            instance = new XMLConfigurator(source);
        } else {
            logger.debug("using existing instance");
        }
        try {
            instance.addConfiguration(source);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    /**
     * forces an instance unique for this configuration
     *
     * @param source
     * @return
     */
    public static XMLConfigurator forceInstance(final Source source) {
        final XMLConfigurator myInstance = new XMLConfigurator(source);

        return myInstance;
    }

    public void destroy() {
        instance = null;
    }

    /**
     * used
     */
    public static final String FACTORY_LOADER = XMLConfigurator.class.getName()
            + ".factory";

    /**
     *
     */
    public static final String SOURCE_LOADER = XMLConfigurator.class.getName()
            + ".source";

    /**
     * @uml.property name="root"
     * @uml.associationEnd elementType="edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.util.config.xml.XMLConfigable"
     * multiplicity="(0 -1)"
     */
    Element root = null;

    /**
     * @uml.property name="logger"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    private static Logger logger = LoggerFactory.getLogger(XMLConfigurator.class);

    /**
     * Creates a new XMLConfigurator object.
     */
    private XMLConfigurator() {
        try {
            String factory = System.getProperties().getProperty(FACTORY_LOADER);
            String source = System.getProperties().getProperty(SOURCE_LOADER);

            logger.debug("using factory: " + factory);
            logger.debug("using source: " + source);

            if (factory != null) {
                try {
                    logger.info("factory is not null");
                    factory = factory.trim();
                    source = source.trim();

                    init(SourceFactory.newInstance(factory)
                            .createSource(source));
                } catch (final Exception e) {
                    logger.debug(e.getMessage(), e);
                    try {
                        init(new FileSource(new File("config/configurator.xml")));
                    } catch (final Exception ex) {
                        logger.debug(ex.getMessage(), ex);
                        init(new ResourceSource("/config/configurator.xml"));
                    }
                }
            } else if (new File("config/configurator.xml").exists()) {
                logger
                        .info("using config file from file system: "
                                + new File("config/configurator.xml")
                                .getAbsolutePath());
                init(new FileSource(new File("config/configurator.xml")));
            } else if (new File(System.getProperty("user.home")
                    + "/.config/applicationServer.xml").exists()) {
                logger.info("using config file from home config directoy: "
                        + new File(System.getProperty("user.home")
                        + "/.config/applicationServer.xml"));
                init(new FileSource(new File(System.getProperty("user.home")
                        + "/.config/applicationServer.xml")));
            } else {
                logger.info("using config file from classpath");
                init(new ResourceSource("/config/configurator.xml"));
            }
        } catch (final Exception e) {
            logger
                    .error("no default file found, so you need todo it your self");
        }
    }

    /**
     * Creates a new XMLConfigurator object.
     * <p>
     * DOCUMENT ME!
     */
    private XMLConfigurator(final Source source) {
        try {
            init(source);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * returns the given properties
     *
     * @return
     */
    public Properties getProperties() {
        if (properties == null) {
            return new Properties();
        }
        return properties;
    }

    /**
     * gibt die konfigurationsmethode zur?ck
     *
     * @return
     * @throws ConfigurationException
     * @version Aug 6, 2003
     * @author wohlgemuth <br>
     */
    public synchronized XMLConfigable getXMLConfigable(final String target) {
        try {

            XMLConfigable myconfig = findConfig(target);

            if (myconfig == null) {
                logger
                        .info("target was not found in the registered configurations adding possible other configurations and try again");

                addConfiguration(new ResourceSource(
                        "/config/configurator.xml"));
                myconfig = findConfig(target);

                if (myconfig != null) {
                    logger.info("caching from class path: " + target);
                    return myconfig;
                } else {
                    throw new Exception("target not found: " + target);
                }
            } else {
                return myconfig;
            }

        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    @SuppressWarnings("unchecked")
    protected synchronized XMLConfigable findConfig(final String target)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        XMLConfigable myconfig = null;

        logger.info("searching configurations for target: " + target);

        final List<Element> elements = root.getChildren("target");

        if (elements != null) {
            final Iterator<Element> it = elements.iterator();

            while (it.hasNext() == true) {
                final Element e = it.next();

                logger.info("current target: " + e.getAttributeValue("name"));

                if (e.getAttributeValue("name").equals(target)) {
                    myconfig = (XMLConfigable) Class.forName(
                            e.getChild("class").getAttributeValue("name")
                                    .trim()).newInstance();

                    myconfig.setParameter(e.getChild("parameter"));
                    myconfig.setConfigProvider(this);

                    logger.info("==> found!");
                    return myconfig;
                }
            }
        } else {
            logger.warn("no targets found!");
        }
        return myconfig;
    }

    /**
     * inititialisiert den configurator
     *
     * @return
     * @throws Exception
     * @version Aug 7, 2003
     * @author wohlgemuth <br>
     */
    protected void init(final Source source) throws Exception {
        this.addConfiguration(source);
    }

    /**
     * adds another configuration file to this configurator, usefull if you
     * dynamicly want to add other files, because yi
     *
     * @param source
     * @throws Exception
     * @author wohlgemuth
     * @version Aug 1, 2006
     */
    public void addConfiguration(final Source source) throws Exception {
        if (source.exist()) {
            logger.info(" -> adding source: " + source.getSourceName() + " - "
                    + source.getClass() + " - " + source.toString());
            final Element root = XmlHandling.readXml(source);
            addConfiguration(root);
        } else {
            logger.warn("source doesn't exist!");
        }
    }

    public void addConfigurationPropertie(final String name, final String value) {
        addConfigurationPropertie(name, value, false);
    }

    public void addConfigurationPropertie(final String name,
                                          final String value, final boolean forSystem) {
        logger.debug("adding custom propertie: " + name + " with value: "
                + value);
        properties.put(name, value);
        if (forSystem) {
            System.getProperties().put(name, value);
        }
    }

    /**
     * configuration in form of an element
     *
     * @param root
     */
    @SuppressWarnings("unchecked")
    public void addConfiguration(final Element root) throws Exception {
        logger.info("add configurations: " + root.getName());

        if (this.root == null) {
            this.root = new Element("root");
        }

        String s = new XMLOutputter(Format.getPrettyFormat())
                .outputString(root);

        logger.debug("\n" + s + "\n");

        List<Element> elements = root.getChildren("parameter");

        for (int i = 0; i < elements.size(); i++) {

            final List<Element> list = (elements.get(i)).getChildren("param");
            final Iterator<Element> it = list.iterator();

            while (it.hasNext()) {
                final Element e = it.next();
                logger.debug("setting property: name = \""
                        + e.getAttributeValue("name") + "\" value: \""
                        + e.getAttributeValue("value") + "\"");
                properties.setProperty(e.getAttributeValue("name").trim(), e
                        .getAttributeValue("value").trim());

                if (e.getAttribute("public") != null) {
                    if (e.getAttribute("public").getBooleanValue()) {
                        logger.debug("export parameter to system: "
                                + e.getAttributeValue("name") + " - "
                                + e.getAttributeValue("value"));
                        System.setProperty(e.getAttributeValue("name").trim(),
                                e.getAttributeValue("value").trim());
                    }
                }
                it.remove();
                this.root.addContent(e.detach());
            }

        }

        elements = root.getChildren("target");

        if (elements != null) {
            final Iterator it = elements.iterator();

            while (it.hasNext()) {
                final Element e = (Element) it.next();
                logger.debug("adding target: " + e.getAttributeValue("name"));
                it.remove();

                final Iterator itx = this.root.getChildren("target").iterator();

                while (itx.hasNext()) {
                    final Element ex = (Element) itx.next();

                    if (ex.getAttributeValue("name").equals(
                            e.getAttributeValue("name"))) {
                        logger.debug("remove old config target: "
                                + ex.getAttributeValue("name"));
                        itx.remove();
                    }
                }

                this.root.addContent(e.detach());
            }
        }
    }
}
