package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.anotation.logger;

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;

/**
 * a simple prefixed logger
 * 
 * @author wohlgemuth
 * 
 */
public class PrefixedLogger extends Logger {

	protected PrefixedLogger(String name) {
		super(name);
		logger = LoggerFactory.getLogger(name);
	}

	private Logger logger;

	private String loggingPrefix = "";

	public static PrefixedLogger getLogger(String name) {
		return new PrefixedLogger(name);

	}

	public static PrefixedLogger getLogger(Class name) {
		return getLogger(name.getSimpleName());

	}

	public void warn(Object message) {
		this.logger.warn(loggingPrefix + message);
	}

	public void warn(Object message, Throwable e) {
		this.logger.warn(loggingPrefix + message, e);
	}

	public void info(Object message) {
		this.logger.info(loggingPrefix + message);
	}

	public void info(Object message, Throwable e) {
		this.logger.info(loggingPrefix + message, e);
	}

	public void error(Object message) {
		this.logger.error(loggingPrefix + message);
	}

	public void error(Object message, Throwable e) {
		this.logger.error(loggingPrefix + message, e);
	}

	public void debug(Object message) {
		if (logger.isDebugEnabled()) {
			this.logger.debug(loggingPrefix + message);
		}
	}

	public void debug(Object message, Throwable e) {
		if (logger.isDebugEnabled()) {
			this.logger.debug(loggingPrefix + message, e);
		}
	}

	public void fatal(Object message, Throwable t) {
		this.logger.fatal(loggingPrefix + message, t);
	}

	public void fatal(Object message) {
		this.logger.fatal(loggingPrefix + message);
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public String getLoggingPrefix() {
		return loggingPrefix;
	}

	public void setLoggingPrefix(String loggingPrefix) {
		this.loggingPrefix = loggingPrefix;
	}

	/**
	 * needed for bin logging
	 * 
	 * @param bin
	 * @param spectra
	 */
	public void setLoggingPrefix(Object bin, Object binSpectraId, Object spectra) {
		this.loggingPrefix = new StringBuffer().append("(").append( bin).append("{").append( binSpectraId).append("}").append(" vs ").append(spectra).append(") - ").toString();
	}

	public void addAppender(Appender newAppender) {
		logger.addAppender(newAppender);
	}

	public void assertLog(boolean assertion, String msg) {
		logger.assertLog(assertion, msg);
	}

	public void callAppenders(LoggingEvent event) {
		logger.callAppenders(event);
	}

	public boolean equals(Object obj) {
		return logger.equals(obj);
	}

	public boolean getAdditivity() {
		return logger.getAdditivity();
	}

	public Enumeration getAllAppenders() {
		return logger.getAllAppenders();
	}

	public Appender getAppender(String name) {
		return logger.getAppender(name);
	}

	public Priority getChainedPriority() {
		return logger.getChainedPriority();
	}

	public Level getEffectiveLevel() {
		return logger.getEffectiveLevel();
	}

	public LoggerRepository getHierarchy() {
		return logger.getHierarchy();
	}

	public LoggerRepository getLoggerRepository() {
		return LoggerFactory.getLoggerRepository();
	}

	public ResourceBundle getResourceBundle() {
		return logger.getResourceBundle();
	}

	public int hashCode() {
		return logger.hashCode();
	}

	public boolean isAttached(Appender appender) {
		return logger.isAttached(appender);
	}

	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	public boolean isEnabledFor(Priority level) {
		return logger.isEnabledFor(level);
	}

	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	public void l7dlog(Priority priority, String key, Object[] params,
			Throwable t) {
		logger.l7dlog(priority, key, params, t);
	}

	public void l7dlog(Priority priority, String key, Throwable t) {
		logger.l7dlog(priority, key, t);
	}

	public void log(Priority priority, Object message, Throwable t) {
		logger.log(priority, message, t);
	}

	public void log(Priority priority, Object message) {
		logger.log(priority, message);
	}

	public void log(String callerFQCN, Priority level, Object message,
			Throwable t) {
		logger.log(callerFQCN, level, message, t);
	}

	public void removeAllAppenders() {
		logger.removeAllAppenders();
	}

	public void removeAppender(Appender appender) {
		logger.removeAppender(appender);
	}

	public void removeAppender(String name) {
		logger.removeAppender(name);
	}

	public void setAdditivity(boolean additive) {
		logger.setAdditivity(additive);
	}

	public void setLevel(Level level) {
		logger.setLevel(level);
	}

	public void setPriority(Priority priority) {
		logger.setPriority(priority);
	}

	public void setResourceBundle(ResourceBundle bundle) {
		logger.setResourceBundle(bundle);
	}

	public void clearPrefix() {
		this.loggingPrefix = "";
	}

	public String toString() {
		return logger.toString();
	}

}
