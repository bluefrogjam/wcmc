package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import javax.ejb.Remote;

/**
 * Created by wohlgemuth on 10/18/16.
 */

public interface CommunicationJMXFacade {
    /**
     * @return
     * @jmx.managed-operation description="email address of the sender"
     */
    String getFromAdress();

    /**
     * @param fromAdress
     * @jmx.managed-operation description="email address of the sender"
     */
    void setFromAdress(String fromAdress);

    /**
     * @return
     * @jmx.managed-operation description="password of the email account"
     */
    String getPassword();

    /**
     * @param password
     * @jmx.managed-operation description="password of the email account"
     */
    void setPassword(String password);

    /**
     * @return
     * @jmx.managed-operation description="smtp port"
     */
    String getSmtpPort();

    /**
     * @param smtpPort
     * @jmx.managed-operation description="smtp port"
     */
    void setSmtpPort(String smtpPort);

    /**
     * @return
     * @jmx.managed-operation description="smtp server"
     */
    String getSmtpServer();

    /**
     * @param smtpServer
     * @jmx.managed-operation description="smtp server"
     */
    void setSmtpServer(String smtpServer);

    /**
     * @return
     * @jmx.managed-operation description="username"
     */
    String getUsername();

    /**
     * @param username
     * @jmx.managed-operation description="username"
     */
    void setUsername(String username);

}
