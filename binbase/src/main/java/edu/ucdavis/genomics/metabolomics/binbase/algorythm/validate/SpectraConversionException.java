/*
 * Created on Oct 1, 2003
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate;


/**
 * @author wohlgemuth
 * @version Sep 18, 2003
 * <br>
 * BinBaseDatabase
 * @description
 */
public class SpectraConversionException extends RuntimeException {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2L;

    /**
     * @author wohlgemuth
     * @version Sep 18, 2003
     * <br>
     * BinBaseDatabase
     * @description
     */
    public SpectraConversionException() {
        super();
    }

    /**
     * @author wohlgemuth
     * @version Sep 18, 2003
     * <br>
     * BinBaseDatabase
     * @description
     */
    public SpectraConversionException(String message) {
        super(message);
    }

    /**
     * @author wohlgemuth
     * @version Sep 18, 2003
     * <br>
     * BinBaseDatabase
     * @description
     */
    public SpectraConversionException(Throwable cause) {
        super(cause);
    }

    /**
     * @author wohlgemuth
     * @version Sep 18, 2003
     * <br>
     * BinBaseDatabase
     * @description
     */
    public SpectraConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
