package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import edu.ucdavis.genomics.metabolomics.binbase.bci.server.types.DSL;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.util.SchedulingObject;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Created by wohlgemuth on 10/18/16.
 */
public interface StatusJMXMBean extends
        javax.management.MBeanRegistration {
    /**
     * @return
     * @throws BinBaseException
     * @jmx.managed-operation description = "a list of email addresses to notify
     * users"
     * @author wohlgemuth
     * @version Apr 21, 2006
     */
    Collection getNotificationsAdresses() throws BinBaseException;

    /**
     * @param string
     * @jmx.managed-operation description = "adds an email for notifications"
     * @author wohlgemuth
     * @version Jul 16, 2006
     */
    void addNotificationAdress(String emailAdress);

    /**
     * @param string
     * @jmx.managed-operation description = "removes an email address for
     * notifications"
     * @author wohlgemuth
     * @version Jul 16, 2006
     */
    void removeNotificationAdress(String emailAdresses);

    /**
     * @return
     * @throws BinBaseException
     * @jmx.managed-operation description = "returns the dsl queue"
     * @author wohlgemuth
     */
    Collection<DSL> listDSLJobs() throws BinBaseException;

    /**
     * @return
     * @throws BinBaseException
     * @jmx.managed-operation description = "returns the export queue"
     * @author wohlgemuth
     * @version Apr 21, 2006
     */
    Collection<SchedulingObject> listAllJobs() throws BinBaseException;

    /**
     * generates a map containing the count of different jobs in the system
     *
     * @return
     * @throws BinBaseException
     * @jmx.managed-operation description = "returns the scheduling jobs"
     * @author wohlgemuth
     */
    Map listJobCounts() throws BinBaseException;

    /**
     * @return
     * @throws BinBaseException
     * @jmx.managed-operation description = "returns the export queue"
     * @author wohlgemuth
     * @version Apr 21, 2006
     */
    long getTotalJobCount() throws BinBaseException;

    /**
     * @return
     * @throws BinBaseException
     * @jmx.managed-operation description = "clears the queue"
     * @author wohlgemuth
     * @version Apr 21, 2006
     */
    void clearQueue() throws BinBaseException;
}
