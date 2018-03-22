/*
 * Created on Dec 6, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.thread.locking;

public class SimpleLockingTest extends AbstractLockableTest{

	protected AbstractLocking getLockable() {
		return SimpleLocking.getInstance();
	}

	@Override
	protected int getLoad() {
		return 5;
	}

}
