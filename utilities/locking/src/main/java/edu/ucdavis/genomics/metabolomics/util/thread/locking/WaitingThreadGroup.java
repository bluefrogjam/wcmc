/*
 * Created on Dec 9, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.thread.locking;

/**
 * an extended thread group
 *
 * @author wohlgemuth
 * @version Dec 9, 2005
 */
public class WaitingThreadGroup extends ThreadGroup {

    public WaitingThreadGroup(String arg0) {
        super(arg0);
    }

    public WaitingThreadGroup(ThreadGroup arg0, String arg1) {
        super(arg0, arg1);
    }

    /**
     * waits until all threads are done
     *
     * @throws InterruptedException
     * @author wohlgemuth
     * @version Dec 9, 2005
     */
    public void join() throws InterruptedException {
        while (this.activeCount() > 0) {
            Thread.sleep(1000);
        }
    }
}
