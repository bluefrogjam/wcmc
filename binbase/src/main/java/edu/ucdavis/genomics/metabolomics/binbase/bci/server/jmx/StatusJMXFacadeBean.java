package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import edu.ucdavis.genomics.metabolomics.binbase.bci.server.types.DSL;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.util.SchedulingObject;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.management.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Created by wohlgemuth on 10/18/16.
 */
@Remote(StatusJMXFacade.class)
@Stateless
public class StatusJMXFacadeBean implements StatusJMXFacade,Serializable{
    @Override
    public Collection getNotificationsAdresses() throws BinBaseException {
        sync();
        return bean.getNotificationsAdresses();
    }

    @Override
    public void addNotificationAdress(String emailAdress) {
        sync();
        bean.addNotificationAdress(emailAdress);
    }

    @Override
    public void removeNotificationAdress(String emailAdresses) {
        sync();
        bean.removeNotificationAdress(emailAdresses);
    }

    @Override
    public Collection<DSL> listDSLJobs() throws BinBaseException {
        sync();
        return bean.listDSLJobs();
    }


    @Override
    public Collection<SchedulingObject> listAllJobs() throws BinBaseException {
        sync();
        return bean.listAllJobs();
    }

    @Override
    public Map listJobCounts() throws BinBaseException {
        sync();
        return bean.listJobCounts();
    }

    @Override
    public long getTotalJobCount() throws BinBaseException {
        sync();
        return bean.getTotalJobCount();
    }

    @Override
    public void clearQueue() throws BinBaseException {
        sync();
        bean.clearQueue();
    }

    private StatusJMXMBean bean;


    protected void sync() {
        MBeanServer server = MBeanServerFactory.findMBeanServer(null).get(0);

        try {
            bean = MBeanServerInvocationHandler
                    .newProxyInstance(server, new ObjectName(
                                    "binbase:service=Status"),
                            StatusJMXMBean.class, false);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }
}

