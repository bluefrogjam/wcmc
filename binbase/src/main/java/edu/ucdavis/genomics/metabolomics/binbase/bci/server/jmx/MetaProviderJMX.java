package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import edu.ucdavis.genomics.metabolomics.binbase.bci.setupX.SetupXProvider;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import org.slf4j.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Collection;
import java.util.HashSet;

public class MetaProviderJMX implements MetaProviderJMXMBean {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger
			.getLogger(MetaProviderJMX.class);

	/**
	 * our collection of providers
	 */
	private Collection<String> provider = new HashSet<>();

	@Override
	public void addProvider(String providerClass) {
		if (!isProviderRegistered(providerClass)) {
			this.provider.add(providerClass);
		}
	}

	@Override
	public void removeProvider(String providerClass) {
		if (isProviderRegistered(providerClass)) {
			this.provider.remove(providerClass);
		}
	}

	@Override
	public Collection getProvider() {
		return provider;
	}

	@Override
	public boolean isProviderRegistered(String providerClass) {
		return this.provider.contains(providerClass);
	}

	public ObjectName preRegister(MBeanServer arg0, ObjectName arg1)
			throws Exception {
		return arg1;
	}

	/**
	 * time to load the last configuration
	 */
	public void postRegister(Boolean arg0) {
	}

	/**
	 * time to save the current configuration
	 */
	public void preDeregister() throws Exception {
	}

	public void postDeregister() {
	}


	@Override
	public String getSetupXId(String sampleName) throws BinBaseException {
		try {

			for (Object o : this.getProvider()) {
				SetupXProvider pro = (SetupXProvider) Class.forName(
						o.toString()).newInstance();
				String result = pro.getSetupXId(sampleName);

				if (result != null) {
					return result;
				}
			}

			return null;
		} catch (Exception e) {
			throw new BinBaseException(e);
		}
	}

	@Override
	public void upload(String experimentId, String content)
			throws BinBaseException {
		try {

			for (Object o : this.getProvider()) {
				SetupXProvider pro = (SetupXProvider) Class.forName(
						o.toString()).newInstance();
				pro.upload(experimentId, content);
			}
		} catch (Exception e) {
			throw new BinBaseException(e);
		}
	}


	@Override
	public boolean canCreateBins(String setupxId) throws BinBaseException {
		try {

			for (Object o : this.getProvider()) {
				SetupXProvider pro = (SetupXProvider) Class.forName(
						o.toString()).newInstance();
				boolean result = pro.canCreateBins(setupxId);

				if (!result) {
					return false;
				}
			}

			return true;

		} catch (Exception e) {
			throw new BinBaseException(e);
		}
	}



	@Override
	public void upload(String experimentId, byte[] data) throws BinBaseException {
		try {

			for (Object o : this.getProvider()) {
				SetupXProvider pro = (SetupXProvider) Class.forName(
						o.toString()).newInstance();
				pro.upload(experimentId, data);
			}
		} catch (Exception e) {
			throw new BinBaseException(e);
		}
	}

}