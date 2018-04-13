/**
 * Created on Jul 17, 2003
 */
package edu.ucdavis.genomics.metabolomics.exception;


/**
 * <p>
 *
 * @author wohlgemuth
 * @version Jul 17, 2003
 * </p>
 * <p>
 * <h4>File: ValueNotFoundException.java </h4>
 * <h4>Project: glibj </h4>
 * <h4>Package: edu.ucdavis.genomics.metabolomics.glibj.alg.search </h4>
 * <h4>Type: ValueNotFoundException </h4>
 */
public class ValueNotFoundException extends RuntimeException {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2L;

    /**
     *
     */
    public ValueNotFoundException() {
        super();
    }

    /**
     * @param message
     */
    public ValueNotFoundException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ValueNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ValueNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
