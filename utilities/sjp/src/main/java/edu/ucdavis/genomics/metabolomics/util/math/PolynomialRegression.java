package edu.ucdavis.genomics.metabolomics.util.math;

import org.jdom2.Element;


/**
 * die routine zum berechnen von polynomen, welche eigentlich nur ein wrapper
 * ist um zu verhindern das jemand eine andere deviation setzt und
 *
 * @author wohlgemuth
 */
public class PolynomialRegression implements Regression {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     *
     * @uml.property name="poly"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private Polynom poly = new Polynom();
	private double[] y;
	private double[] x;
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

    public PolynomialRegression(int derivation) {
        this.poly.setDerivation(derivation);
    }

    public void setDerivation(int derivation){
    	this.poly.setDerivation(derivation);
    }
    /**
     * @return
     */
    public double[] getCoeffizent() {
        return this.poly.getCoeffizent();
    }

    /**
     * @param x
     * @param y
     */
    public void setData(double[] x, double[] y) {
        this.x = x;
        this.y = y;
    	
    	this.poly.setData(x, y);
        
        this.poly.calculate();
    }

    /**
     * @return
     */
    public int getDerivation() {
        return this.poly.getDerivation();
    }

    /**
     * @param x
     * @param coeffizent
     * @return
     */
    public double getY(double x) {
        return this.poly.getY(x, this.poly.getCoeffizent());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.poly.toString();
    }

	public void config(Element element) {
		if(element.getAttribute("order") != null){
			this.poly.setDerivation(Integer.parseInt(element.getAttributeValue("order")));
		}
	}

	@Override
	public double[] getXData() {
		// TODO Auto-generated method stub
		return x;
	}

	@Override
	public double[] getYData() {
		// TODO Auto-generated method stub
		return y;
	}

	@Override
	public String[] getFormulas() {
		return poly.getFormulas();
	}
}
