package edu.ucdavis.genomics.metabolomics.util.thread.locking;

import edu.ucdavis.genomics.metabolomics.exception.LockingException;
import edu.ucdavis.genomics.metabolomics.exception.TimeoutException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * this implementation allows cross server based locking and requires a redis server to run
 */
@Component
@Profile("sjp.locking.redis")
public class RedissonLocking implements Lockable {

    @Autowired
    RedissonClient client;

    @Value("${sjp.locking.timeout:5000}")
    private long timeout;

    @Override
    public void aquireRessource(String o) throws LockingException {
        aquireRessource(o, getTimeout());
    }

    @Override
    public void aquireRessource(String o, long timeout) throws LockingException {
        RLock lock = client.getLock(o);
        try {
            if (!lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                throw new TimeoutException("locking failed due to timeout");
            }
        } catch (InterruptedException e) {
            throw new LockingException(e.getMessage());
        }
    }

    @Override
    public void releaseRessource(String o) throws LockingException {
        RLock lock = client.getLock(o);
        lock.forceUnlock();
    }

    @Override
    public long getTimeout() throws LockingException {
        return timeout;
    }
}
