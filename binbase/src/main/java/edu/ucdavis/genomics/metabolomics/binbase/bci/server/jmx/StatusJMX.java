/*
 * Created on Apr 21, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import edu.ucdavis.genomics.metabolomics.binbase.bci.server.types.DSL;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.ejb.client.Configurator;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.exception.ClusterException;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.util.JBosssPropertyHolder;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.util.JMSScheduler;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.util.SchedulingObject;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import org.slf4j.Logger;

import javax.annotation.Resource;
import javax.jms.Queue;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


public class StatusJMX implements StatusJMXMBean {

    /**
     * to notify users over events
     */
    private Collection<String> notificationsAdresses = new Vector<>();

    private Logger logger = LoggerFactory.getLogger(getClass());

    public StatusJMX() {
        super();
    }

    @Override
    public Collection getNotificationsAdresses() throws BinBaseException {
        return notificationsAdresses;
    }

    @Override
    public void addNotificationAdress(String emailAdress) {

        if (!notificationsAdresses.contains(emailAdress)) {
            notificationsAdresses.add(emailAdress);
            this.store();
        }
    }

    @Override
    public void removeNotificationAdress(String emailAdresses) {
        notificationsAdresses.remove(emailAdresses);
        this.store();
    }

    @Override
    public Collection<DSL> listDSLJobs() throws BinBaseException {
        return listAllJobs().stream().filter(message -> message.getContent() instanceof DSL).map(message -> (DSL) message.getContent()).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Collection<SchedulingObject> listAllJobs() throws BinBaseException {
        try {
            return Configurator.getSchedulingQueue(false).getMessages();
        } catch (Exception e) {
            throw new BinBaseException(e);
        }
    }

    @Override
    public Map listJobCounts() throws BinBaseException {
        try {
            Collection jobs = listAllJobs();
            Map<String, Integer> result = new HashMap<>();

            for (Object o : jobs) {
                if (o != null) {
                    String className = o.getClass().getName();

                    if (!result.containsKey(className)) {
                        result.put(className, 0);
                    }
                    result.put(className, result.get(className) + 1);
                }
            }

            return result;
        } catch (Exception e) {
            throw new BinBaseException(e);
        }
    }

    @Override
    public long getTotalJobCount() throws BinBaseException {
        try {
            return Configurator.getSchedulingQueue(false).messageCount();
        } catch (Exception e) {
            throw new BinBaseException(e);
        }
    }

    @Override
    public void clearQueue() throws BinBaseException {
        try {
            Configurator.getSchedulingQueue(false).clear();
        } catch (Exception e) {
            throw new BinBaseException(e);
        }
    }

    public ObjectName preRegister(MBeanServer server, ObjectName name)
            throws Exception {
        return null;
    }

    public void postRegister(Boolean registrationDone) {
        try {
            File file = JBosssPropertyHolder.getPropertyFile(getClass());
            if (!file.exists()) {
                return;
            }

            ObjectInputStream in = new ObjectInputStream(new FileInputStream(
                    file));
            this.notificationsAdresses = (Collection<String>) in.readObject();

        } catch (Exception e) {
            logger.error("postRegister(Boolean)", e); //$NON-NLS-1$

        }
    }

    public void preDeregister() throws Exception {
        store();
    }

    public void postDeregister() {
    }

    private void store() {
        try {
            File file = JBosssPropertyHolder.getPropertyFile(getClass());

            ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(file));
            out.writeObject(this.getNotificationsAdresses());
            out.flush();
            out.close();

        } catch (Exception e) {
            logger.error(e.getMessage(), e); //$NON-NLS-1$
        }
    }

}
