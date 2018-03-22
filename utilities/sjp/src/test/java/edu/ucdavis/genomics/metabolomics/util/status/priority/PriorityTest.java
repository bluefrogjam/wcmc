package edu.ucdavis.genomics.metabolomics.util.status.priority;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PriorityTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCompareDebug() {

		assertTrue(Priority.DEBUG.compareTo(Priority.TRACE) > 0);
		assertTrue(Priority.DEBUG.compareTo(Priority.DEBUG) == 0);

		assertTrue(Priority.DEBUG.compareTo(Priority.INFO) < 0);
		assertTrue(Priority.DEBUG.compareTo(Priority.WARNING) < 0);
		assertTrue(Priority.DEBUG.compareTo(Priority.ERROR) < 0);
		assertTrue(Priority.DEBUG.compareTo(Priority.FATAL) < 0);

	}
	

	@Test
	public void testCompareInfo() {

		assertTrue(Priority.INFO.compareTo(Priority.TRACE) > 0);
		assertTrue(Priority.INFO.compareTo(Priority.DEBUG) > 0);

		assertTrue(Priority.INFO.compareTo(Priority.INFO) == 0);
		assertTrue(Priority.INFO.compareTo(Priority.WARNING) < 0);
		assertTrue(Priority.INFO.compareTo(Priority.ERROR) < 0);
		assertTrue(Priority.INFO.compareTo(Priority.FATAL) < 0);

	}
	@Test
	public void testCompareFATAL() {

		assertTrue(Priority.FATAL.compareTo(Priority.TRACE) > 0);
		assertTrue(Priority.FATAL.compareTo(Priority.DEBUG) > 0);

		assertTrue(Priority.FATAL.compareTo(Priority.INFO) > 0);
		assertTrue(Priority.FATAL.compareTo(Priority.WARNING) > 0);
		assertTrue(Priority.FATAL.compareTo(Priority.ERROR) > 0);
		assertTrue(Priority.FATAL.compareTo(Priority.FATAL) == 0);

	}
	

}
