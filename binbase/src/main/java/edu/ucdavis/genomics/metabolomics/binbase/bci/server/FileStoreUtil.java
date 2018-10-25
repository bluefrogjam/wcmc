package edu.ucdavis.genomics.metabolomics.binbase.bci.server;

import edu.ucdavis.genomics.metabolomics.binbase.bci.Configurator;
import edu.ucdavis.genomics.metabolomics.binbase.bci.server.exception.FileNotFoundException;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.io.dest.DatabaseDestinationFactoryImpl;
import edu.ucdavis.genomics.metabolomics.util.io.dest.Destination;
import edu.ucdavis.genomics.metabolomics.util.io.dest.DestinationFactory;
import edu.ucdavis.genomics.metabolomics.util.io.source.*;
import org.slf4j.Logger;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

/**
 * simple util to store datafiles in the database so that we can access them
 * from everywhere and speed parallel operations up
 * 
 * @author wohlgemuth
 * 
 */
class FileStoreUtil {

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * try to find the file in the database
	 * 
	 * @param name
	 * @param dirs
	 * @param connection
	 * @throws BinBaseException
	 * @throws NamingException
	 * @throws CreateException
	 * @throws RemoteException
	 */
	@SuppressWarnings("unchecked")
	public void storeFile( String name, Connection connection)
			throws Exception {
		Collection<String> dirs = Configurator.getImportServiceLocal()
				.getImportDirectories();
		if (dirs == null) {
			throw new BinBaseException(
					"you need to configure a directory first in the jmx");
		}

		if (name.contains(":")) {
			name = name.replace(':', '_');
		}

		try {
			if (Configurator.getImportServiceLocal().isValidateSources()) {
				if (dirs.isEmpty()) {
					throw new BinBaseException(
							"you need to configure a directory first in the jmx");
				}
			} else {
				logger.debug("validation of sources is disabled,so we don't check if directories are configured");
			}
		} catch (Exception e1) {
			throw new BinBaseException(e1);
		}

		for (String dir1 : dirs) {
			try {
				String dir = dir1;
				File file = new File(Configurator.getImportServiceLocal()
						.generateFileName(dir, name));
				File filegz = new File(Configurator.getImportServiceLocal()
						.generateFileName(dir, name) + ".gz");

				logger.info("looking for: " + file + "("
						+ file.getAbsolutePath() + ")");
				logger.info("or looking for: " + filegz + "("
						+ filegz.getAbsolutePath() + ")");

				if (file.exists()) {
					logger.info("file exists so we try to store it");
					Source source = SourceFactory.newInstance(
							FileSourceFactoryImpl.class.getName())
							.createSource(file);
					try {
						name = storeFile(name, connection, source,
								false);

					} catch (Exception e) {
						logger.error(e.getMessage(), e);

						throw new BinBaseException(e);
					}
					return;

				} else if (filegz.exists()) {
					logger.info("file exists so we try to store it");
					Source source = SourceFactory.newInstance(
							FileSourceFactoryImpl.class.getName())
							.createSource(filegz);
					try {
						name = storeFile(name, connection, source, true);

					} catch (Exception e) {
						logger.error(e.getMessage(), e);

						throw new BinBaseException(e);
					}
					return;

				} else {
					logger.info("file didn't exist in this dir: " + dir);
				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new FileNotFoundException(e);
			}
		}

		// checking on the application server and import the file from there if
		// it's available
		byte[] content = Configurator.getImportServiceLocal().downloadFile(name);

		if (content != null) {
			logger.info("found file on the server and using it");
			name = storeFile( name, connection, new ByteArraySource(
					content), false);

		} else {
			logger.info("server didn't have file either");
		}

		// checking the database if it exist in there
		name = name.replaceAll(".txt.gz", "");
		name = name.replaceAll(".txt", "");

		Properties p = Configurator.getDatabaseServiceLocal().getProperties();
		p.put("CONNECTION", connection);
		Source source = SourceFactory.newInstance(
				DatabaseSourceFactoryImpl.class.getName())
				.createSource(name, p);
		if (source.exist()) {
			logger.info("using database version since no local version was found");
			return;
		}
		// ignore and move on

		// give up and scream for help
		throw new FileNotFoundException("couldn't find file " + name
				+ " , please check configuration");
	}

	private String storeFile(String name, Connection connection,
			Source source, boolean gzip) throws BinBaseException,
			CreateException, NamingException,
			IOException {
		// never ever save the endings!!!
		if (gzip) {
			name = name.replaceAll(".txt.gz", "");

		} else {
			name = name.replaceAll(".txt", "");
		}

		Properties p = Configurator.getDatabaseServiceLocal().getProperties();
		p.put("CONNECTION", connection);

		Destination destination = DestinationFactory.newInstance(
				DatabaseDestinationFactoryImpl.class.getName())
				.createDestination(name, p);

		OutputStream out = destination.getOutputStream();

		int size = 1024 * 16;
		byte[] buffer = new byte[size];
		int length;

		InputStream in = null;

		if (gzip) {
			in = new GZIPInputStream(source.getStream());
		} else {
			in = source.getStream();
		}

		while ((length = in.read(buffer, 0, size)) != -1) {
			out.write(buffer, 0, length);
		}

		in.close();
		out.flush();
		out.close();
		return name;
	}

	/**
	 * try to find the file in the database
	 * 
	 * @param name
	 * @param dirs
	 * @param connection
	 * @throws BinBaseException
	 * @throws NamingException
	 * @throws CreateException
	 * @throws RemoteException
	 */
	@SuppressWarnings("unchecked")
	public long calculateTimeStamp(String name) throws Exception {
		Collection<String> dirs = Configurator.getImportServiceLocal()
				.getImportDirectories();
		if (dirs == null) {
			throw new BinBaseException(
					"you need to configure a directory first in the jmx");
		}

		if (name.contains(":")) {
			name = name.replace(':', '_');
		}

		Iterator<String> it = dirs.iterator();

		while (it.hasNext()) {
			try {

				String dir = it.next();
				File file = new File(Configurator.getImportServiceLocal()
						.generateFileName(dir, name));

				File fileGZ = new File(Configurator.getImportServiceLocal()
						.generateFileName(dir, name) + ".gz");

				logger.info("looking for: " + file + " or n");
				logger.info("looking for: " + fileGZ + " or n");

				if (file.exists()) {
					return file.lastModified();
				} else if (fileGZ.exists()) {
					return fileGZ.lastModified();
				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new FileNotFoundException(e);
			}
		}

		// give up and scream for help
		throw new FileNotFoundException("couldn't find file " + name
				+ " , please check configuration");
	}

}
