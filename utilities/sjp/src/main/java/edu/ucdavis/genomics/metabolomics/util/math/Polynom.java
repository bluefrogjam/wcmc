package edu.ucdavis.genomics.metabolomics.util.math;

import java.text.DecimalFormat;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.2 $
 */
public class Polynom implements Regression {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	private int derivation;

	/**
	 * DOCUMENT ME!
	 */
	private double[] coeffizent;

	/**
	 * DOCUMENT ME!
	 */
	private double[] x;

	/**
	 * DOCUMENT ME!
	 */
	private double[] y;

	/**
	 * Creates a new Polynom object.
	 */
	public Polynom() {
	}

	/**
	 * @param x
	 *            xvalues
	 * @param y
	 *            yvalues
	 */
	public Polynom(double[] x, double[] y,int derivation) {
		this.x = x;
		this.y = y;
		this.derivation = derivation;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * @uml.property name="coeffizent"
	 */
	public double[] getCoeffizent() {
		return this.coeffizent;
	}

	/**
	 * @param x
	 *            xvalues
	 * @param y
	 *            yvalues
	 */
	public void setData(double[] x, double[] y) {
		this.x = x;
		this.y = y;
		this.coeffizent = null;
	}

	/**
	 * setzt dir ableitung der berechnung
	 * 
	 * @param derivation
	 * @uml.property name="derivation"
	 */
	public void setDerivation(int derivation) {
		this.derivation = derivation + 1;
		this.coeffizent = null;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * @uml.property name="derivation"
	 */
	public int getDerivation() {
		return this.derivation;
	}

	/**
	 * @param xx
	 *            value
	 * @param regressioncoeffizent
	 *            koeffizienten zur berechnung
	 * @return y value
	 */
	public double getY(double xx, double[] regressioncoeffizent) {
		double yy = 0;

		if (this.coeffizent == null) {
			this.calculate();
		}

		for (int i = 0; i < regressioncoeffizent.length; i++) {
			yy += (regressioncoeffizent[i] * Math.pow(xx, i));
		}

		return yy;
	}

	/**
	 * DOCUMENT ME!
	 */
	public void calculate() {
		if (this.x.length != this.y.length) {
			throw new RuntimeException("Arrays have different length");
		}

		if (this.x.length == 0) {
			throw new RuntimeException("Arrays is empty");
		}

		int length = x.length;

		Jama.Matrix mat = new Jama.Matrix(length, this.derivation);
		Jama.Matrix bmat = new Jama.Matrix(length, 1);

		for (int j = 0; j < this.derivation; j++) {
			for (int i = 0; i < length; i++) {
				mat.set(i, j, Math.pow(this.x[i], j));
			}
		}

		for (int i = 0; i < length; i++) {
			bmat.set(i, 0, this.y[i]);
		}

		this.coeffizent = mat.solve(bmat).getRowPackedCopy();
	}

	/*
	 * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.math.Regression#getY(double)
	 */
	public double getY(double xx) {
		if (this.coeffizent == null) {
			this.calculate();
		}

		return this.getY(xx, this.coeffizent);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String toString() {
		this.calculate();

		StringBuffer buffer = new StringBuffer();

		buffer.append("y\t=\t");

		DecimalFormat format = new DecimalFormat("#.#################################");

		for (int i = this.coeffizent.length - 1; i >= 0; i--) {
			if (i == 1) {
				buffer.append(" " + this.coeffizent[i] + "x");
				buffer.append(" +");
			}
			else if (i == 0) {
				buffer.append(" " + this.coeffizent[i]);
			}
			else {
				buffer.append(" " + format.format(this.coeffizent[i]) + "x^" + i);
				buffer.append(" +");
			}
		}

		buffer.append("\n");
		buffer.append("d\t=\t" + (this.derivation - 1));
		buffer.append("\n");
		buffer.append("c\t=\t");

		for (int i = 0; i < (this.coeffizent.length - 1); i++) {
			buffer.append(this.coeffizent[i] + ", ");
		}

		buffer.append(coeffizent[coeffizent.length - 1]);
		buffer.append("\n");
		buffer.append("y - values calculated\n");
		buffer.append("\n");

		for (int i = 0; i < this.x.length; i++) {
			buffer.append("y[" + i + "]\t=\t" + this.getY(x[i], this.getCoeffizent()));
			buffer.append("\n");
		}

		buffer.append("\n");
		buffer.append("y - values calibration\n");
		buffer.append("\n");

		for (int i = 0; i < this.x.length; i++) {
			buffer.append("y[" + i + "]\t=\t" + y[i]);
			buffer.append("\n");
		}

		buffer.append("\n");
		buffer.append("x - values\n");
		buffer.append("\n");

		for (int i = 0; i < this.x.length; i++) {
			buffer.append("x[" + i + "]\t=\t" + x[i]);
			buffer.append("\n");
		}

		return buffer.toString();
	}

	@Override
	public double[] getXData() {
		return x;
	}

	@Override
	public double[] getYData() {
		return y;
	}

	@Override
	public String[] getFormulas() {
		this.calculate();

		StringBuffer buffer = new StringBuffer();

		buffer.append("y\t=\t");

		DecimalFormat format = new DecimalFormat("0.000E0");

		for (int i = this.coeffizent.length - 1; i >= 0; i--) {
			if (i == 1) {
				buffer.append(" " + this.coeffizent[i] + "x");
				buffer.append(" +");
			}
			else if (i == 0) {
				buffer.append(" " + this.coeffizent[i]);
			}
			else {
				buffer.append(" " + format.format(this.coeffizent[i]) + "x^" + i);
				buffer.append(" +");
			}
		}

		buffer.append("\n");

		return new String[] { buffer.toString() };
	}
}
