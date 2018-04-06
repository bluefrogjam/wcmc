package edu.ucdavis.genomics.metabolomics.util.thread.locking;

import edu.ucdavis.genomics.metabolomics.exception.AlreadyLockedException;
import edu.ucdavis.genomics.metabolomics.exception.LockingException;
import edu.ucdavis.genomics.metabolomics.exception.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
/*
 * Created on Dec 6, 2005
 */

/**
 * provides a standard to aquire objects
 *
 * @author wohlgemuth
 * @version Dec 6, 2005
 */
public abstract class AbstractLocking implements Lockable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * defines the time of the attempt to lock the object
     */
    public static long LOCK_TIME = 300000;


    public AbstractLocking(String owner) {
        super();
    }


    /**
     * @throws LockingException
     * @author wohlgemuth
     * @version Dec 6, 2005
     * @see Lockable#aquireRessource(Object)
     */
    public synchronized void aquireRessource(String o)
        throws LockingException {
        aquireRessource(o, LOCK_TIME);
    }

    /**
     * @throws LockingException
     * @author wohlgemuth
     * @version Dec 6, 2005
     * @see Lockable#aquireRessource(Object,
     * long)
     */
    public synchronized void aquireRessource(String o, long timeout)
        throws LockingException {
        Date date = new Date();
        long begin = date.getTime();
        long end = begin + timeout;

        int waits = 1;
        if (!lock(o)) {
            while (!lock(o)) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("waiting till ressource is released: " + o
                            + " time left: "
                            + (timeout - (getSleepTime() * waits)) + " ("
                            + Thread.currentThread().getId() + "/"
                            + Thread.currentThread().getName() + "/"
                            + Thread.currentThread().getThreadGroup().getName()
                            + ")");

                    }

                    Thread.sleep(getSleepTime());
                    waits++;
                } catch (InterruptedException e) {

                    throw new TimeoutException(e);
                }
                date = new Date();
                if (date.getTime() >= end) {

                    TimeoutException e = new TimeoutException(
                        "sorry coudln't aquire ressource, operation timed out after: "
                            + timeout);

                    logger.debug("timed out: " + e);

                    throw e;
                }
            }
        }
        try {
            logger.debug("locking ressource: " + o);
            this.lock(o);

        } catch (AlreadyLockedException e) {
            logger.debug("ressource was already locked!");
            aquireRessource(o, timeout);
        }
    }


    /**
     * lock this object
     *
     * @param o
     * @throws LockingException
     * @author wohlgemuth
     * @version Dec 6, 2005
     */
    protected abstract boolean lock(String o) throws LockingException;

    /**
     * how long are we going to sleep
     *
     * @author wohlgemuth
     * @version Dec 6, 2005
     * @see Object#wait()
     */
    protected abstract long getSleepTime();

    public final void releaseRessource(String o) throws LockingException {
        doRelease(o);
    }

    protected abstract void doRelease(String o) throws LockingException;
}
