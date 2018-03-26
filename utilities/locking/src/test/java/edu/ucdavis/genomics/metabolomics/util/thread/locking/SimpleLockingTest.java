/*
 * Created on Dec 6, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.thread.locking;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("sjp.locking.local")
public class SimpleLockingTest extends AbstractLockableTest{
	@Override
	protected int getLoad() {
		return 5;
	}

}
