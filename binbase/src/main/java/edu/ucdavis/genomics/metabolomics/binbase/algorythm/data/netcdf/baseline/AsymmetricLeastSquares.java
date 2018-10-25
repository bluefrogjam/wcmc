package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.baseline;

import java.util.Arrays;


/**
 * Peforms asymmetric least squares smoothing for baseline computation.
 * Algorithm by Eilers, P.H.C & Boelens, H.F.M:
 * http://www.science.uva.nl/~hboelens/publications/draftpub/Eilers_2005.pdf
 */
public class AsymmetricLeastSquares {
	public static final double EPSILON = 1.0e-8;

	private double lambda;
	private double p;
	private double[] chromatogram;
	private int n;


	/**
	 *
	 * @param smoothing
	 * @param asymmetry
	 */
	public AsymmetricLeastSquares(double smoothing, double asymmetry) {
		this.chromatogram = new double[0];
		this.n = 0;
		this.lambda = smoothing;
		this.p = asymmetry;
	}


	/**
	 *
	 * @param chromatogram
	 */
	public void setChromatogram(double[] chromatogram) {
		this.chromatogram = chromatogram;
		this.n = chromatogram.length;
	}


	/**
	 *
	 * @return
	 */
	public double[] getBaseline() {
		double[] w = new double[n];
		double[] z = new double[n];
		Arrays.fill(w, 1);

		double diff;

		do {
			diff = 0;
			z = smooth(w, z);

			for(int i = 0; i < n; i++) {
				double wp = w[i];
				w[i] = (chromatogram[i] - z[i] > EPSILON) ? p : 1.0 - p;
				diff += Math.abs(w[i] - wp);
			}
		} while(diff > 0);

		return z;
	}

	/**
	 * Implementation of smooth2 C routine in R's ptw package.
	 * @param w weights
	 * @param z smoothed vector
	 */
	private double[] smooth(final double[] w, double[] z) {
		// Initialize computation vectors
		double[] c = new double[n];
		double[] d = new double[n];
		double[] e = new double[n];

		// Set initial
		d[0] = w[0] + lambda;
		c[0] = -2 * lambda / d[0];
		e[0] = lambda / d[0];
		z[0] = w[0] * chromatogram[0];

		d[1] = w[1] + 5 * lambda - d[0] * c[0] * c[0];
		c[1] = (-4 * lambda - d[0] * c[0] * e[0]) / d[1];
		e[1] = lambda / d[1];
		z[1] = w[1] * chromatogram[1] - c[0] * z[0];

		for(int i = 2; i < n - 2; i++) {
			d[i] = w[i] + 6 * lambda - c[i - 1] * c[i - 1] * d[i - 1] - e[i - 2] * e[i - 2] * d[i - 2];
			c[i] = (-4 * lambda - d[i - 1] * c[i - 1] * e[i - 1]) / d[i];
			e[i] = lambda / d[i];
			z[i] = w[i] * chromatogram[i] - c[i - 1] * z[i - 1] - e[i - 2] * z[i - 2];
		}

		d[n - 2] = w[n - 2] + 5 * lambda - c[n - 3] * c[n - 3] * d[n - 3] - e[n - 4] * e[n - 4] * d[n - 4];
		c[n - 2] = (-2 * lambda - d[n - 3] * c[n - 3] * e[n - 3]) / d[n - 2];
		z[n - 2] = w[n - 2] * chromatogram[n - 2] - c[n - 3] * z[n - 3] - e[n - 4] * z[n - 4];

		d[n - 1] = w[n - 1] + lambda - c[n - 2] * c[n - 2] * d[n - 2] - e[n - 3] * e[n - 3] * d[n - 3];
		z[n - 1] = (w[n - 1] * chromatogram[n - 1] - c[n - 2] * z[n - 2] - e[n - 3] * z[n - 3]) / d[n - 1];
		z[n - 2] = z[n - 2] / d[n - 2] - c[n - 2] * z[n - 1];

		for(int i = n - 3; 0 <= i; i--)
			z[i] = z[i] / d[i] - c[i] * z[i + 1] - e[i] * z[i + 2];

		return z;
	}
}