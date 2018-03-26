/*
 * Created on Dec 6, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.thread.locking;

import edu.ucdavis.genomics.metabolomics.exception.LockingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.locks.Lock;

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


	@Autowired
	private Lockable lock;

	/**
	 * how high should be the load for the locking algorythm
	 * 
	 * @return
	 */
	protected abstract int getLoad();

	@Test
	public void testGetTimeout() throws LockingException {
		assertTrue(lock.getTimeout() > 0);
	}

	@Test
	public void testAquireRessourceObjectLongNoTimeout() throws Exception {
		System.err.println("create threads...");

        WaitingThreadGroup group = new WaitingThreadGroup("dadsa");
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
	public void testAquireRessourceObjectLongShouldTimeout() throws Exception {
		failed = false;
        WaitingThreadGroup group = new WaitingThreadGroup("dadsa");
		for (int i = 0; i < this.getLoad(); i++) {
			this.startLockingTask(i, 1, group);
		}

		waitForOperation(group);
		assertTrue(failed);
	}

	/**
	 * simple test needed for testing the locking
	 * 
	 * @author wohlgemuth
	 * @version Dec 6, 2005
	 * 
	 */
	class ThreadTestTimeout extends Thread {
		long timeout = 0;
		String name;

		public ThreadTestTimeout(ThreadGroup group, long timeout, String string) {
			super(group, string);
			this.timeout = timeout;
			name = string;
			this.start();
		}

		public void run() {
			try {
				System.err.println(name + " aquire ressource");
				lock.aquireRessource(ressource, timeout);
				for (int i = 0; i < 80; i++) {
					Thread.sleep(50);
					System.err.print(name);
				}
				System.err.println();
				System.err.println(name + " release ressource");
				lock.releaseRessource(ressource);
				System.err.println(name + " done");
			} catch (Exception e) {
				e.printStackTrace();
				failed = true;
			}
		}

	}

}
