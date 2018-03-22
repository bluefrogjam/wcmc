/*
 * Created on Dec 6, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.thread.locking;

import edu.ucdavis.genomics.metabolomics.exception.LockingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * abstract test which validates that we can lock ressources successfull
 * 
 * @author wohlgemuth
 * 
 */
public abstract class AbstractLockableTest {

	private String ressource = "a fancy locked ressource";

	private boolean failed;

	/**
	 * how high should be the load for the locking algorythm
	 * 
	 * @return
	 */
	protected abstract int getLoad();

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetTimeout() throws LockingException {
		assertTrue(getLockable().getTimeout() > 0);
	}

	@Test
	public void testAquireRessourceObjectLongNoTimeout() throws Exception {
		System.err.println("create threads...");

		WaitingThreadGroup group = new WaitingThreadGroup("my group");

		for (int i = 0; i < this.getLoad(); i++) {
			this.startLockingTask(i, 900000000, group);
		}

		waitForOperation(group);
		assertTrue(failed == false);
	}

	protected void waitForOperation(WaitingThreadGroup group) throws Exception {
		group.join();
	}

	protected void startLockingTask(int id, long timeout, ThreadGroup group) {
		new ThreadTestTimeout(group, timeout, String.valueOf(id));
	}

	@Test
	public void testAquireRessourceObjectLong() throws Exception {
		failed = false;

		// 5 is way to short to finish one of these threads so we should have 4
		// timeouts!
		WaitingThreadGroup group = new WaitingThreadGroup("my group");

		for (int i = 0; i < this.getLoad(); i++) {
			this.startLockingTask(i, 1, group);
		}

		waitForOperation(group);
		assertTrue(failed);
	}

	/**
	 * returns the correct implementation
	 * 
	 * @author wohlgemuth
	 * @version Dec 6, 2005
	 * @return
	 */
	protected abstract Lockable getLockable();

	/**
	 * simple test needed for testing the locking
	 * 
	 * @author wohlgemuth
	 * @version Dec 6, 2005
	 * 
	 */
	class ThreadTestTimeout extends Thread {
		long timeout = 0;
		Lockable lockable;
		String name;

		public ThreadTestTimeout(ThreadGroup group, long timeout, String string) {
			super(group, string);
			this.timeout = timeout;
			name = string;
			lockable = getLockable();
			this.start();
		}

		public void run() {
			try {
				System.err.println(name + " aquire ressource");
				lockable.aquireRessource(ressource, timeout);
				for (int i = 0; i < 80; i++) {
					Thread.sleep(50);
					System.err.print(name);
				}
				System.err.println();
				System.err.println(name + " release ressource");
				lockable.releaseRessource(ressource);
				System.err.println(name + " done");
			} catch (Exception e) {
				e.printStackTrace();
				failed = true;
			}
		}

	}

}
