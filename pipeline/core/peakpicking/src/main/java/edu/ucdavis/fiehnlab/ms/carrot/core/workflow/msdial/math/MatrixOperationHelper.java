package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.math;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.LUMatrix;

/**
 * Created by diego on 8/12/2016.
 */
public class MatrixOperationHelper {
	public static LUMatrix matrixDecompose(double[][] rawMatrix) {
		int elementSize = rawMatrix[0].length, imax, tmp;
		double[] scalingVector = new double[elementSize];
		int[] indexVector = new int[elementSize];
		double big, dum, sum, temp, d = 1.0;

		for (int i = 0; i < elementSize; i++) {
			big = 0.0;
			for (int j = 0; j < elementSize; j++) {
				temp = Math.abs(rawMatrix[i][j]);
				if (temp > big) big = temp;
			}
			if (big == 0.0) {
				System.out.println("Singular matrix in touine ludcmp");
				return null;
			}
			scalingVector[i] = 1.0 / big;
			indexVector[i] = i;
		}

		for (int j = 0; j < elementSize; j++) {
			imax = j;
			for (int i = 0; i < j; i++) {
				sum = rawMatrix[i][j];
				for (int k = 0; k < i; k++) {
					sum -= rawMatrix[i][k] * rawMatrix[k][j];
				}
				rawMatrix[i][j] = sum;
			}

			big = 0.0;
			for (int i = j; i < elementSize; i++) {
				sum = rawMatrix[i][j];
				for (int k = 0; k < j; k++) {
					sum -= rawMatrix[i][k] * rawMatrix[k][j];
				}
				rawMatrix[i][j] = sum;

				dum = scalingVector[i] * Math.abs(sum);
				if (dum >= big) {
					big = dum;
					imax = i;
				}
			}

			if (j != imax) {
				for (int k = 0; k < elementSize; k++) {
					dum = rawMatrix[imax][k];
					rawMatrix[imax][k] = rawMatrix[j][k];
					rawMatrix[j][k] = dum;
				}
				d = -1 * d;
				temp = scalingVector[imax];
				scalingVector[imax] = scalingVector[j];
				scalingVector[j] = temp;

				tmp = indexVector[imax];
				indexVector[imax] = indexVector[j];
				indexVector[j] = tmp;
			}

			if (rawMatrix[j][j] == 0.0) {
				rawMatrix[j][j] = Math.pow(10, -10);
			}

			if (j != elementSize) {
				dum = 1.0 / rawMatrix[j][j];
				for (int i = j + 1; i < elementSize; i++) rawMatrix[i][j] *= dum;
			}
		}
		return new LUMatrix(rawMatrix, indexVector, d);
	}

	public static double determinantA(LUMatrix luMatrix) {
		double detA = luMatrix.reverse;
		int elementSize = luMatrix.matrix[0].length;

		for (int i = 0; i < elementSize; i++) {
			detA *= luMatrix.matrix[i][i];
		}
		return detA;
	}

	public static double[][] matrixInverse(LUMatrix luMatrix) {
		int elementSize = luMatrix.matrix[0].length;
		double[][] inverseMatrix = new double[elementSize][elementSize];
		double[] colVector, inverseVector;

		for (int j = 0; j < elementSize; j++) {
			colVector = new double[elementSize];
			for (int i = 0; i < elementSize; i++) {
				if (j == luMatrix.indexVector[i])
					colVector[i] = 1.0;
				else
					colVector[i] = 0.0;
			}
			inverseVector = helperSolve(luMatrix, colVector);
			for (int i = 0; i < elementSize; i++) inverseMatrix[i][j] = inverseVector[i];
		}
		return inverseMatrix;
	}

	// solve luMatrix * x = b
	public static double[] helperSolve(LUMatrix luMatrix, double[] b) {
		double sum;
		int n = luMatrix.matrix[0].length;

		double[] x = new double[n];
		System.arraycopy(b, 0, x, 0, b.length);

		for (int i = 1; i < n; ++i) {
			sum = x[i];
			for (int j = 0; j < i; ++j)
				sum -= luMatrix.matrix[i][j] * x[j];
			x[i] = sum;
		}
		x[n - 1] /= luMatrix.matrix[n - 1][n - 1];
		for (int i = n - 2; i >= 0; --i) {
			sum = x[i];
			for (int j = i + 1; j < n; ++j)
				sum -= luMatrix.matrix[i][j] * x[j];
			x[i] = sum / luMatrix.matrix[i][i];
		}
		return x;
	}
}
