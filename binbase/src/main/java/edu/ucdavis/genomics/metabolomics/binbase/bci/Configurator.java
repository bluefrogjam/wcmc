package edu.ucdavis.genomics.metabolomics.binbase.bci;

import edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx.*;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.ejb.client.EjbClient;
import edu.ucdavis.genomics.metabolomics.binbase.minix.jmx.MiniXConfigurationJMXFacade;
import edu.ucdavis.genomics.metabolomics.binbase.minix.jmx.MiniXConfigurationJMXFacadeBean;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import java.rmi.RemoteException;

/**
 * access to the jmx configurations so that we can configure the server from the
 * client side
 *
 * @author wohlgemuth
 */
public class Configurator extends edu.ucdavis.genomics.metabolomics.binbase.cluster.ejb.client.Configurator {

    public static final String BCI_GLOBAL_NAME = "bci/bci-core";


    public static MiniXConfigurationJMXFacade getMinixConfiguration() throws RemoteException, CreateException, NamingException {
        return (MiniXConfigurationJMXFacade) EjbClient.getRemoteEjb(BCI_GLOBAL_NAME,MiniXConfigurationJMXFacadeBean.class);
    }


    public static MiniXConfigurationJMXFacade getMinixConfigurationLocal() throws RemoteException, CreateException, NamingException {
        return (MiniXConfigurationJMXFacade) EjbClient.getLocalEjb(BCI_GLOBAL_NAME,MiniXConfigurationJMXFacadeBean.class);
    }

    public static CommunicationJMXFacade getCommunicationService() throws RemoteException, CreateException, NamingException {
        return (CommunicationJMXFacade) EjbClient.getEjb(CommunicationJMXFacadeBean.class,BCI_GLOBAL_NAME);
    }

    public static CommunicationJMXFacade getCommunicationServiceLocal() throws RemoteException, CreateException, NamingException {
        return (CommunicationJMXFacade) EjbClient.getLocalEjb(BCI_GLOBAL_NAME,CommunicationJMXFacadeBean.class);
    }


    public static DatabaseJMXFacade getDatabaseService() throws RemoteException, CreateException, NamingException {
        return (DatabaseJMXFacade) EjbClient.getEjb( DatabaseJMXFacadeBean.class,BCI_GLOBAL_NAME);
    }
    public static DatabaseJMXFacade getDatabaseServiceLocal() throws RemoteException, CreateException, NamingException {
        return (DatabaseJMXFacade) EjbClient.getLocalEjb( BCI_GLOBAL_NAME,DatabaseJMXFacadeBean.class);
    }

    public static ExportJMXFacade getExportService() throws RemoteException, CreateException, NamingException {
        return (ExportJMXFacade) EjbClient.getEjb(ExportJMXFacadeBean.class,BCI_GLOBAL_NAME);
    }
    public static ExportJMXFacade getExportServiceLocal() throws RemoteException, CreateException, NamingException {
        return (ExportJMXFacade) EjbClient.getLocalEjb(BCI_GLOBAL_NAME,ExportJMXFacadeBean.class);
    }

    public static MetaProviderJMXFacade getMetaService() throws RemoteException, CreateException, NamingException {
        return (MetaProviderJMXFacade) EjbClient.getRemoteEjb(BCI_GLOBAL_NAME,MetaProviderJMXFacadeBean.class);
    }

    public static MetaProviderJMXFacade getMetaServiceLocal() throws RemoteException, CreateException, NamingException {
        return (MetaProviderJMXFacade) EjbClient.getLocalEjb(BCI_GLOBAL_NAME,MetaProviderJMXFacadeBean.class);
    }

    public static StatusJMXFacade getStatusService() throws RemoteException, CreateException, NamingException {
        return (StatusJMXFacade) EjbClient.getRemoteEjb(BCI_GLOBAL_NAME,StatusJMXFacadeBean.class);
    }

    public static StatusJMXFacade getStatusServiceLocal() throws RemoteException, CreateException, NamingException {
        return (StatusJMXFacade) EjbClient.getLocalEjb(BCI_GLOBAL_NAME,StatusJMXFacadeBean.class);
    }

    public static ServiceJMXFacade getImportService() throws RemoteException, CreateException, NamingException {
        return (ServiceJMXFacade) EjbClient.getRemoteEjb(BCI_GLOBAL_NAME,ServiceJMXFacadeBean.class);
    }
    public static ServiceJMXFacade getImportServiceLocal() throws RemoteException, CreateException, NamingException {
        return (ServiceJMXFacade) EjbClient.getLocalEjb(BCI_GLOBAL_NAME,ServiceJMXFacadeBean.class);
    }
}
