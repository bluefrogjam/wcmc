package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching;

import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.AlgorythmHandler;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.anotation.BasicFilter;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.anotation.Filter;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.anotation.MatchingException;
import edu.ucdavis.genomics.metabolomics.util.SQLable;
import edu.ucdavis.genomics.metabolomics.util.config.Configable;

/**
 * a highly customizeable algorithmhandler where you can add as many filters as
 * you like. The big advantage to the old implementation is that the complete
 * complexity is capseled in a couple f interface instead of one gigantic class
 * <p>
 * it self is a filter so that subclasses of this can be used as filters you
 * just need to override the config method
 *
 * @author wohlgemuth
 */
public class StandardAlgorithmHandler extends BasicFilter implements
        AlgorythmHandler {

    /**
     * contains all registered filters
     */
    private Collection<Filter> filters;

    private Connection c = null;

    private Element config = null;

    /**
     * loads the internal vector with filter objects and initializes the system
     *
     * @throws Exception
     */
    public StandardAlgorithmHandler() {
        filters = new Vector<Filter>();
        configHandler();
    }

    /**
     * provides the configurations of the handler
     */
    @SuppressWarnings("unchecked")
    protected void configHandler() {

        Collection<Element> list = Configable.CONFIG
                .getElement("class.filters").getChildren("filter");
        Iterator<Element> it = list.iterator();

        while (it.hasNext()) {
            try {
                addFilter(it.next().getAttributeValue("class"));
            } catch (Exception e) {
                logger.error("carefull could not initilize the current filter!", e);
            }
        }
    }

    /**
     * adds a filter
     *
     * @param className
     * @throws Exception
     */
    public void addFilter(String className) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("initialize filter: " + className);
        }
        Filter filter = (Filter) Class.forName(className).newInstance();
        addFilter(filter);
    }

    /**
     * adds a filter
     *
     * @param filter
     */
    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    /**
     * sets the configuration and forwards to the actual compareTo Method which
     * than calls the compare method. Design mistake but not possible to change
     * right now
     */
    public final boolean compare(Map<String, Object> bin,
                                 Map<String, Object> spectra, Element configuration) {
        this.config = configuration;

        try {
            return this.compareTo(bin, spectra);
        } catch (MatchingException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public Connection getConnection() {
        return c;
    }

    /**
     * sets the connection to all the filters
     */
    public void setConnection(Connection connection) {
        if (logger.isDebugEnabled()) {
            logger.debug("set connection for handler...");
        }
        this.c = connection;

        Iterator<Filter> it = filters.iterator();

        while (it.hasNext()) {
            Filter filter = it.next();

            if (filter instanceof SQLable) {
                if (logger.isDebugEnabled()) {
                    logger.debug("assigning SQL connection to filter: " + filter);
                }
                ((SQLable) filter).setConnection(this.getConnection());
            }
        }
    }

    /**
     * the filter method for the filter or the actual compare action of the
     * algorythm handler
     */
    @Override
    protected boolean compare(Map<String, Object> bin,
                              Map<String, Object> spectra) throws MatchingException {
        Iterator<Filter> it = filters.iterator();

        int binId = Integer.parseInt(bin.get("bin_id").toString());
        int spectraId = Integer.parseInt(spectra.get("spectra_id").toString());

        if (filters.isEmpty()) {
            return false;
        }

        if (isDebugEnabled()) {
                logger.debug("working on spectra: " + spectra.get("spectra_id"));
                logger.debug("current bin id is: " + bin.get("bin_id"));
        }
        while (it.hasNext()) {
            Filter filter = it.next();

            if (logger.isDebugEnabled()) {
                logger.debug("running filter: " + filter);
            }

            try {
                if (!filter.compareTo(bin, spectra)) {
                    if (isDebugEnabled()) {
                        logger.debug("rejected at filter: " + filter);
                        logger.debug("reason: " + filter.getReasonForRejection());

                        getDiagnosticsService().diagnosticActionFailed(spectraId,
                                binId, this.getClass(), "standard filter action",
                                "massspec was rejected by called filter",
                                new Object[]{filter.getClass().getSimpleName()});
                    }
                    return false;

                } else {
                    if (logger.isDebugEnabled()) {
                        getDiagnosticsService().diagnosticActionSuccess(spectraId,
                                binId, this.getClass(), "standard filter action",
                                "massspec was accepted by calling filter",
                                new Object[]{filter.getClass().getSimpleName()});

                        logger.debug("accepted at filter: " + filter);
                    }
                }
            } catch (MatchingException e) {
                logger.error(e.getMessage(), e);
            }
        }

        if (isDebugEnabled()) {
            logger.debug("massspec passed all registered filters, found: "
                    + bin.get("name") + " with bin id " + bin.get("bin_id"));


            getDiagnosticsService().diagnosticActionSuccess(spectraId, binId,
                    this.getClass(), "standard filter action",
                    "massspec passed all registered filters", new Object[]{});
        }
        return true;
    }

    protected Element getConfig() {
        return config;
    }
}
