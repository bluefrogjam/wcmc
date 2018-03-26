package edu.ucdavis.genomics.metabolomics.util.thread.locking;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("sjp.locking.redis")
public class RedissonLockingTest extends AbstractLockableTest {
    @Override
    protected int getLoad() {
        return 5;
    }
}
