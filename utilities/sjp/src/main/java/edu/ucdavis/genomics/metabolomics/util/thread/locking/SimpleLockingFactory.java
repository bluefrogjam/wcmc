/*
 * Created on Dec 6, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.thread.locking;

import java.util.Properties;

/**
 * 
 * @author wohlgemuth
 * @version Dec 6, 2005
 *
 */
public class SimpleLockingFactory extends LockableFactory{

	public SimpleLockingFactory() {
		super();
	}

	/**
	 * returns the simple locking singleton
	 * @author wohlgemuth
	 * @version Dec 6, 2005
	 * @see LockableFactory#create(Properties)
	 */
	public Lockable create(String owner,Properties p) {
		return SimpleLocking.getInstance();
	}

}
