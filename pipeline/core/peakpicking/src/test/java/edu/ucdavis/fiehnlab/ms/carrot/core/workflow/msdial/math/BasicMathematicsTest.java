package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.math;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by diego on 7/20/2016.
 */

public class BasicMathematicsTest {
	List<Double> listOddD = Arrays.asList(1.0, 2.0, 3.0);
	List<Double> listEvenD = Arrays.asList(1.0, 2.0, 3.0, 4.0);
	List<Integer> listOddI = Arrays.asList(1, 2, 3);
	List<Integer> listEvenI = Arrays.asList(1, 2, 3, 4);

	@Test
	public void testMedianOnOddSizedList() {
		assertEquals (2.0, BasicMathematics.Median(listOddD));
		assertEquals (2.0, BasicMathematics.MedianInt(listOddI));
	}

	@Test
	public void testMedianOnEvenSizedList() {
		assertEquals (2.5, BasicMathematics.Median(listEvenD));
		assertEquals (2.5, BasicMathematics.MedianInt(listEvenI));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMedianOnEmptyList() {
		BasicMathematics.Median(new java.util.ArrayList<>());
		BasicMathematics.MedianInt(new java.util.ArrayList<>());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMedianOnNullList() {
		BasicMathematics.Median(null);
		BasicMathematics.MedianInt(null);
	}
}
