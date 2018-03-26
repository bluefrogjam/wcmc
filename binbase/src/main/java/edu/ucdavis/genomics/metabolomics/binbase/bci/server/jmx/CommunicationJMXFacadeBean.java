package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import edu.ucdavis.genomics.metabolomics.binbase.cluster.exception.ClusterException;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.jmx.ClusterConfiglJMXMBean;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.management.*;
import java.io.Serializable;

/**
 * Created by wohlgemuth on 10/18/16.
 */
@Stateless
@Remote(CommunicationJMXFacade.class)
public class CommunicationJMXFacadeBean implements CommunicationJMXFacade, Serializable {

    @Override
    public String getFromAdress() {
        sync();
        return bean.getFromAdress();
    }

    @Override
    public void setFromAdress(String fromAdress) {
        sync();
        bean.setFromAdress(fromAdress);
    }

    @Override
    public String getPassword() {
        sync();
        return bean.getPassword();
    }

    @Override
    public void setPassword(String password) {
        sync();
        bean.setPassword(password);
    }

    @Override
    public String getSmtpPort() {
        sync();
        return bean.getSmtpPort();
    }

    @Override
    public void setSmtpPort(String smtpPort) {
        sync();
        bean.setSmtpPort(smtpPort);
    }

    @Override
    public String getSmtpServer() {
        sync();
        return bean.getSmtpServer();
    }

    @Override
    public void setSmtpServer(String smtpServer) {
        sync();
        bean.setSmtpServer(smtpServer);
    }

    @Override
    public String getUsername() {
        sync();
        return bean.getUsername();
    }

    @Override
    public void setUsername(String username) {
        sync();
        bean.setUsername(username);
    }

    private CommunicationJMXMBean bean;

    protected void sync() {
        MBeanServer server = MBeanServerFactory.findMBeanServer(null).get(0);

        try {
            bean =  MBeanServerInvocationHandler
                    .newProxyInstance(server, new ObjectName(
                                    "binbase:service=Communication"),
                            CommunicationJMXMBean.class, false);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }
}
