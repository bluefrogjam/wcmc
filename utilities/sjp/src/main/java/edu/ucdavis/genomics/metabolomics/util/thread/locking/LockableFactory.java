/*
 * Created on Dec 6, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.thread.locking;

import edu.ucdavis.genomics.metabolomics.exception.FactoryException;
import edu.ucdavis.genomics.metabolomics.util.AbstractFactory;

import java.util.Properties;

/**
 * is used to provide us with implementations of the Lockable object
 * 
 * @author wohlgemuth
 * @version Dec 6, 2005
 * 
 */
public abstract class LockableFactory extends AbstractFactory {
	public static final String DEFAULT_PROPERTY_NAME = LockableFactory.class.getName();

	public LockableFactory() {
		super();
	}

	/**
	 * creates an default instance
	 * 
	 * @author wohlgemuth
	 * @version Dec 6, 2005
	 * @return
	 */
	public static LockableFactory newInstance() {
		return newInstance(findFactory(DEFAULT_PROPERTY_NAME, SimpleLockingFactory.class.getName()));
	}

	/**
	 * returns an new instance of the factory
	 * 
	 * @author wohlgemuth
	 * @version Nov 9, 2005
	 * @return
	 */
	public static LockableFactory newInstance(String factoryClass) {
		Class<?> classObject;
		LockableFactory factory;

		try {
			classObject = Class.forName(factoryClass);
			factory = (LockableFactory) classObject.newInstance();
			return factory;

		} catch (Exception e) {
			throw new FactoryException(e);
		}
	}

	/**
	 * creates our lockable object
	 * 
	 * @author wohlgemuth
	 * @version Dec 6, 2005
	 * @return
	 */
	public abstract Lockable create(String owner,Properties p);

	/**
	 * 
	 * @author wohlgemuth
	 * @version Dec 6, 2005
	 * @return
	 */
	public Lockable create(String owner) {
		return create(owner,System.getProperties());
	}
}
