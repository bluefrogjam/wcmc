package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by wohlgemuth on 10/18/16.
 */
public interface DatabaseJMXMBean extends
        javax.management.MBeanRegistration{
    /**
     * @jmx.managed-operation description="database"
     * @return
     */
    String getDatabase();

    /**
     * @jmx.managed-operation description="database"
     * @param database
     */
    void setDatabase(String database);

    /**
     * @jmx.managed-operation description="server"
     */
    String getDatabaseServer();

    /**
     * @jmx.managed-operation description="server"
     * @param databaseServer
     */
    void setDatabaseServer(String databaseServer);

    /**
     * @jmx.managed-operation description="password"
     * @return
     */
    String getDatabaseServerPassword();

    /**
     * @jmx.managed-operation description="password"
     * @param databaseServerPassword
     */
    void setDatabaseServerPassword(String databaseServerPassword);

    Properties createProperties(String column);

    Properties getProperties();

    /**
     * adds a new database
     * @param databaseName
     */
    void addProcessingColumn(String databaseName) throws SQLException;

    void dropProcessingColumn(String column) throws SQLException;
}
