package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import edu.ucdavis.genomics.metabolomics.binbase.cluster.util.JBosssPropertyHolder;
import org.slf4j.Logger;


/**
 * jmx needed for communications
 *
 */
public class CommunicationJMX implements CommunicationJMXMBean {
	private String username = "binbase@gmail.com";

	private String password = "password";

	private String smtpServer = "smtp.gmail.com";

	private String fromAdress = "binbase@gmail.com";

	private String smtpPort = "465";

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public String getFromAdress() {
		return fromAdress;
	}

	@Override
	public void setFromAdress(String fromAdress) {
		this.fromAdress = fromAdress;
		store();
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
		store();
	}

	@Override
	public String getSmtpPort() {
		return smtpPort;
	}

	@Override
	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
		store();
	}

	@Override
	public String getSmtpServer() {
		return smtpServer;
	}

	@Override
	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
		store();
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public void setUsername(String username) {
		this.username = username;
		store();
	}

	public void postDeregister() {
		// TODO Auto-generated method stub
		store();
	}

	public void postRegister(Boolean arg0) {
		try {
			File file = JBosssPropertyHolder.getPropertyFile(getClass());
			if (!file.exists()) {
				return;
			}

			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					file));

			HashMap map = (HashMap) in.readObject();

			if(map.get("from") != null) {
				this.fromAdress = (String) map.get("from");
			}
			if(map.get("password") != null) {
				this.password = (String) map.get("password");
			}
			if(map.get("port") != null) {
				this.smtpPort = (String) map.get("port");
			}
			if(map.get("server") != null) {
				this.smtpServer = (String) map.get("server");
			}
			if(map.get("user") != null) {
				this.username = (String) map.get("user");
			}


		} catch (Exception e) {
			logger.warn("postRegister(Boolean)", e); //$NON-NLS-1$

		}
	}

	public void preDeregister() throws Exception {
	}

	public ObjectName preRegister(MBeanServer arg0, ObjectName arg1)
			throws Exception {
		return null;
	}

	private void store() {
		logger.info("store properties");
		try {
			File file = JBosssPropertyHolder.getPropertyFile(getClass());

			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(file));
			HashMap map = new HashMap();
			map.put("from",getFromAdress());
			map.put("password",getPassword());
			map.put("port",getSmtpPort());
			map.put("server",getSmtpServer());
			map.put("user",getUsername());

			out.writeObject(map);
			out.flush();
			out.close();

		} catch (Exception e) {
			logger.error(e.getMessage(), e); //$NON-NLS-1$
		}
	}


}
