package edu.ucdavis.genomics.metabolomics.exception;


/**
 * thown in cases of calculation mistakes
 * @author wohlgemuth
 *
 */
public class CalculationException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 2L;

    /**
     * Creates a new CalculationException object.
     */
    public CalculationException() {
        super();

        
    }

    /**
     * Creates a new CalculationException object.
     *
     * @param message DOCUMENT ME!
     */
    public CalculationException(String message) {
        super(message);

        
    }

    /**
     * Creates a new CalculationException object.
     *
     * @param message DOCUMENT ME!
     * @param cause DOCUMENT ME!
     */
    public CalculationException(String message, Throwable cause) {
        super(message, cause);

        
    }

    /**
     * Creates a new CalculationException object.
     *
     * @param cause DOCUMENT ME!
     */
    public CalculationException(Throwable cause) {
        super(cause);

        
    }
}
