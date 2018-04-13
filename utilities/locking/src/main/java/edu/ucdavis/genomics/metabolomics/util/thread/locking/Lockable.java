/*
 * Created on Dec 6, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.thread.locking;

import edu.ucdavis.genomics.metabolomics.exception.LockingException;

import java.io.Serializable;

/**
 * is needed for thread synchronisation
 *
 * @author wohlgemuth
 * @version Dec 6, 2005
 */
public interface Lockable extends Serializable {

    /**
     * get access to this ressource
     *
     * @throws edu.ucdavis.genomics.metabolomics.exception.TimeoutException
     * @author wohlgemuth
     * @version Dec 6, 2005
     */
    public void aquireRessource(String o) throws LockingException;

    /**
     * aquire the ressource during a given time
     *
     * @param o       object to auqire
     * @param timeout the timeout in millisecond
     * @author wohlgemuth
     * @version Dec 6, 2005
     */
    public void aquireRessource(String o, long timeout) throws LockingException;

    /**
     * release the ressource
     *
     * @param o
     * @throws LockingException
     * @author wohlgemuth
     * @version Dec 6, 2005
     */
    public void releaseRessource(String o) throws LockingException;

    /**
     * returns the time out
     *
     * @return
     * @throws LockingException
     */
    abstract long getTimeout() throws LockingException;
}
