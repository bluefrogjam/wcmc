package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.math;

/**
 * Created by diego on 8/12/2016.
 */
public class LUMatrix {
	public double[][] matrix;
	public int[] indexVector;
	public double reverse;

	public LUMatrix(double[][] matrix, int[] indexVector, double reverse) {
		this.matrix = matrix;
		this.indexVector = indexVector;
		this.reverse = reverse;
	}
}
