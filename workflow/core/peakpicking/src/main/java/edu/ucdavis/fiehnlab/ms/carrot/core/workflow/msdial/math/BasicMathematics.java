package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.math;

import java.util.*;

/**
 * Created by diego on 7/20/2016.
 */
public class BasicMathematics {
	public static double Median(List<Double> list) {
		if (list == null || list.size() == 0)
			throw new IllegalArgumentException("The list can't be empty");

		list.sort(Double::compareTo);

		if (list.size() % 2 == 0) {
			return (list.get(list.size() / 2) + list.get(list.size() / 2 - 1)) / 2.0;
		} else {
			return list.get(list.size() / 2);
		}
	}

	public static double MedianInt(List<Integer> list) {
		if (list == null || list.size() == 0)
			throw new IllegalArgumentException("The list can't be empty");

		list.sort(Integer::compareTo);

		if (list.size() % 2 == 0) {
			return (list.get(list.size() / 2) + list.get(list.size() / 2 - 1)) / 2.0;
		} else {
			return list.get(list.size() / 2);
		}
	}

	public static double sumOfSquare(double[] array) {
		double sum = 0;
		for (double number : array) {
			sum += number * number;
		}
		return sum;
	}

	public static double innerProduct(double[] array1, double[] array2) {
		double sum = 0;
		for (int i = 0; i < array1.length; i++) {
			sum += array1[i] * array2[i];
		}
		return sum;
	}

	public static double BrokenMedian(List<Double> list) {
		List<Double> sortedlist = new ArrayList<>();
		sortedlist.addAll(list);

		sortedlist.sort(Double::compareTo);
		return sortedlist.get(sortedlist.size() / 2);
	}
}
