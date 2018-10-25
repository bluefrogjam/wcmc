package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

/**
 * Created by wohlgemuth on 10/18/16.
 */
public interface CommunicationJMXMBean extends
        javax.management.MBeanRegistration{
    /**
     * @jmx.managed-operation description="email address of the sender"
     * @return
     */
    String getFromAdress();

    /**
     * @jmx.managed-operation description="email address of the sender"
     * @param fromAdress
     */
    void setFromAdress(String fromAdress);

    /**
     * @jmx.managed-operation description="password of the email account"
     * @return
     */
    String getPassword();

    /**
     * @jmx.managed-operation description="password of the email account"
     * @param password
     */
    void setPassword(String password);

    /**
     * @jmx.managed-operation description="smtp port"
     * @return
     */
    String getSmtpPort();

    /**
     * @jmx.managed-operation description="smtp port"
     * @param smtpPort
     */
    void setSmtpPort(String smtpPort);

    /**
     * @jmx.managed-operation description="smtp server"
     * @return
     */
    String getSmtpServer();

    /**
     * @jmx.managed-operation description="smtp server"
     * @param smtpServer
     */
    void setSmtpServer(String smtpServer);

    /**
     * @jmx.managed-operation description="username"
     * @return
     */
    String getUsername();

    /**
     * @jmx.managed-operation description="username"
     * @param username
     */
    void setUsername(String username);
}
