package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.math;

/**
 * Created by diego on 8/12/2016.
 */
public class MatrixOperations {

	public static LUMatrix matrixDecompose(double[][] rawMatrix) {
		int elementSize = rawMatrix[0].length;
		double[] scalingVector = new double[elementSize];
		int[] indexVector = new int[elementSize];
		double d = 1.0;

		for (int i = 0; i < elementSize; i++) {
			double big = 0.0;

			for (int j = 0; j < elementSize; j++) {
				if (Math.abs(rawMatrix[i][j]) > big)
					big = Math.abs(rawMatrix[i][j]);
			}

			if (big == 0.0) {
				System.out.println("Singular matrix in touine ludcmp");
				return null;
			}

			scalingVector[i] = 1.0 / big;
			indexVector[i] = i;
		}

		for (int j = 0; j < elementSize; j++) {
			int i_max = j;

			for (int i = 0; i < j; i++) {
				double sum = rawMatrix[i][j];

				for (int k = 0; k < i; k++) {
					sum -= rawMatrix[i][k] * rawMatrix[k][j];
				}

				rawMatrix[i][j] = sum;
			}

			double big = 0.0;

            for (int i = j; i < elementSize; i++) {
                double sum = rawMatrix[i][j];

				for (int k = 0; k < j; k++) {
					sum -= rawMatrix[i][k] * rawMatrix[k][j];
				}

				rawMatrix[i][j] = sum;

                double dum = scalingVector[i] * Math.abs(sum);

				if (dum >= big) {
					big = dum;
					i_max = i;
				}
			}

			if (j != i_max) {
				for (int k = 0; k < elementSize; k++) {
					double tmp = rawMatrix[i_max][k];
					rawMatrix[i_max][k] = rawMatrix[j][k];
					rawMatrix[j][k] = tmp;
				}

				d = -1 * d;
				double doubleSwap = scalingVector[i_max];
				scalingVector[i_max] = scalingVector[j];
				scalingVector[j] = doubleSwap;

				int intSwap = indexVector[i_max];
				indexVector[i_max] = indexVector[j];
				indexVector[j] = intSwap;
			}

			if (rawMatrix[j][j] == 0.0) {
				rawMatrix[j][j] = Math.pow(10, -10);
			}

			if (j != elementSize) {
				for (int i = j + 1; i < elementSize; i++)
				    rawMatrix[i][j] /= rawMatrix[j][j];
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
