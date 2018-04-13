package edu.ucdavis.genomics.metabolomics.util.thread.locking;

import edu.ucdavis.genomics.metabolomics.exception.LockingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
/*
 * Created on Dec 6, 2005
 */

/**
 * provides a simple locking based on a collection
 *
 * @author wohlgemuth
 * @version Dec 6, 2005
 */
@Component
@Profile("sjp.locking.local")
public class SimpleLocking extends AbstractLocking {
    /**
     *
     */
    private static final long serialVersionUID = 2L;


    @Value("${sjp.locking.timeout:5000}")
    private long timeout;

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    /**
     * contains all locked objects
     */
    private Collection<Serializable> data = new Vector<Serializable>();

    private long sleepTime = 2500;

    protected SimpleLocking() {
        this(SimpleLocking.class.getSimpleName());
    }

    protected SimpleLocking(String owner) {
        super(owner);
    }

    /**
     * @author wohlgemuth
     * @version Dec 6, 2005
     * @see edu.ucdavis.genomics.metabolomics.util.thread.locking.AbstractLocking#isLocked(Object)
     */
    public boolean isLocked(Serializable o) throws LockingException {
        boolean returnboolean = data.contains(o);
        return returnboolean;
    }

    /**
     * @author wohlgemuth
     * @version Dec 6, 2005
     * @see edu.ucdavis.genomics.metabolomics.util.thread.locking.AbstractLocking#lock(Object)
     */
    protected boolean lock(String o) throws LockingException {
        if (isLocked(o)) {
            return false;
        }
        this.data.add(o);
        return true;
    }

    /**
     * we wait between the attemps to auquire a ressource
     *
     * @author wohlgemuth
     * @version Dec 6, 2005
     * @see edu.ucdavis.genomics.metabolomics.util.thread.locking.AbstractLocking#getSleepTime()
     */
    protected synchronized long getSleepTime() {
        return (long) sleepTime;
    }

    /**
     * @author wohlgemuth
     * @version Dec 6, 2005
     * @see Lockable#releaseRessource(Object)
     */
    public void doRelease(String o) throws LockingException {
        if (isLocked(o) == false) {
            logger.warn("ressource was not locked: " + o);
        } else {
            this.data.remove(o);
        }
    }

    /**
     * contains all locked ressources
     *
     * @return
     * @author wohlgemuth
     * @version Apr 21, 2006
     */
    public Collection<Serializable> getLockedRessources() {
        return Collections.unmodifiableCollection(data);
    }

    public long getTimeout() throws LockingException {

        return timeout;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }
}
