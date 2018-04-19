package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.math;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diego on 7/20/2016.
 */
public class BasicMathematics {

    /**
     * Far more efficient and less error-prone binomial coefficient
     * @param n
     * @param k
     * @return
     */
    public static long binomialCoefficient(int n, int k) {
        if (k < 0 || k > n) {
            return 0;
        } else {
            int p = 1, q = 1;

            for (int i = 1; i <= Math.min(k, n - k); i++) {
                p *= n;
                q *= i;
                n -= 1;
            }

            return p / q;
        }
    }

	public static double median(List<Double> list) {
		if (list == null || list.size() == 0)
			throw new IllegalArgumentException("The list can't be empty");

        list.sort(Double::compareTo);

        if (list.size() % 2 == 0) {
            return (list.get(list.size() / 2) + list.get(list.size() / 2 - 1)) / 2.0;
        } else {
            return list.get(list.size() / 2);
        }
    }

	public static double medianInt(List<Integer> list) {
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

	public static double brokenMedian(List<Float> list) {
		List<Float> sortedlist = new ArrayList<>(list);
		sortedlist.sort(Float::compareTo);

        return sortedlist.get(sortedlist.size() / 2);
    }
}
