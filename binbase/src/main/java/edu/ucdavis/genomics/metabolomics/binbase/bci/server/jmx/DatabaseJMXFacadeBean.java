package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import javax.ejb.CreateException;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.management.*;
import javax.naming.NamingException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by wohlgemuth on 10/18/16.
 */
@Stateless
@Remote(DatabaseJMXFacade.class)
public class DatabaseJMXFacadeBean implements DatabaseJMXFacade, Serializable {

    @Override
    public String getDatabase() {
        sync();
        return bean.getDatabase();
    }

    @Override
    public void setDatabase(String database) {
        sync();
        bean.setDatabase(database);
    }

    @Override
    public String getDatabaseServer() {
        sync();
        return bean.getDatabaseServer();
    }

    @Override
    public void setDatabaseServer(String databaseServer) {
        sync();
        bean.setDatabaseServer(databaseServer);
    }

    @Override
    public String getDatabaseServerPassword() {
        sync();
        return bean.getDatabaseServerPassword();
    }

    @Override
    public void setDatabaseServerPassword(String databaseServerPassword) {
        sync();
        bean.setDatabaseServerPassword(databaseServerPassword);
    }

    @Override
    public Properties getProperties() {
        sync();
        return bean.getProperties();
    }

    private DatabaseJMXMBean bean;

    protected void sync() {
        MBeanServer server = MBeanServerFactory.findMBeanServer(null).get(0);

        try {
            bean = MBeanServerInvocationHandler
                    .newProxyInstance(server, new ObjectName(
                                    "binbase:service=DatabaseConfiguration"),
                            DatabaseJMXMBean.class, false);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * this will init a complete binbase database schema on your configured sql database. Please be aware that this could be destructive and can destroy all your data!
     *
     * @param column
     */
    @Override
    public void addProcessingColumn(String column) throws SQLException {
        sync();
        bean.addProcessingColumn(column);
    }
    @Override
    public void dropProcessingColumn(String column) throws SQLException {
        sync();
        bean.dropProcessingColumn(column);
    }

    @Override
    public Properties createProperties(String column) {
        sync();
        return bean.createProperties(column);
    }


}
