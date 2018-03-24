package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import javax.ejb.CreateException;
import javax.ejb.Remote;
import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by wohlgemuth on 10/18/16.
 */
public interface DatabaseJMXFacade {
    /**
     * @return
     * @jmx.managed-operation description="database"
     */
    String getDatabase();

    /**
     * @param database
     * @jmx.managed-operation description="database"
     */
    void setDatabase(String database);

    /**
     * @jmx.managed-operation description="server"
     */
    String getDatabaseServer();

    /**
     * @param databaseServer
     * @jmx.managed-operation description="server"
     */
    void setDatabaseServer(String databaseServer);

    /**
     * @return
     * @jmx.managed-operation description="password"
     */
    String getDatabaseServerPassword();

    /**
     * @param databaseServerPassword
     * @jmx.managed-operation description="password"
     */
    void setDatabaseServerPassword(String databaseServerPassword);

    Properties getProperties();

    void addProcessingColumn(String column) throws SQLException;

    void dropProcessingColumn(String column) throws SQLException;

    public Properties createProperties(String column);
}