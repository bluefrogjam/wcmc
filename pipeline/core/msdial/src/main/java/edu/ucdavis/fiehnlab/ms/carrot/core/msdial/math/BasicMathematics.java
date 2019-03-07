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
    public static long old_binomialCoefficient(int n, int k) {
//        System.out.print("n="+n+", k="+k);
        if (k < 0 || k > n) {
            return 0;
        } else {
            int p = 1, q = 1;

            for (int i = 1; i <= Math.min(k, n - k); i++) {
                p *= n;
                q *= i;
                n -= 1;
            }

            // print and try block added by me to compare values with other binomial coeff.
//            System.out.println("  (p="+p+",q="+q+") => " + p/q);
            try {
                return p / q;
            } catch (ArithmeticException e) {
                return 0;
            }
        }
    }

    /**
     * Alternative binomial coefficient, doesn't seem to break on C57H114
     * from https://www.geeksforgeeks.org/binomial-coefficient-dp-9
     *
     * @param n
     * @param k
     * @return
     */
    public static long binomialCoefficient(int n, int k) {
//        System.out.print("n="+n+", k="+k);
        int C[][] = new int[n+1][k+1];
        int i, j;

        // Calculate  value of Binomial Coefficient in bottom up manner
        for (i = 0; i <= n; i++)
        {
            for (j = 0; j <= Math.min(i, k); j++)
            {
                // Base Cases
                if (j == 0 || j == i)
                    C[i][j] = 1;

                    // Calculate value using previously stored values
                else
                    C[i][j] = C[i-1][j-1] + C[i-1][j];
            }
        }

//        System.out.println("  C[n][k] => " + C[n][k]);
        return C[n][k];
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
