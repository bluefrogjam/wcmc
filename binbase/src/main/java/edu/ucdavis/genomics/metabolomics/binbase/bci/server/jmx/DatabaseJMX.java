package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import javax.ejb.CreateException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.NamingException;

import edu.ucdavis.genomics.metabolomics.binbase.cluster.ejb.client.Configurator;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.util.JBosssPropertyHolder;
import edu.ucdavis.genomics.metabolomics.util.database.ConnectionFactory;
import edu.ucdavis.genomics.metabolomics.util.database.DatabaseUtilities;
import edu.ucdavis.genomics.metabolomics.util.database.DriverUtilities;
import org.apache.ibatis.jdbc.SqlRunner;
import org.slf4j.Logger;

import edu.ucdavis.genomics.metabolomics.binbase.util.net.DetermineIpForInterface;
import edu.ucdavis.genomics.metabolomics.binbase.util.net.InterfaceNotFoundException;
import edu.ucdavis.genomics.metabolomics.util.database.SimpleConnectionFactory;


public class DatabaseJMX implements DatabaseJMXMBean {
	private Logger logger = LoggerFactory.getLogger(getClass());

    private String databaseServer = "127.0.0.1";

    private String databaseServerPassword =  "password";

    private String database = "binbase";

	private void store() {
		try {
			File file = JBosssPropertyHolder.getPropertyFile(getClass());

			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(file));

			out.writeObject(createProperties());
			out.flush();
			out.close();

		} catch (Exception e) {
			logger.error(e.getMessage(), e); //$NON-NLS-1$
		}
	}

	public void postDeregister() {

	}

	public void postRegister(Boolean arg0) {
		try {
			File file = JBosssPropertyHolder.getPropertyFile(getClass());
			if (!file.exists()) {
				return;
			}

			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					file));
			Properties p = (Properties) in.readObject();

			if(p.getProperty(SimpleConnectionFactory.KEY_DATABASE_PROPERTIE) != null){
				this.database = p.getProperty(SimpleConnectionFactory.KEY_DATABASE_PROPERTIE);
			}
			if(p.getProperty(SimpleConnectionFactory.KEY_PASSWORD_PROPERTIE) != null){
				this.databaseServerPassword = p.getProperty(SimpleConnectionFactory.KEY_PASSWORD_PROPERTIE);
			}
			if(p.getProperty(SimpleConnectionFactory.KEY_HOST_PROPERTIE) != null){
				this.databaseServer = p.getProperty(SimpleConnectionFactory.KEY_HOST_PROPERTIE);
			}


        } catch (Exception e) {
            logger.debug("postRegister(Boolean)", e); //$NON-NLS-1$

        }
    }

    /**
     * @jmx.managed-operation description="server"
     */
    public Properties createProperties() {
        Properties p = new Properties();
        p.setProperty(SimpleConnectionFactory.KEY_DATABASE_PROPERTIE,
                getDatabase());
        p.setProperty(SimpleConnectionFactory.KEY_PASSWORD_PROPERTIE,
                getDatabaseServerPassword());
        p.setProperty(SimpleConnectionFactory.KEY_TYPE_PROPERTIE,
                "" + DriverUtilities.POSTGRES);
        p.setProperty(SimpleConnectionFactory.KEY_HOST_PROPERTIE,
                getDatabaseServer());

        return p;
    }

    @Override
    public Properties createProperties(String column) {
        Properties p = createProperties();

        p.setProperty(SimpleConnectionFactory.KEY_USERNAME_PROPERTIE, column);
        return p;
    }

    public Properties getProperties() {
        return createProperties();
    }

    public void preDeregister() throws Exception {
    }

    public ObjectName preRegister(MBeanServer arg0, ObjectName arg1)
            throws Exception {
        return null;
    }

    @Override
    public String getDatabase() {
        return database;
    }

    @Override
    public void setDatabase(String database) {
        this.database = database;
        store();
    }

    @Override
    public String getDatabaseServer() {
        return databaseServer;
    }

    @Override
    public void setDatabaseServer(String databaseServer) {
        this.databaseServer = databaseServer;
        store();
    }

    @Override
    public String getDatabaseServerPassword() {
        return databaseServerPassword;
    }

    @Override
    public void setDatabaseServerPassword(String databaseServerPassword) {
        this.databaseServerPassword = databaseServerPassword;
        store();
    }

    /**
     * adds another database
     *
     * @param column
     */
    @Override
    public void addProcessingColumn(String column) throws SQLException {
        logger.info("adding processing column: " + column);
        try {
            ConnectionFactory factory = ConnectionFactory.createFactory();
            factory.setProperties(createProperties(column));

            Connection connection = factory.getConnection();
            edu.ucdavis.genomics.metabolomics.binbase.bci.Configurator.getImportServiceLocal().addDatabase(column);

            SqlRunner runner = new SqlRunner(connection);

            String sqlToRun = "create schema " + column + ";\n " +
                    " set search_path = " + column + ";\n" +
                    new Scanner(getClass().getClassLoader().getResourceAsStream("/binbase.sql")).useDelimiter("\\Z").next();

            logger.info("running file: \n\n"+sqlToRun + "\n\n");
            runner.run(sqlToRun);
            factory.close(connection);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new SQLException(e);
        }
    }

    @Override
    public void dropProcessingColumn(String column) throws SQLException {
        logger.info("dropping processing column: " + column);
        try {
            ConnectionFactory factory = ConnectionFactory.createFactory();
            factory.setProperties(createProperties(column));

            Connection connection = factory.getConnection();
            connection.setAutoCommit(false);
            connection.createStatement().execute("drop schema IF EXISTS " + column + " CASCADE");
            connection.commit();
            edu.ucdavis.genomics.metabolomics.binbase.bci.Configurator.getImportServiceLocal().removeDatabase(column);
            factory.close(connection);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new SQLException(e);
        }
    }
}
